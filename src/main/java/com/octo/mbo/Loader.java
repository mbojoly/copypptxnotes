package com.octo.mbo;

import com.octo.mbo.data.*;
import com.octo.mbo.data.extracter.SlideExtractorFactory;
import com.octo.mbo.data.updater.Function;
import com.octo.mbo.data.updater.SlideUpdator;
import com.octo.mbo.exceptions.CopyNotesException;
import com.octo.mbo.io.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;

class Loader {
    private static Logger log = LoggerFactory.getLogger(Loader.class);

    private final XmlPackageFactory xmlPackageFactoryInjected;
    private final Pptx4jPackageFactory pptx4jPackageFactoryInjected;
    private final Util utilInjected;

    Loader(XmlPackageFactory xmlPackageFactory, Pptx4jPackageFactory pptx4jPackageFactory, Util util) {

        xmlPackageFactoryInjected = xmlPackageFactory;
        pptx4jPackageFactoryInjected = pptx4jPackageFactory;
        utilInjected = util;
    }

    XmlUpdatablePackage loadXml(String filePath) throws CopyNotesException {
        log.info("Loading. {}..", filePath);
        Function<InputStream, XmlUpdatablePackage> function = xmlPackageFactoryInjected::loadXml;
        return utilInjected.applyToInputFile(filePath, function);
    }

    Pptx4jUpdatablePackage loadPptx4Update(SlideUpdator slideUpdator, String filePath) throws CopyNotesException {
        return (Pptx4jUpdatablePackage) doloadPptx(slideUpdator, filePath);
    }

    Pptx4jPackage loadPptx4ReadOnly(String filePath) throws CopyNotesException {
        return doloadPptx(null, filePath);
    }

    Pptx4jPackage doloadPptx(SlideUpdator slideUpdator, String filePath) throws CopyNotesException {
        Function<InputStream, Pptx4jPackage> function = is -> {
            SlideExtractorFactory extractorFactory = new SlideExtractorFactory();

            return slideUpdator == null ?
                    pptx4jPackageFactoryInjected.loadPackage(extractorFactory, is) :
                    pptx4jPackageFactoryInjected.loadPackage(slideUpdator, extractorFactory, is);

        };
        log.info("Loading. {}..", filePath);
        return utilInjected.applyToInputFile(filePath, function);
    }
}
