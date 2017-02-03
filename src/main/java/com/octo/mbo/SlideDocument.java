package com.octo.mbo;

import com.octo.mbo.Slide;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name="slides")
public class SlideDocument {
    private final List<Slide> slides = new ArrayList<>();

    public SlideDocument() {
        //Required for JAXB
    }

    public SlideDocument(List<Slide> slides) {
        this.slides.addAll(slides);
    }

    @XmlElement(name="slide")
    public List<Slide> getSlides() {
        return slides;
    }
 }
