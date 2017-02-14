package com.octo.mbo.xml;

import com.octo.mbo.exceptions.CopyCommentException;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.OpcPackage;
import org.docx4j.openpackaging.packages.PresentationMLPackage;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.PartName;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

public class Pptx4jPackage {

    PresentationMLPackage pMLpkg;

    /**
     * Only for test purpose
     */
    Pptx4jPackage(final PresentationMLPackage pMLpkg) {
        this.pMLpkg = pMLpkg;
    }

    private Pptx4jPackage() {
        //Prevent instanciating
    }

    public static Pptx4jPackage loadPackage(final InputStream inputStream) throws CopyCommentException {
        Pptx4jPackage instance = new Pptx4jPackage();
        if(inputStream == null) {
            throw new CopyCommentException("Can not load from a null InputStream");
        }
        try {
            instance.pMLpkg =
                    (PresentationMLPackage) OpcPackage.load(inputStream);
            return instance;
        } catch (Docx4JException docx4jEx) {
            throw new CopyCommentException("Unable to load from the InputStream", docx4jEx);
        }
    }

    public Map<PartName, Part> loadPptxPartMap() throws CopyCommentException {
        checkPackageNullElements();
        return this.pMLpkg.getParts().getParts();
    }

    public void saveChanges(final OutputStream outputStream) throws CopyCommentException {
        if(outputStream == null) {
            throw new CopyCommentException("Can not save on null outputStream");
        }
        try {
            this.pMLpkg.save(outputStream);
        } catch (Docx4JException docx4jEx) {
            throw new CopyCommentException("Unable to save package", docx4jEx);
        }
    }

    /**
     * In the 3.3.1 version of docx4j this exception can never be thrown (but it is not documented)
     *
     * @throws CopyCommentException If elements of package are null
     */
    private void checkPackageNullElements() throws CopyCommentException {
        if (this.pMLpkg.getParts() == null ||
                this.pMLpkg.getParts().getParts() == null) {
            throw new CopyCommentException("Source document tree has a null element. Exiting");
        }
    }
}
