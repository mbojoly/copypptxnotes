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

import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;

import org.docx4j.dml.CTRegularTextRun;
import org.docx4j.dml.CTTextBody;
import org.docx4j.dml.CTTextParagraph;
import org.docx4j.openpackaging.packages.OpcPackage;
import org.docx4j.openpackaging.packages.PresentationMLPackage;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.PresentationML.SlidePart;
import org.docx4j.samples.AbstractSample;
import org.pptx4j.pml.Notes;
import org.pptx4j.pml.Shape;


/**
 * This sample is useful if you want to see what objects are used in your document.xml.
 * <p>
 * This shows a general approach for traversing the JAXB object tree in
 * the Main Document part.  It can also be applied to headers, footers etc.
 * <p>
 * It is an alternative to XSLT, and doesn't require marshalling/unmarshalling.
 * <p>
 * If many cases, the method getJAXBNodesViaXPath
 * may be more convenient.
 *
 * @author jharrop
 */
public class TraverseSlide extends AbstractSample {

    /**
     * @param args Arguments
     */
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

        PresentationMLPackage pMLPackage =
                (PresentationMLPackage) OpcPackage.load(new java.io.File(inputfilepath));

        doOnEachComment(pMLPackage, TraverseSlide::print);
    }

    public static void print(final String toBePrint) {
        System.out.println(toBePrint);
    }

    public static void doOnEachComment(PresentationMLPackage pMLPackage, Consumer<String> function) throws org.docx4j.openpackaging.exceptions.Docx4JException {
        for (HashMap.Entry<PartName, Part> hme : pMLPackage.getParts().getParts().entrySet()) {
            final PartName partName = hme.getKey();

            if (hme.getValue() instanceof SlidePart) {
                SlidePart slide = (SlidePart) hme.getValue();
                Notes notes = slide.getNotesSlidePart().getContents();

                for (Object o : notes.getCSld().getSpTree().getSpOrGrpSpOrGraphicFrame()) {
                    if (o instanceof Shape) {
                        CTTextBody txBody = ((Shape) o).getTxBody();
                        if (txBody != null) {
                            for (CTTextParagraph tp : txBody.getP()) {
                                if (tp != null) {
                                    List<Object> textRuns = tp.getEGTextRun();
                                    for (Object otr : textRuns) {
                                        if (otr instanceof CTRegularTextRun) {
                                            CTRegularTextRun tr = (CTRegularTextRun) otr;
                                            function.accept(tr.getT());
                                        }
                                    }
                                }
                            }
                        }
                    }

                }
            }
        }
    }

}