/**
 * Copyright (C) 2017 mbojoly (mbojoly@octo.com)
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.octo.mbo;

import com.octo.mbo.exceptions.CopyCommentException;
import com.octo.mbo.extractor.SlideExtractor;
import com.octo.mbo.extractor.SlideExtractorFactory;
import com.octo.mbo.updator.SlideUpdator;
import com.octo.mbo.xml.JAXBMarshaller;
import com.octo.mbo.xml.Pptx4jPackage;
import com.octo.mbo.xml.Slide;
import com.octo.mbo.xml.SlideDocument;
import org.apache.commons.cli.*;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.PartName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBException;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;


public class CopyNotes {

    private static Logger log = LoggerFactory.getLogger(CopyNotes.class);

    private final SlideUpdator slideUpdatorInjected;
    private SlideExtractorFactory srcSlideExtractorFactoryInjected;

    CopyNotes() {
        slideUpdatorInjected = new SlideUpdator();
        srcSlideExtractorFactoryInjected = new SlideExtractorFactory();
    }

    /**
     * For testing only
     * @param slideUpdatorInjected dependencyInjection
     * @param srcSlideExtractorFactory dependencyInjection
     */
    CopyNotes(SlideUpdator slideUpdatorInjected, SlideExtractorFactory srcSlideExtractorFactory) {
        this.slideUpdatorInjected = slideUpdatorInjected;
        this.srcSlideExtractorFactoryInjected = srcSlideExtractorFactory;
    }


    public static void main(String[] args) throws JAXBException, IOException {
        CopyNotes copyNotes = new CopyNotes();

        try {
            CommandLine cli = parseCommandLine(args);

            final String srcFilePath = cli.getOptionValue("s");
            final String targetFilePath = cli.getOptionValue("t");
            
            log.info("Loading. {}..", srcFilePath);
            final Pptx4jPackage srcPackage = Pptx4jPackage.loadPackage(new FileInputStream(srcFilePath));
            log.info("{} loaded", srcFilePath);
            final Map<PartName, Part> hmSrc = srcPackage.loadPptxPartMap();

            log.info("Extracting comment from source file...");
            SlideExtractor srcSlideExtractor = copyNotes.extractNote(hmSrc);

            log.info("Loading. {}..", targetFilePath);
            final Pptx4jPackage targetPackage = Pptx4jPackage.loadPackage(new FileInputStream(targetFilePath));
            log.info("{} loaded", targetFilePath);
            final Map<PartName, Part> hmTgt = targetPackage.loadPptxPartMap();

            Map<String, Slide> srcSlides = srcSlideExtractor.getSlides();

            log.info("Extracting comment from target file...");
            SlideExtractor tgtSlideExtractor = copyNotes.extractNote(hmTgt);

            Map<String, Slide> tgtSlides = tgtSlideExtractor.getSlides();

            checkMapSizesAreEqual(srcSlides, tgtSlides);

            log.info("Merging source and target slides");
            Map<String, Slide> slidesPerPartName = copyNotes.mergeSlides(targetFilePath, srcSlides, tgtSlides);

            SlideDocument slideDocument = new SlideDocument();
            slideDocument.getSlides().addAll(slidesPerPartName.values());
            String xml = JAXBMarshaller.marshall(slideDocument);
            Files.write(Paths.get(targetFilePath + ".xml"), xml.getBytes());

            log.info("Updating target...");
            copyNotes.updatePptxStructure(hmTgt, slidesPerPartName);
            log.info("Target updated.");

            log.info("Saving modified target...");

            // All done: save it
            targetPackage.saveChanges(new FileOutputStream(targetFilePath));

            log.info("done .. {} saved ", targetFilePath);

        } catch (MissingOptionException mex) {
            log.error(mex.getMessage());
            log.debug("Error parsing command line {}", mex);
            System.exit(-1);
        } catch (ParseException pex) {
            log.error("Error parsing the command line. Please use CopyComment --help", pex);
            System.exit(-1);
        } catch (Docx4JException d4jex) {
            log.error("Exception processing the document. Exiting", d4jex);
            System.exit(-1);
        } catch (CopyCommentException ccex) {
            log.error("Internal error processing the document. Exiting", ccex);
            System.exit(-1);
        }
    }

    static CommandLine parseCommandLine(String[] args) throws ParseException {
        Options options = new Options();
        Option optSource = new Option("s", "Source file where the comments can be extracted (only pptx format supported)");
        optSource.setRequired(true);
        optSource.setArgs(1);
        optSource.setLongOpt("source");
        options.addOption(optSource);

        Option optTarget = new Option("t", "Target file where the comments are merged (case of pptx format)");
        optTarget.setRequired(true);
        optTarget.setArgs(1);
        optTarget.setLongOpt("target");
        options.addOption(optTarget);

        CommandLineParser cliParser = new DefaultParser();
        return cliParser.parse(options, args);
    }

    static void checkMapSizesAreEqual(Map<String, Slide> hmSrc, Map<String, Slide> hmTgt) throws CopyCommentException {
        final int srcSize = hmSrc.size();
        final int tgtSize = hmTgt.size();

        if (srcSize != tgtSize) {
            throw new CopyCommentException(String.format("Source document has %s slides and target document %s.", srcSize, tgtSize));
        }
        log.debug("Map of documents parts have the same size {} for src, {} for target", srcSize, tgtSize);
    }

    SlideExtractor extractNote(Map<PartName, Part> hm) throws CopyCommentException, Docx4JException {
        SlideExtractor slideExtractor = srcSlideExtractorFactoryInjected.build();
        for (Map.Entry<PartName, Part> hme : hm.entrySet()) {
            log.trace("ExtractComment from PartName {}", hme.getKey().getName());
            slideExtractor.processNoteEntry(hme);
        }
        return slideExtractor;
    }

    Map<String, Slide> mergeSlides(String targetFilePath, Map<String, Slide> srcSlides, Map<String, Slide> tgtSlides) {
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

    void updatePptxStructure(Map<PartName, Part> hmTgt, Map<String, Slide> slidesPerPartName) throws Docx4JException, CopyCommentException {
        for (HashMap.Entry<PartName, Part> hmeTgt : hmTgt.entrySet()) {
            String partName = hmeTgt.getKey().getName();
            if (slidesPerPartName.containsKey(partName)) {
                slideUpdatorInjected.processEntry(hmeTgt.getValue(), slidesPerPartName.get(partName).getParagraphs());
            }
        }
    }

}