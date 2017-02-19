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

import com.octo.mbo.data.Pptx4jPackage;
import com.octo.mbo.data.XmlUpdatablePackage;
import com.octo.mbo.domain.Slide;
import com.octo.mbo.exceptions.CopyNotesException;
import com.octo.mbo.exceptions.NotImplementedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Paths;
import java.util.Map;

/**
 * Orchestrate different behaviour for PPTX and XML files according to the file extension
 * Prcessor implement it
 */
public class Orchestrator {
    static final String SHOUD_NEVER_COMES_HERE_IN_THEORY_CHECKED_PREVIOUSLY;

    static {
        SHOUD_NEVER_COMES_HERE_IN_THEORY_CHECKED_PREVIOUSLY = "Shoud never comes here: in theory checked previously";
    }

    private static Logger log = LoggerFactory.getLogger(Orchestrator.class);

    private final Loader loaderInjected;
    private final Processor processorInjected;

    Orchestrator(Loader loader, Processor processor) {
        this.loaderInjected = loader;
        this.processorInjected = processor;
    }

    public void run(String srcFilePath, String targetFilePath) throws CopyNotesException {
        try {
            String srcExtension = getExtension(srcFilePath);

            String targetExtension = getExtension(targetFilePath);

            java.nio.file.Path targetPath = Paths.get(targetFilePath);
            boolean targetFileExists = targetPath.toFile().exists();

            final Map<String, Slide> srcSlides;
            if ("pptx".equals(srcExtension)) {
                final Pptx4jPackage srcPackage = loaderInjected.loadPptx4ReadOnly(srcFilePath);
                srcSlides = srcPackage.getSlides();
            } else if ("xml".equals(srcExtension)) {
                final XmlUpdatablePackage srcXmlUpdatablePackage = loaderInjected.loadXml(srcFilePath);
                srcSlides = srcXmlUpdatablePackage.getSlides();
            } else {
                log.error(SHOUD_NEVER_COMES_HERE_IN_THEORY_CHECKED_PREVIOUSLY);
                srcSlides = null;
            }

            if (targetFileExists) {
                processorInjected.processWithExistingTargetFile(targetFilePath, targetExtension, srcSlides);
            } else {
                processorInjected.processWithANewTargetFile(targetFilePath, targetExtension, srcSlides);
            }
        } catch (NotImplementedException niex) {
            throw new CopyNotesException("Orchestrator error", niex);
        }
    }

    String getExtension(String srcFilePath) throws NotImplementedException {
        String srcExtension;
        if (srcFilePath.toLowerCase().endsWith(".pptx")) {
            srcExtension = "pptx";
        } else if (srcFilePath.toLowerCase().endsWith(".xml")) {
            srcExtension = "xml";
        } else throw new NotImplementedException("Only pptx and xml are supported");
        return srcExtension;
    }
}
