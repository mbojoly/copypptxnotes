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
package com.octo.mbo.data.domain;

import com.octo.mbo.domain.Merger;
import com.octo.mbo.domain.Slide;
import com.octo.mbo.exceptions.CopyNotesException;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.junit.Assert;
import org.junit.Test;

import java.util.*;


public class MergerTest {

    @Test
    public void checkMapSizeAreOkFailsIfSrcGreaterThanTarget() throws InvalidFormatException, CopyNotesException {
        Map<String, Slide> hmSrc = new HashMap<>();
        Map<String, Slide> hmTgt = new HashMap<>();
        hmSrc.put("Title", new Slide("/partname", "Title", Collections.singletonList("Paragraph 1")));

        Merger merger = new Merger();
        merger.checkMapSizesAreOk(hmSrc, hmTgt);
    }

    @Test
    public void checkMapSizeAreOkFailsIfSrcSmallerThanTarget() throws InvalidFormatException, CopyNotesException {
        Map<String, Slide> hmSrc = new HashMap<>();
        Map<String, Slide> hmTgt = new HashMap<>();
        hmTgt.put("Title", new Slide("/partname", "Title", Collections.singletonList("Paragraph 1")));

        Merger merger = new Merger();
        merger.checkMapSizesAreOk(hmSrc, hmTgt);
    }

    @Test
    public void mergeSlideTest() {
        String targetFilePath = "/target/file/path/test";

        List<Slide> slidesSrc = Arrays.asList(
                new Slide("/part1", "Slide 1", new ArrayList<>(Collections.singletonList("Paragraph 11"))),
                new Slide("/part2", "Slide 2", new ArrayList<>(Collections.singletonList("Paragraph 21"))),
                new Slide("/part3", "Slide 3", new ArrayList<>(Collections.singletonList("Paragraph 31")))
        );
        // Source and target slides must be different so that adding a paragraph has no side effect
        List<Slide> slidesTgt = Arrays.asList(
                new Slide("/part1", "Slide 1", new ArrayList<>(Collections.singletonList("Paragraph 11"))),
                new Slide("/part2", "Slide 2", new ArrayList<>(Collections.singletonList("Paragraph 21"))),
                new Slide("/part3", "Slide 3", new ArrayList<>(Collections.singletonList("Paragraph 31")))
        );

        Map<String, Slide> srcSlides = new HashMap<>();
        srcSlides.put(slidesSrc.get(0).getTitle(), slidesSrc.get(0));
        srcSlides.put(slidesSrc.get(1).getTitle(), slidesSrc.get(1));
        srcSlides.put(slidesSrc.get(2).getTitle(), slidesSrc.get(2));
        Map<String, Slide> tgtSlides = new HashMap<>();
        tgtSlides.put(slidesSrc.get(0).getTitle(), slidesTgt.get(0));
        tgtSlides.put(slidesSrc.get(2).getTitle(), slidesTgt.get(2));
        tgtSlides.put(slidesSrc.get(1).getTitle(), slidesTgt.get(1));

        Merger merger = new Merger();
        merger.mergeSlides(targetFilePath, srcSlides, tgtSlides);

        Assert.assertEquals(3, tgtSlides.size());
        Assert.assertEquals(3, tgtSlides.get("Slide 1").getParagraphs().size());
        Assert.assertEquals(3, tgtSlides.get("Slide 2").getParagraphs().size());
        Assert.assertEquals(3, tgtSlides.get("Slide 3").getParagraphs().size());
        Assert.assertEquals("Paragraph 11", tgtSlides.get("Slide 1").getParagraphs().get(0));
        Assert.assertEquals("=== Original comments from " + targetFilePath + "===", tgtSlides.get("Slide 1").getParagraphs().get(1));
        Assert.assertEquals("Paragraph 11", tgtSlides.get("Slide 1").getParagraphs().get(2));
    }
}
