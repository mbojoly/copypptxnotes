package com.octo.mbo.domain;


import com.octo.mbo.exceptions.CopyNotesException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class Merger {
    private static Logger log = LoggerFactory.getLogger(Merger.class);

    public void checkMapSizesAreEqual(Map<String, Slide> hmSrc, Map<String, Slide> hmTgt) throws CopyNotesException {
        final int srcSize = hmSrc.size();
        final int tgtSize = hmTgt.size();

        if (srcSize != tgtSize) {
            throw new CopyNotesException(String.format("Source document has %s slides and target document %s.", srcSize, tgtSize));
        }
        log.debug("Map of documents parts have the same size {} for src, {} for target", srcSize, tgtSize);
    }

    public Map<String, Slide> mergeSlides(String targetFilePath, Map<String, Slide> srcSlides, Map<String, Slide> tgtSlides) {
        Set<String> processedKeys = new HashSet<>();

        Map<String, Slide> slidesPerPartName = new HashMap<>();
        for (Map.Entry<String, Slide> e : tgtSlides.entrySet()) {
            if (!srcSlides.containsKey(e.getKey())) {
                log.warn("No slide with key {} in the source document. This slide has been ignored.", e.getKey());
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
                log.warn("No slide with first string {} has been found in the target document. The corresponding comments are not copied", s);
            }
        }
        return slidesPerPartName;
    }
}
