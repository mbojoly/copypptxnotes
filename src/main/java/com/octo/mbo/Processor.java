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
package com.octo.mbo;

import com.octo.mbo.data.UpdatablePackage;
import com.octo.mbo.data.XmlPackageFactory;
import com.octo.mbo.data.XmlUpdatablePackage;
import com.octo.mbo.io.Function;
import com.octo.mbo.data.updater.SlideUpdator;
import com.octo.mbo.domain.Merger;
import com.octo.mbo.domain.Slide;
import com.octo.mbo.exceptions.CopyNotesException;
import com.octo.mbo.exceptions.NotImplementedException;
import com.octo.mbo.io.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.OutputStream;
import java.util.Map;

import static java.util.Objects.requireNonNull;

/**
 * Implement different behaviour for PPTX and XML files
 * Simply export to XML if the target does not exists
 * Copy and merge if the target exists
 * Target PPTX must exist
 */
class Processor {
    private static Logger log = LoggerFactory.getLogger(Processor.class);

    private final Merger mergerInjected;
    private final Loader loaderInjected;
    private final SlideUpdator slideUpdatorInjected;
    private final XmlPackageFactory xmlPackageFactoryInjected;
    private final Util utilInjected;

    Processor(Merger merger, Loader loader, SlideUpdator slideUpdator, XmlPackageFactory xmlPackageFactory, Util util) {
        this.mergerInjected = merger;
        this.loaderInjected = loader;
        this.slideUpdatorInjected = slideUpdator;
        this.xmlPackageFactoryInjected = xmlPackageFactory;
        this.utilInjected = util;
    }

    void processWithANewTargetFile(String targetFilePath, String targetExtension, Map<String, Slide> srcSlides) throws CopyNotesException {
        if ("pptx".equals(targetExtension)) {
            throw new CopyNotesException(
                    "Target PPTX file must exist",
                    new NotImplementedException("CopyNotes to a pptx file that does not exist is not yet implemented"));
        } else if ("xml".equals(targetExtension)) {
            log.info("Save source comments to the target Xml file");
            XmlUpdatablePackage xmlUpdatablePackage = xmlPackageFactoryInjected.buildFromSlides(requireNonNull(srcSlides).values());
            Function<OutputStream, Object> function = os -> {
                xmlUpdatablePackage.saveChanges(os);
                return 0; //Only to Satisfy Function Type
            };

            utilInjected.applyToOutputFile(targetFilePath, function);
        } else {
            log.error("Shoud never comes here: in theory checked previously");
        }
    }

    void processWithExistingTargetFile(String targetFilePath, String targetExtension, Map<String, Slide> srcSlides) throws CopyNotesException {
        Map<String, Slide> tgtSlides;
        UpdatablePackage targetPackage = null;
        if ("pptx".equals(targetExtension)) {
            targetPackage = loaderInjected.loadPptx4Update(this.slideUpdatorInjected, targetFilePath);
            tgtSlides = targetPackage.getSlides();
        } else if ("xml".equals(targetExtension)) {
            targetPackage = loaderInjected.loadXml(targetFilePath);
            tgtSlides = targetPackage.getSlides();
        } else {
            log.error(Orchestrator.SHOUD_NEVER_COMES_HERE_IN_THEORY_CHECKED_PREVIOUSLY);
            tgtSlides = null;
        }

        //Merge source slidess and target slides
        mergerInjected.checkMapSizesAreEqual(srcSlides, tgtSlides);

        log.info("Merging source and target slides");
        Map<String, Slide> slidesPerPartName = mergerInjected.mergeSlides(targetFilePath, srcSlides, tgtSlides);
        final UpdatablePackage pkg = targetPackage; //For capture by lambda
        Function<OutputStream, Void> function = os -> {
            requireNonNull(pkg).updateStructure(slidesPerPartName);
            pkg.saveChanges(os);
            return null;
        };
        utilInjected.applyToOutputFile(targetFilePath, function);
    }
}
