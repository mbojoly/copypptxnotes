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
    docx4j is licensed under the Apache License, Version 2.0 (the "License"); 
    you may not use this file except in compliance with the License. 
    You may obtain a copy of the License at 
        http://www.apache.org/licenses/LICENSE-2.0 
    Unless required by applicable law or agreed to in writing, software 
    distributed under the License is distributed on an "AS IS" BASIS, 
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
    See the License for the specific language governing permissions and 
    limitations under the License.
 */


import org.docx4j.XmlUtils;
import org.docx4j.openpackaging.contenttype.ContentTypeManager;
import org.docx4j.openpackaging.contenttype.ContentTypes;
import org.docx4j.openpackaging.packages.OpcPackage;
import org.docx4j.openpackaging.packages.PresentationMLPackage;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.PresentationML.MainPresentationPart;
import org.docx4j.openpackaging.parts.PresentationML.SlideLayoutPart;
import org.docx4j.openpackaging.parts.PresentationML.SlidePart;
import org.docx4j.samples.AbstractSample;
import org.pptx4j.jaxb.Context;
import org.pptx4j.pml.Notes;
import org.pptx4j.pml.Shape;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;


/**
 * @author jharrop
 *
 */
public class CopyComment extends AbstractSample {

    protected static Logger log = LoggerFactory.getLogger(CopyComment.class);

    private static boolean MACRO_ENABLE = false;

    public static void main(String[] args) throws Exception {

        /*
		 * You can invoke this from an OS command line with something like:
		 *
		 * java -cp dist/docx4j.jar:dist/log4j-1.2.15.jar
		 * org.docx4j.samples.OpenMainDocumentAndTraverse inputdocx
		 *
		 * Note the minimal set of supporting jars.
		 *
		 * If there are any images in the document, you will also need:
		 *
		 * dist/xmlgraphics-commons-1.4.jar:dist/commons-logging-1.1.1.jar
		 */

        try {
            getInputFilePath(args);
        } catch (IllegalArgumentException e) {
            inputfilepath = System.getProperty("user.dir")
                    + "/sample-docs/pptx-test.pptx";
        }

        String outputfilepath = System.getProperty("user.dir") + "/sample-docs/pptx-copy.pptx";

        PresentationMLPackage presentationMLPackage =
                (PresentationMLPackage) OpcPackage.load(new java.io.File(inputfilepath));


        SlidePart slide = (SlidePart)presentationMLPackage.getParts().get(new PartName("/ppt/slides/slide1.xml") );
        Notes notes = slide.getNotesSlidePart().getContents();

        //See https://github.com/plutext/docx4j/blob/master/src/samples/pptx4j/org/pptx4j/samples/SlideNotes.java

        // All done: save it
        presentationMLPackage.save(new java.io.File(outputfilepath));

        System.out.println("\n\n done .. saved " + outputfilepath);

    }





}