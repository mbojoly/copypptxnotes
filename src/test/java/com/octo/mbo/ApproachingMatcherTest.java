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

import com.octo.mbo.domain.ApproachingMatcher;
import org.hamcrest.collection.IsIterableContainingInOrder;
import org.junit.Assert;
import org.junit.Test;

import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsMapContaining.hasEntry;

public class ApproachingMatcherTest {

    @Test
    public void normalizeShouldRemoveAdditionalSpaces() {
        //Given
        ApproachingMatcher approachingMatcher = new ApproachingMatcher();
        String inputString = "The Six Benefits of Cloud Computing                     on AWS: #2";

        //When
        String normalized = approachingMatcher.normalize(inputString);

        //Then
        Assert.assertEquals("The Six Benefits of Cloud Computing on AWS: #2", normalized);
    }

    @Test
    public void slideKeysShouldBeSortedByLevensteinDistanceWithReferenceKey() {
        //Given
        String referenceKey = "key";
        Collection<String> keysOfSlides = Arrays.asList("kexx", "Kexx", "keyy", "kex", "ke      x");
        ApproachingMatcher approachingMatcher = new ApproachingMatcher();

        //When
        SortedMap<Integer, Set<String>> keysSortedByDistance  =
                approachingMatcher.sortByDistanceWithKey(keysOfSlides, referenceKey);

        //Then
        assertThat(keysSortedByDistance.keySet(),IsIterableContainingInOrder.contains(1,2,3));
        //Use a set so that the test does not depend of the order of the keys for the same distance
        assertThat(keysSortedByDistance, hasEntry(1, new HashSet<>(Arrays.asList("keyy", "kex"))));
        assertThat(keysSortedByDistance, hasEntry(2, new HashSet<>(Arrays.asList("kexx", "ke      x"))));
        assertThat(keysSortedByDistance, hasEntry(3, new HashSet<>(Collections.singletonList("Kexx"))));
    }

    @Test
    public void bestMatchShouldBeTheSrcKeyWithLeastLevensteinDistance() {
        ///Given
        String referenceKey = "key";
        Collection<String> keysOfSrcSlides = Arrays.asList("kex", "Kexx", "ke      x");
        Collection<String> keysOfTgtSlides = Arrays.asList("key", "kexxxx", "Kexx", "ke      y");
        ApproachingMatcher approachingMatcher = new ApproachingMatcher();

        //When
        Optional<String> tgtKey = approachingMatcher.findBestMatch(keysOfSrcSlides, keysOfTgtSlides, referenceKey);

        //Then
        Assert.assertEquals(Optional.of("kex"), tgtKey);
    }

    @Test
    public void shouldNotMatchIfSeveralSrcKeysAreAtTheSameDistance() {
        ///Given
        String referenceKey = "key";
        Collection<String> keysOfSrcSlides = Arrays.asList("kex", "kez", "Kexx", "ke      x");
        Collection<String> keysOfTgtSlides = Arrays.asList("key", "kexxxx", "Kexx", "ke      y");
        ApproachingMatcher approachingMatcher = new ApproachingMatcher();

        //When
        Optional<String> tgtKey = approachingMatcher.findBestMatch(keysOfSrcSlides, keysOfTgtSlides, referenceKey);

        //Then
        Assert.assertEquals(Optional.empty(), tgtKey);
    }

    @Test
    public void shouldNotMatchIfSeveralTgtKeysAreAtTheSameDistance() {
        ///Given
        String referenceKey = "key";
        Collection<String> keysOfSrcSlides = Arrays.asList("kex", "Kexx", "ke      x");
        Collection<String> keysOfTgtSlides = Arrays.asList("key", "kez", "kexxxx", "Kexx", "ke      y");
        ApproachingMatcher approachingMatcher = new ApproachingMatcher();

        //When
        Optional<String> tgtKey = approachingMatcher.findBestMatch(keysOfSrcSlides, keysOfTgtSlides, referenceKey);

        //Then
        Assert.assertEquals(Optional.empty(), tgtKey);
    }

    @Test
    public void shouldNotMatchIfPotentialKeyIsCloserToAnotherKeyThanTgtKey() {
        ///Given
        String referenceKey = "key";
        Collection<String> keysOfSrcSlides = Arrays.asList("kex", "Kexx", "ke      xz");
        Collection<String> keysOfTgtSlides = Arrays.asList("key", "kex", "kexxxx", "Kexx", "ke      y");
        ApproachingMatcher approachingMatcher = new ApproachingMatcher();

        //When
        Optional<String> tgtKey = approachingMatcher.findBestMatch(keysOfSrcSlides, keysOfTgtSlides, referenceKey);

        //Then
        Assert.assertEquals(Optional.empty(), tgtKey);
    }
}
