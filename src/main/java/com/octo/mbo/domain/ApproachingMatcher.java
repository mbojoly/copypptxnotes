package com.octo.mbo.domain;

import org.apache.commons.text.similarity.LevenshteinDistance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ApproachingMatcher {
    private final Pattern p = Pattern.compile("[ ]+");
    private final LevenshteinDistance levenshteinDistance = new LevenshteinDistance();
    private static Logger log = LoggerFactory.getLogger(ApproachingMatcher.class);

    /**
     * First ignore additional spaces "........"
     * @param inputString string to normalize
     * @return inputString without more than one consecutive space
     */
    public String normalize(String inputString) {
        assert inputString != null;

        Matcher m = p.matcher(inputString);
        return m.replaceAll(" ");
    }

    /**
     * Sort the list of keys according to their distance with the reference key
     * @param keysOfSlides List of keys
     * @param key A reference key that will be compared to each String
     * @return A map whose index are the distances and values a list of keys for the corresponding distance.
     */
    public SortedMap<Integer, Set<String>> sortByDistanceWithKey(Collection<String> keysOfSlides, String key) {
        assert keysOfSlides != null;
        assert key != null;

        SortedMap<Integer, Set<String>> keysSortedByDistance = new TreeMap<>();

        for(String slideKey : keysOfSlides) {
            int distK2k = levenshteinDistance.apply(
                    normalize(key),
                    normalize(slideKey)
            );
            if(keysSortedByDistance.containsKey(distK2k)) {
                keysSortedByDistance.get(distK2k).add(slideKey);
            } else {
                keysSortedByDistance.put(distK2k, new HashSet<>((Collections.singletonList(slideKey))));
            }
        }

        log.trace("Sort by least distance to '{}' after normalization : {}", key, keysSortedByDistance);

        return keysSortedByDistance;
    }

    public Optional<String> findBestMatch(Collection<String> srcSlidesKeys, Collection<String> tgtSlideKeys,
                                        String tgtKeyToMatch) {
        assert srcSlidesKeys != null;
        assert tgtSlideKeys != null;
        assert tgtKeyToMatch != null;

        SortedMap<Integer, Set<String>> sortedSrcSlidesKeys = sortByDistanceWithKey(srcSlidesKeys, tgtKeyToMatch);
        int smallestDistanceBtwTgtKeyToMatchAndSrcKey = sortedSrcSlidesKeys.firstKey();
        Set<String> potentialKeys = sortedSrcSlidesKeys.get(smallestDistanceBtwTgtKeyToMatchAndSrcKey);
        if(potentialKeys.size() > 1) {
            log.warn("Tgt key '{}' could both match {} with levenstein distance {}",
                    tgtKeyToMatch, potentialKeys, smallestDistanceBtwTgtKeyToMatchAndSrcKey);
            return Optional.empty();
        } else {
            String potentialKey = potentialKeys.iterator().next();
            //Be sure that this potential key is to at the equal distance of another tgtKey
            SortedMap<Integer, Set<String>> sortedTgtSlideKeys = sortByDistanceWithKey(tgtSlideKeys, potentialKey);
            int smallestDistanceBtwPotentialKeyAndTgtKey = sortedTgtSlideKeys.firstKey();
            Set<String> potentialTgtKeys = sortedTgtSlideKeys.get(smallestDistanceBtwPotentialKeyAndTgtKey);
            if(potentialTgtKeys.size() > 1) {
                log.warn("Potential Src key '{}' matching Tgt keys {} could both match TgtKeys {} with levenstein distance {}",
                        tgtKeyToMatch, potentialKeys, potentialTgtKeys, smallestDistanceBtwTgtKeyToMatchAndSrcKey);
                return Optional.empty();
            } else {
                String potentialTgtKey = potentialTgtKeys.iterator().next();
                if(potentialTgtKey.equals(tgtKeyToMatch)) {
                    log.info("Src key '{}' has been associated with Tgt key '{}'", potentialKey, potentialTgtKey);
                    return Optional.of(potentialKey);
                } else {
                    log.warn("Potential Src key '{}' matches Tgt key '{}' with distance '{}' with is different from the Tgt we looked up '{}'",
                            potentialKey, potentialTgtKey, smallestDistanceBtwPotentialKeyAndTgtKey, tgtKeyToMatch);
                    return Optional.empty();
                }
            }
        }
    }
}
