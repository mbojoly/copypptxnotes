package com.octo.mbo;


import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("ALL")
@XmlRootElement
@XmlType(propOrder = { "title", "partName", "paragraphs"})
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

    public Slide() {
        //Required for JAXB
    }

    public Slide(String partName, String title, List<String> paragraphs) {
        this.partName = partName;
        this.title = title;
        this.paragraphs = paragraphs;
    }

    @XmlElement(name="partName")
    public String getPartName() {
        return partName;
    }

    @XmlElement(name="title")
    public String getTitle() {
        return title;
    }

    @XmlElementWrapper(name="notes")
    @XmlElement(name="p")
    public List<String> getParagraphs() {
        return paragraphs;
    }

    //Required for JAXB
    public void setPartName(String partName) {
        this.partName = partName;
    }

    //Required for JAXB
    public void setTitle(String title) {
        this.title = title;
    }
}
