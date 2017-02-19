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
package com.octo.mbo.io;

import com.octo.mbo.exceptions.CopyNotesException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

public class Util {
    private static Logger log = LoggerFactory.getLogger(Util.class);

    public <T> T applyToOutputFile(String targetFilePath, Function<OutputStream, T> function) throws CopyNotesException {
        try (OutputStream targetStream = new FileOutputStream(targetFilePath)) {
            T result = function.apply(targetStream);
            log.info("done .. {} saved", targetFilePath);
            return result;
        } catch (FileNotFoundException fnfex) {
            throw new CopyNotesException("No file found for " + targetFilePath, fnfex);
        } catch (IOException ioex) {
            throw new CopyNotesException("Error writing raw content for " + targetFilePath, ioex);
        }
    }

    public <T> T applyToInputFile(String sourceFilePath, Function<InputStream, T> function) throws CopyNotesException {
        log.info("Loading. {}..", sourceFilePath);
        try (InputStream inputStream = new FileInputStream(sourceFilePath)) {
            T result = function.apply(inputStream);
            log.info("{} loaded", sourceFilePath);
            return result;
        } catch (FileNotFoundException fnfex) {
            throw new CopyNotesException("File not found " + sourceFilePath, fnfex);
        } catch (IOException ioex) {
            throw new CopyNotesException("Error opening " + sourceFilePath, ioex);
        }
    }
}
