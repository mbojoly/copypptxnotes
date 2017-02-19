/**
 * Copyright (C) 2017 mbojoly (mbojoly@octo.com)
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.octo.mbo.data;

import com.octo.mbo.data.extracter.SlideExtractor;
import com.octo.mbo.data.extracter.SlideExtractorFactory;
import com.octo.mbo.domain.Slide;
import com.octo.mbo.exceptions.CopyNotesException;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.PresentationMLPackage;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.PartName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.OutputStream;
import java.util.Map;

public class Pptx4jPackage {

    private static Logger log = LoggerFactory.getLogger(Pptx4jPackage.class);

    PresentationMLPackage pMLpkg;
    private SlideExtractorFactory slideExtractorFactoryInjected;


    /**
     * Only for test purpose
     */
    Pptx4jPackage(final PresentationMLPackage pMLpkg, final SlideExtractorFactory slideExtractorFactory) {
        this.pMLpkg = pMLpkg;
        this.slideExtractorFactoryInjected = slideExtractorFactory;
    }


    Pptx4jPackage(final SlideExtractorFactory slideExtractorFactory) {
        //Prevent instanciating
        this.slideExtractorFactoryInjected = slideExtractorFactory;
    }

    /**
     * Extract slides
     *
     * @return Map with first string found in the slide as key and Slide object as value
     * @throws CopyNotesException for every errors
     */
    public Map<String, Slide> getSlides() throws CopyNotesException {
        final Map<PartName, Part> hm = getPptxPartMap();

        log.info("Extracting comment from source file...");
        SlideExtractor slideExtractor = slideExtractorFactoryInjected.build();
        for (Map.Entry<PartName, Part> hme : hm.entrySet()) {
            log.trace("ExtractComment from PartName {}", hme.getKey().getName());
            slideExtractor.processNoteEntry(hme);
        }

        return slideExtractor.getSlides();
    }


    Map<PartName, Part> getPptxPartMap() throws CopyNotesException {
        checkPackageNullElements();
        return this.pMLpkg.getParts().getParts();
    }

    @SuppressWarnings("ALL") //Has to be public in order to be compatible with interface visibility
    public void saveChanges(final OutputStream outputStream) throws CopyNotesException {
        if (outputStream == null) {
            throw new CopyNotesException("Can not save on null outputStream");
        }
        try {
            this.pMLpkg.save(outputStream);
        } catch (Docx4JException docx4jEx) {
            throw new CopyNotesException("Unable to save package", docx4jEx);
        }
    }

    /**
     * In the 3.3.1 version of docx4j this exception can never be thrown (but it is not documented)
     *
     * @throws CopyNotesException If elements of package are null
     */
    private void checkPackageNullElements() throws CopyNotesException {
        if (this.pMLpkg.getParts() == null ||
                this.pMLpkg.getParts().getParts() == null) {
            throw new CopyNotesException("Source document tree has a null element. Exiting");
        }
    }

}
