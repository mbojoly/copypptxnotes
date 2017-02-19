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

import com.octo.mbo.data.Pptx4jPackageFactory;
import com.octo.mbo.data.XmlPackageFactory;
import com.octo.mbo.data.updater.SlideUpdator;
import com.octo.mbo.domain.Merger;
import com.octo.mbo.exceptions.CommandLineException;
import com.octo.mbo.exceptions.CopyNotesException;
import com.octo.mbo.io.Util;
import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBException;
import java.io.IOException;


public class CopyNotes {

    private static Logger log = LoggerFactory.getLogger(CopyNotes.class);

    private CopyNotes() {
        //Prevent instanciating
    }

    public static void main(String[] args) throws JAXBException, IOException {
        try {
            CommandLine cli = parseCommandLine(args);

            final String srcFilePath = cli.getOptionValue("s");
            final String targetFilePath = cli.getOptionValue("t");

            //Dependency injection
            com.octo.mbo.io.Util util = new Util();
            Loader loader = new Loader(new XmlPackageFactory(), new Pptx4jPackageFactory(), util);
            Processor processor = new Processor(
                    new Merger(),
                    loader,
                    new SlideUpdator(),
                    new XmlPackageFactory(),
                    util
            );
            Orchestrator orchestrator = new Orchestrator(loader, processor);
            orchestrator.run(srcFilePath, targetFilePath);

        } catch (CommandLineException clex) {
            log.debug("Error parsing the command line. Please use CopyComment --help", clex);
            //Error messages are already printed by the parseCommandeLine() method
            log.error(clex.getMessage());
            System.exit(-1);
        } catch (CopyNotesException ccex) {
            log.error("Internal error processing the document. Exiting", ccex);
            System.exit(-1);
        }
    }

    static CommandLine parseCommandLine(String[] args) throws CommandLineException {
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

        HelpFormatter formatter = new HelpFormatter();
        final String header = "CopyNotes allows to extract, copy and merge notes of pptx files. \n" +
                "Notes can be merged with the notes of an existing files \n" +
                "Notes can be exported to a xml file with a custom format or imported \n" +
                "from this xml format and merged into an existing pptx document. \n" +
                "The target file is updated \n";
        final String footer = "";


        CommandLineParser cliParser = new DefaultParser();
        try {
            return cliParser.parse(options, args);
        } catch (MissingOptionException mex) {
            log.error(mex.getMessage());
            log.debug("Error parsing command line", mex);
            formatter.printHelp("java -jar copypptxnotes-<version>-jar-with-dependencies.jar -s <source> -t <target>",
                    header, options, footer);
            throw new CommandLineException("Missing option", mex);
        } catch (ParseException pex) {
            log.debug("Error parsing the command line. Please use CopyComment --help", pex);
            formatter.printHelp("java -jar copypptxnotes-<version>-jar-with-dependencies.jar  -s <source> -t <target>",
                    header, options, footer);
            throw new CommandLineException("Parse Exception", pex);
        }
    }

}