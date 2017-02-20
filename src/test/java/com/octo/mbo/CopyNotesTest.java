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

import com.octo.mbo.exceptions.CommandLineException;
import org.apache.commons.cli.CommandLine;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CopyNotesTest {
    private static Logger log = LoggerFactory.getLogger(CopyNotesTest.class);

    @Test(expected = CommandLineException.class)
    public void commandLineParseTestInError() throws CommandLineException {
        log.warn("This unit test will produce console output");
        CopyNotes.parseCommandLine(new String[]{""});
        log.warn("End of unit test producing console output");
    }

    @Test
    public void commandLineParserTest() throws CommandLineException {
        CommandLine cli = CopyNotes.parseCommandLine(new String[]{"-s", "/source/file", "-t", "/target/file"});
        Assert.assertEquals("/source/file", cli.getOptionValue("s"));
        Assert.assertEquals("/target/file", cli.getOptionValue("t"));
    }
}
