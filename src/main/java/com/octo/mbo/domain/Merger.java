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
package com.octo.mbo.domain;


import com.octo.mbo.exceptions.CopyNotesException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class Merger {
    private static Logger log = LoggerFactory.getLogger(Merger.class);

    public void checkMapSizesAreOk(Map<String, Slide> hmSrc, Map<String, Slide> hmTgt) throws CopyNotesException {
        final int srcSize = hmSrc.size();
        final int tgtSize = hmTgt.size();

        if (srcSize > tgtSize) {
            log.warn("Map of target documents parts have a smaller size {} than src, {}", srcSize, tgtSize);
        }
        else if (srcSize == tgtSize) {
            log.debug("Map of documents parts have the same size {} for src, {} for target", srcSize, tgtSize);
        }
        else {
            log.debug("Map of target documents parts have a greater size {} for src, {} for target", srcSize, tgtSize);
        }
    }

    public Map<String, Slide> mergeSlides(String targetFilePath, Map<String, Slide> srcSlides, Map<String, Slide> tgtSlides) {
        Set<String> processedKeys = new HashSet<>();

        Map<String, Slide> slidesPerPartName = new HashMap<>();
        for (Map.Entry<String, Slide> e : tgtSlides.entrySet()) {
            if (!srcSlides.containsKey(e.getKey())) {
                log.warn("SRC slide missing. No slide with key \"{}\" in the source document. This slide has been ignored.", e.getKey());
            } else {
                Slide srcSlide = srcSlides.get(e.getKey());
                if (srcSlide == null || srcSlide.getParagraphs() == null) {
                    log.error("Slide data are null. Slide {} has been ignored.", e.getKey());
                } else {
                    processedKeys.add(e.getKey());
                    List<String> srcParagraphs = srcSlide.getParagraphs();
                    List<String> tgtParagraphs = e.getValue().getParagraphs();
                    //Preprend source comments (they are the reference)
                    tgtParagraphs.add(0, "=== Original comments from " + targetFilePath + "===");
                    tgtParagraphs.addAll(0, srcParagraphs);
                }
            }
            //Switch key  from first string to partName
            slidesPerPartName.put(e.getValue().getPartName(), e.getValue());
        }

        //Check if some source slides are missing
        for (String s : srcSlides.keySet()) {
            if (!processedKeys.contains(s)) {
                log.warn("TARGET slide missing. No slide with first string \"{}\" has been found in the target document. The corresponding comments are not copied", s);
            }
        }
        return slidesPerPartName;
    }
}
