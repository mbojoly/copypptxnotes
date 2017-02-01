package com.octo.mbo;


import org.docx4j.dml.*;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.PresentationML.SlidePart;
import org.pptx4j.pml.GroupShape;
import org.pptx4j.pml.Notes;
import org.pptx4j.pml.Shape;

import java.util.List;

class SlideUpdator {

    static void processEntry(Part part, List<String> newParagraphs) throws Docx4JException, CopyCommentException {
        if(newParagraphs == null) {
            throw new CopyCommentException("List of newParagraphs to update is null");
        }
        if(part != null && part instanceof SlidePart) {
            SlidePart slidePart = (SlidePart) part;
            if (slidePart.getNotesSlidePart() == null || slidePart.getNotesSlidePart().getContents() == null) {
                throw new CopyCommentException("Processing of documents without an existing NoteSlidePart with content is not yet implemented");
            }
            Notes notesSrc = slidePart.getNotesSlidePart().getContents();
            if (notesSrc == null || notesSrc.getCSld() == null || notesSrc.getCSld().getSpTree() == null) {
                throw new CopyCommentException("Processing of documents without a valide SPTree of NoteSlidePart is not yet implemented");
            }
            GroupShape shapeNotesTgt = notesSrc.getCSld().getSpTree();
            updateSlide(shapeNotesTgt, newParagraphs);
        }
    }

    static void updateSlide(GroupShape shapeNotesSrc, List<String> newParagraphs) throws CopyCommentException {
        if(shapeNotesSrc == null) {
            throw new CopyCommentException("ShapeNotesSrc is null");
        }
        if(newParagraphs == null) {
            throw new CopyCommentException("List of newParagraphs to update is null");
        }

        for (Object o : shapeNotesSrc.getSpOrGrpSpOrGraphicFrame()) {
            if (o != null && o instanceof Shape) {
                CTTextBody txBody = ((Shape) o).getTxBody();
                if (txBody != null) {
                    List<CTTextParagraph> paragraphs = txBody.getP();
                    //New paragraphs must have these properties. They are copied from the first one
                    CTTextParagraph referenceParagraph = paragraphs.get(0);
                    CTTextParagraphProperties paragraphProperties = referenceParagraph.getPPr();
                    CTTextCharacterProperties characterProperties = referenceParagraph.getEndParaRPr();
                    //Clear and replace
                    paragraphs.clear();
                    for(String p : newParagraphs) {
                        CTTextParagraph newParagraph = new CTTextParagraph();
                        newParagraph.setPPr(paragraphProperties);
                        newParagraph.setEndParaRPr(characterProperties);
                        CTRegularTextRun tr = new CTRegularTextRun();
                        tr.setT(p);
                        newParagraph.getEGTextRun().add(tr);
                        paragraphs.add(newParagraph);
                    }
                }
            }
        }
    }
}
