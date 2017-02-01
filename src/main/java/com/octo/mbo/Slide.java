package com.octo.mbo;


import java.util.ArrayList;
import java.util.List;

public class Slide {

    /**
     * Value of PartName extracted from the tree
     */
    private String partName;
    /**
     * This is the first string found in the slide. It is used as a key
     */
    private String title;
    private List<String> paragraphs = new ArrayList<>();


    public Slide(String partName, String title, List<String> paragraphs) {
        this.partName = partName;
        this.title = title;
        this.paragraphs = paragraphs;
    }

    public String getPartName() {
        return partName;
    }

    public String getTitle() {
        return title;
    }

    public List<String> getParagraphs() {
        return paragraphs;
    }
}
