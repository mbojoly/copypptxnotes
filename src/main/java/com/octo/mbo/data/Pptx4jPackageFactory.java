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

import com.octo.mbo.data.extracter.SlideExtractorFactory;
import com.octo.mbo.data.updater.SlideUpdator;
import com.octo.mbo.exceptions.CopyNotesException;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.OpcPackage;
import org.docx4j.openpackaging.packages.PresentationMLPackage;

import java.io.InputStream;

/**
 * To allow mocks
 */
public class Pptx4jPackageFactory {

    public Pptx4jUpdatablePackage loadPackage(SlideUpdator slideUpdator,
                                              final SlideExtractorFactory slideExtractorFactory,
                                              final InputStream inputStream) throws CopyNotesException {
        if (inputStream == null) {
            throw new CopyNotesException("Can not load from a null InputStream");
        }
        Pptx4jUpdatablePackage instance = new Pptx4jUpdatablePackage(slideUpdator, slideExtractorFactory);
        try {
            instance.pMLpkg =
                    (PresentationMLPackage) OpcPackage.load(inputStream);
            return instance;
        } catch (Docx4JException docx4jEx) {
            throw new CopyNotesException("Unable to load from the InputStream", docx4jEx);
        }
    }

    public Pptx4jPackage loadPackage(final SlideExtractorFactory slideExtractorFactory, final InputStream inputStream) throws CopyNotesException {
        if (inputStream == null) {
            throw new CopyNotesException("Can not load from a null InputStream");
        }
        Pptx4jPackage instance = new Pptx4jPackage(slideExtractorFactory);
        try {
            instance.pMLpkg =
                    (PresentationMLPackage) OpcPackage.load(inputStream);
            return instance;
        } catch (Docx4JException docx4jEx) {
            throw new CopyNotesException("Unable to load from the InputStream", docx4jEx);
        }
    }
}
