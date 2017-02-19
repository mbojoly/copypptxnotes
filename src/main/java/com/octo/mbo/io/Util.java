package com.octo.mbo.io;

import com.octo.mbo.data.updater.Function;
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
