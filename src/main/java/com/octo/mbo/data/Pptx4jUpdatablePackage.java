/**
 * Copyright (C) 2017 mbojoly (mbojoly@octo.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.octo.mbo.data;

import com.octo.mbo.data.extracter.SlideExtractorFactory;
import com.octo.mbo.data.updater.SlideUpdator;
import com.octo.mbo.domain.Slide;
import com.octo.mbo.exceptions.CopyNotesException;
import org.docx4j.openpackaging.packages.PresentationMLPackage;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.PartName;

import java.util.HashMap;
import java.util.Map;


public class Pptx4jUpdatablePackage extends Pptx4jPackage implements UpdatablePackage {
    private SlideUpdator slideUpdatorInjected;

    /**
     * Only for test purpose
     */
    Pptx4jUpdatablePackage(final SlideUpdator slideUpdator, final SlideExtractorFactory slideExtractorFactory, final PresentationMLPackage pMLpkg) {
        super(pMLpkg, slideExtractorFactory);
        this.slideUpdatorInjected = slideUpdator;

    }


    Pptx4jUpdatablePackage(final SlideUpdator slideUpdator, final SlideExtractorFactory slideExtractorFactory) {
        //Prevent instanciating
        super(slideExtractorFactory);
        this.slideUpdatorInjected = slideUpdator;
    }

    @Override
    public void updateStructure(Map<String, Slide> slidesPerPartName) throws CopyNotesException {
        Map<PartName, Part> hmTgt = this.getPptxPartMap();
        for (HashMap.Entry<PartName, Part> hmeTgt : hmTgt.entrySet()) {
            String partName = hmeTgt.getKey().getName();
            if (slidesPerPartName.containsKey(partName)) {
                slideUpdatorInjected.processEntry(hmeTgt.getValue(), slidesPerPartName.get(partName).getParagraphs());
            }
        }
    }
}
