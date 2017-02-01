package com.octo.mbo;

import org.apache.commons.cli.*;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.OpcPackage;
import org.docx4j.openpackaging.packages.PresentationMLPackage;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.PartName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;


public class CopyComment {

    private static Logger log = LoggerFactory.getLogger(CopyComment.class);


    public static void main(String[] args) {

        CommandLine cli = parseCommandLine(args);

        final String SRC_FILE_PATH = cli.getOptionValue("s");
        final String TARGET_FILE_PATH = cli.getOptionValue("t");

        try {
            final PresentationMLPackage pMLpkgSrc = loadPackage(SRC_FILE_PATH);
            final HashMap<PartName, Part> hmSrc = loadPptxPartMap(pMLpkgSrc);


            final PresentationMLPackage pMLpkgTgt = loadPackage(TARGET_FILE_PATH);
            final HashMap<PartName, Part> hmTgt = loadPptxPartMap(pMLpkgTgt);

            CheckPartMapSizesAreEqual(hmSrc, hmTgt);

            log.info("Extracting comment from source file...");
            SlideExtractor srcSlideExtractor = extractComment(hmSrc);

            log.info("Extracting comment from target file...");
            SlideExtractor tgtSlideExtractor = extractComment(hmTgt);

            Map<String, Slide> srcSlides = srcSlideExtractor.getSlides();
            Map<String, Slide> tgtSlides = tgtSlideExtractor.getSlides();

            log.info("Merging source and target slides");
            Map<String, Slide> slidesPerPartName = mergeSlides(TARGET_FILE_PATH, srcSlides, tgtSlides);

            log.info("Updating target...");
            updatePptxStructure(hmTgt, slidesPerPartName);
            log.info("Target updated.");

            log.info("Saving modified target...");

            // All done: save it
            pMLpkgTgt.save(new java.io.File(TARGET_FILE_PATH));

            log.info("done .. saved " + TARGET_FILE_PATH);
        } catch (Docx4JException d4jex) {
            log.error("Exception processing the document. Exiting", d4jex);
            System.exit(-1);
        } catch (CopyCommentException ccex) {
            log.error("Internal error processing the document. Exiting", ccex);
            System.exit(-1);
        }
    }


    private static CommandLine parseCommandLine(String[] args) {
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
        CommandLine cli = null;
        try {
            cli = cliParser.parse(options, args);
        } catch (MissingOptionException mex) {
            log.error(mex.getMessage());
            System.exit(-1);
        } catch (ParseException pex) {
            log.error("Error parsing the command line. Please use CopyComment --help", pex);
            System.exit(-1);
        }
        return cli;
    }

    private static PresentationMLPackage loadPackage(final String srcFilePath) throws Docx4JException {

        log.info("Loading. {}..", srcFilePath);
        PresentationMLPackage pMLpkgSrc =
                (PresentationMLPackage) OpcPackage.load(new java.io.File(srcFilePath));
        log.info("{} loaded", srcFilePath);
        return pMLpkgSrc;

    }

    private static void checkPackageNullElements(PresentationMLPackage pMLpkg) throws CopyCommentException {
        if (pMLpkg == null ||
                pMLpkg.getParts() == null ||
                pMLpkg.getParts().getParts() == null) {
            throw new CopyCommentException("Source document tree has a null element. Exiting");
        }
    }

    private static HashMap<PartName, Part> loadPptxPartMap(PresentationMLPackage pMLpkgSrc) throws Docx4JException, CopyCommentException {
        checkPackageNullElements(pMLpkgSrc);
        return pMLpkgSrc.getParts().getParts();
    }

    private static void CheckPartMapSizesAreEqual(HashMap<PartName, Part> hmSrc, HashMap<PartName, Part> hmTgt) throws CopyCommentException {
        final int srcSize = hmSrc.size();
        final int tgtSize = hmTgt.size();

        if (srcSize != tgtSize) {
            throw new CopyCommentException(String.format("Source document has %s slides and target document %s.", srcSize, tgtSize));
        }
        log.debug("HashMap of documents parts have the same size {} for src, {} for target", srcSize, tgtSize);
    }

    private static SlideExtractor extractComment(HashMap<PartName, Part> hmSrc) throws CopyCommentException, Docx4JException {
        SlideExtractor srcSlideExtractor = new SlideExtractor();
        for (HashMap.Entry<PartName, Part> hmeSrc : hmSrc.entrySet()) {
            srcSlideExtractor.processEntry(hmeSrc);
        }
        return srcSlideExtractor;
    }

    private static Map<String, Slide> mergeSlides(String TARGET_FILE_PATH, Map<String, Slide> srcSlides, Map<String, Slide> tgtSlides) {
        Set<String> processedKeys = new HashSet<>();

        Map<String, Slide> slidesPerPartName = new HashMap<>();
        for(Map.Entry<String, Slide> e : tgtSlides.entrySet()) {
            if(!srcSlides.containsKey(e.getKey())) {
                log.warn("No slide with key {} in the source document. This slide has been ignored.", e.getKey());
            } else {
                Slide srcSlide = srcSlides.get(e.getKey());
                if(srcSlide == null || srcSlide.getParagraphs() == null) {
                    log.error("Slide data are null. Slide {} has been ignored.", e.getKey());
                } else {
                    processedKeys.add(e.getKey());
                    List<String> srcParagraphs = srcSlide.getParagraphs();
                    List<String> tgtParagraphs = e.getValue().getParagraphs();
                    //Preprend source comments (they are the reference)
                    tgtParagraphs.add(0, "=== Original comments from " + TARGET_FILE_PATH + "===");
                    tgtParagraphs.addAll(0, srcParagraphs);
                }
            }
            //Switch key  from first string to partName
            slidesPerPartName.put(e.getValue().getPartName(), e.getValue());
        }

        //Check if some source slides are missing
        for(String s : srcSlides.keySet()) {
            if(!processedKeys.contains(s)) {
                log.warn("No slide with first string {} has been found in the target document. The corresponding comments are not copied", s);
            }
        }
        return slidesPerPartName;
    }


    private static void updatePptxStructure(HashMap<PartName, Part> hmTgt, Map<String, Slide> slidesPerPartName) throws Docx4JException, CopyCommentException {
        for (HashMap.Entry<PartName, Part> hmeTgt : hmTgt.entrySet()) {
            String partName = hmeTgt.getKey().getName();
            if(slidesPerPartName.containsKey(partName)) {
                SlideUpdator.processEntry(hmeTgt.getValue(), slidesPerPartName.get(partName).getParagraphs());
            }
        }
    }

}