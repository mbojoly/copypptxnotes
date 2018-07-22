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
package com.octo.mbo.data.extracter;

import org.junit.Assert;
import org.junit.Test;

import java.util.Optional;

public class FirstStringAppenderTest {
    @Test
    public void FirstStringAppenderTest() {
        SlideExtractor.FirstStringAppender firstStringAppender = new SlideExtractor.FirstStringAppender();

        firstStringAppender.accept("String 1");
        firstStringAppender.accept("String 2");

        Assert.assertEquals(Optional.of("String 1"), firstStringAppender.getContent());
    }

    @Test
    public void acceptTextIfFirstStringIsNull() {
        //Given
        SlideExtractor.FirstStringAppender firstStringAppender = new SlideExtractor.FirstStringAppender();
        firstStringAppender.firstString = null;

        //When
        firstStringAppender.accept("toto");

        //Then
        Assert.assertEquals("toto", firstStringAppender.firstString);

    }

    @Test
    public void acceptTextIfFirstStringLengthIsLessThan10() {
        //Given
        SlideExtractor.FirstStringAppender firstStringAppender = new SlideExtractor.FirstStringAppender();
        firstStringAppender.firstString = "1234567";

        //When
        firstStringAppender.accept("toto");

        //Then
        Assert.assertEquals("1234567 toto", firstStringAppender.firstString);
    }

    @Test
    public void doNotAcceptTextIfFirstStringLengthIsEqualsOrGreaterThan8() {
        //Given
        SlideExtractor.FirstStringAppender firstStringAppender = new SlideExtractor.FirstStringAppender();
        firstStringAppender.firstString = "12345678";

        //When
        firstStringAppender.accept("toto");

        //Then
        Assert.assertEquals("12345678", firstStringAppender.firstString);
    }

    @Test
    public void acceptAndOverrideIfAStringIsMoreThan8AndFirstStringIsSmaller() {
        //Given
        SlideExtractor.FirstStringAppender firstStringAppender = new SlideExtractor.FirstStringAppender();
        firstStringAppender.firstString = "12345678";

        //When
        firstStringAppender.accept("ABCDEFGHIJ");

        //Then
        Assert.assertEquals("ABCDEFGHIJ", firstStringAppender.firstString);
    }

    @Test
    public void acceptTwiceIfAStringIsMoreThan8AndFirstStringIsSmaller() {
        //Given
        SlideExtractor.FirstStringAppender firstStringAppender = new SlideExtractor.FirstStringAppender();
        firstStringAppender.firstString = "12345678";

        //When
        firstStringAppender.accept("ABCDEFGHIJ");
        firstStringAppender.accept("KLMNOPQRSTUVWXYZ");

        //Then
        Assert.assertEquals("KLMNOPQRSTUVWXYZ", firstStringAppender.firstString);
    }

    @Test
    public void nullInputDoesNotOverride() {
        //Given
        SlideExtractor.FirstStringAppender firstStringAppender = new SlideExtractor.FirstStringAppender();
        firstStringAppender.firstString = "toto";

        //When
        firstStringAppender.accept(null);

        //Then
        Assert.assertEquals("toto null", firstStringAppender.firstString);
    }

}
