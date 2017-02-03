package com.octo.mbo;

import org.apache.commons.cli.*;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.OpcPackage;
import org.docx4j.openpackaging.packages.PresentationMLPackage;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.PartName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;


public class CopyComment {

    private static Logger log = LoggerFactory.getLogger(CopyComment.class);

    private final SlideUpdator slideUpdatorInjected;
    private SlideExtractor srcSlideExtractorInjected;

    CopyComment() {
        slideUpdatorInjected = new SlideUpdator();
        srcSlideExtractorInjected = new SlideExtractor();
    }

    /**
     * For testing only
     * @param slideUpdatorInjected dependencyInjection
     * @param srcSlideExtractorInjected dependencyInjection
     */
    CopyComment(SlideUpdator slideUpdatorInjected, SlideExtractor srcSlideExtractorInjected) {
        this.slideUpdatorInjected = slideUpdatorInjected;
        this.srcSlideExtractorInjected = srcSlideExtractorInjected;
    }


    public static void main(String[] args) throws JAXBException, IOException {
        CopyComment copyComment = new CopyComment();

        try {
            CommandLine cli = parseCommandLine(args);

            final String srcFilePath = cli.getOptionValue("s");
            final String targetFilePath = cli.getOptionValue("t");


            final PresentationMLPackage pMLpkgSrc = loadPackage(srcFilePath);
            final Map<PartName, Part> hmSrc = loadPptxPartMap(pMLpkgSrc);

            log.info("Extracting comment from source file...");
            SlideExtractor srcSlideExtractor = copyComment.extractComment(hmSrc);

            final PresentationMLPackage pMLpkgTgt = loadPackage(targetFilePath);
            final Map<PartName, Part> hmTgt = loadPptxPartMap(pMLpkgTgt);

            Map<String, Slide> srcSlides = srcSlideExtractor.getSlides();

            log.info("Extracting comment from target file...");
            SlideExtractor tgtSlideExtractor = copyComment.extractComment(hmTgt);

            Map<String, Slide> tgtSlides = tgtSlideExtractor.getSlides();

            checkMapSizesAreEqual(srcSlides, tgtSlides);

            log.info("Merging source and target slides");
            Map<String, Slide> slidesPerPartName = copyComment.mergeSlides(targetFilePath, srcSlides, tgtSlides);

            SlideDocument slideDocument = new SlideDocument();
            slideDocument.getSlides().addAll(slidesPerPartName.values());
            String xml = JAXBMarshaller.marshall(slideDocument);
            Files.write(Paths.get(targetFilePath + ".xml"), xml.getBytes());

            log.info("Updating target...");
            copyComment.updatePptxStructure(hmTgt, slidesPerPartName);
            log.info("Target updated.");

            log.info("Saving modified target...");

            // All done: save it
            pMLpkgTgt.save(new java.io.File(targetFilePath));

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

    private static PresentationMLPackage loadPackage(final String srcFilePath) throws Docx4JException {

        log.info("Loading. {}..", srcFilePath);
        PresentationMLPackage pMLpkgSrc =
                (PresentationMLPackage) OpcPackage.load(new java.io.File(srcFilePath));
        log.info("{} loaded", srcFilePath);
        return pMLpkgSrc;

    }

    /**
     * In the 3.3.1 version of docx4j this exception can never be thrown (but it is not documented)
     *
     * @param pMLpkg Package
     * @throws CopyCommentException If elements of package are null
     */
    private static void checkPackageNullElements(PresentationMLPackage pMLpkg) throws CopyCommentException {
        if (pMLpkg == null ||
                pMLpkg.getParts() == null ||
                pMLpkg.getParts().getParts() == null) {
            throw new CopyCommentException("Source document tree has a null element. Exiting");
        }
    }

    static Map<PartName, Part> loadPptxPartMap(PresentationMLPackage pMLpkgSrc) throws Docx4JException, CopyCommentException {
        checkPackageNullElements(pMLpkgSrc);
        return pMLpkgSrc.getParts().getParts();
    }

    static void checkMapSizesAreEqual(Map<String, Slide> hmSrc, Map<String, Slide> hmTgt) throws CopyCommentException {
        final int srcSize = hmSrc.size();
        final int tgtSize = hmTgt.size();

        if (srcSize != tgtSize) {
            throw new CopyCommentException(String.format("Source document has %s slides and target document %s.", srcSize, tgtSize));
        }
        log.debug("Map of documents parts have the same size {} for src, {} for target", srcSize, tgtSize);
    }

    SlideExtractor extractComment(Map<PartName, Part> hmSrc) throws CopyCommentException, Docx4JException {
        for (Map.Entry<PartName, Part> hmeSrc : hmSrc.entrySet()) {
            srcSlideExtractorInjected.processEntry(hmeSrc);
        }
        return srcSlideExtractorInjected;
    }

    Map<String, Slide> mergeSlides(String targetFilePath, Map<String, Slide> srcSlides, Map<String, Slide> tgtSlides) {
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
                    tgtParagraphs.add(0, "=== Original comments from " + targetFilePath + "===");
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

    void updatePptxStructure(Map<PartName, Part> hmTgt, Map<String, Slide> slidesPerPartName) throws Docx4JException, CopyCommentException {
        for (HashMap.Entry<PartName, Part> hmeTgt : hmTgt.entrySet()) {
            String partName = hmeTgt.getKey().getName();
            if(slidesPerPartName.containsKey(partName)) {
                slideUpdatorInjected.processEntry(hmeTgt.getValue(), slidesPerPartName.get(partName).getParagraphs());
            }
        }
    }

}