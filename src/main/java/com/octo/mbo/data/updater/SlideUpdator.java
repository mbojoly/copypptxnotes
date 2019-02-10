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
package com.octo.mbo.data.updater;


import com.octo.mbo.data.extracter.SlideExtractor;
import com.octo.mbo.exceptions.CopyNotesException;
import org.docx4j.dml.*;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.PresentationML.SlidePart;
import org.pptx4j.pml.GroupShape;
import org.pptx4j.pml.Notes;
import org.pptx4j.pml.Shape;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class SlideUpdator {

    private static Logger log = LoggerFactory.getLogger(SlideUpdator.class);
    private final SlideUpdator dependencyInjection;

    public SlideUpdator() {
        dependencyInjection = this;
    }

    /**
     * For test purpose only
     * @param dependencyInjection All call to methods of this class will reference this injected instance that
     *                            can be mocked
     */
    SlideUpdator(SlideUpdator dependencyInjection) {
        this.dependencyInjection = dependencyInjection;
    }

    public void processEntry(Part part, List<String> newParagraphs) throws CopyNotesException {
        if (newParagraphs == null) {
            throw new CopyNotesException("List of newParagraphs to update is null");
        }
        try {
            if (part != null && part instanceof SlidePart) {
                SlidePart slidePart = (SlidePart) part;
                if (slidePart.getNotesSlidePart() == null || slidePart.getNotesSlidePart().getContents() == null) {
                    throw new CopyNotesException("Processing of documents without a valid SPTree of NoteSlidePart is not yet implemented. Look at Slide " + slidePart.getPartName() + " or previous one and put a dummy notes to correct the problem.");
                }
                Notes notesSrc = slidePart.getNotesSlidePart().getContents();
                if (notesSrc == null || notesSrc.getCSld() == null || notesSrc.getCSld().getSpTree() == null) {
                    throw new CopyNotesException("Processing of documents without a valid SPTree of NoteSlidePart is not yet implemented");
                }
                GroupShape shapeNotesTgt = notesSrc.getCSld().getSpTree();
                dependencyInjection.updateSlide(shapeNotesTgt, newParagraphs);
            }
        } catch (Docx4JException docx4jex) {
            throw new CopyNotesException("Unable to get slide contents", docx4jex);
        }
    }

    void updateSlide(GroupShape shapeNotesSrc, List<String> newParagraphs) throws CopyNotesException {
        if (shapeNotesSrc == null) {
            throw new CopyNotesException("ShapeNotesSrc is null");
        }
        if (newParagraphs == null) {
            throw new CopyNotesException("List of newParagraphs to update is null");
        }

        for (Object o : shapeNotesSrc.getSpOrGrpSpOrGraphicFrame()) {
            if (o != null && o instanceof Shape) {
                CTTextBody txBody = ((Shape) o).getTxBody();
                if (txBody != null) {
                    List<CTTextParagraph> paragraphs = txBody.getP();
                    //New paragraphs must have these properties. They are copied from the first one
                    CTTextParagraph referenceCTTextParagraph = paragraphs.get(0);
                    //Clear and replace, merge of previous and new comment is done by CopyComment.mergeSlides()
                    paragraphs.clear();
                    paragraphs.addAll(
                            dependencyInjection.createNewCTTextParagraphs(newParagraphs, referenceCTTextParagraph)
                    );
                }
            }
        }
    }

    List<CTTextParagraph> createNewCTTextParagraphs(final List<String> newParagraphs, final CTTextParagraph referenceParagraph) {
        List<CTTextParagraph> result = new ArrayList<>();

        CTTextParagraphProperties paragraphProperties = referenceParagraph.getPPr();
        CTTextCharacterProperties characterProperties = referenceParagraph.getEndParaRPr();
        for (String p : newParagraphs) {
            CTTextParagraph newParagraph = new CTTextParagraph();
            newParagraph.setPPr(paragraphProperties);
            newParagraph.setEndParaRPr(characterProperties);
            CTRegularTextRun tr = new CTRegularTextRun();
            tr.setT(p);
            newParagraph.getEGTextRun().add(tr);
            result.add(newParagraph);
        }
        return result;
    }


}
