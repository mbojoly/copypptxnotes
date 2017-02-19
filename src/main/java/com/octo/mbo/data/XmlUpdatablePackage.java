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
package com.octo.mbo.data;


import com.octo.mbo.domain.Slide;
import com.octo.mbo.domain.SlideDocument;
import com.octo.mbo.exceptions.CopyNotesException;

import java.io.*;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class XmlUpdatablePackage implements UpdatablePackage {

    SlideDocument slideDocument;
    

    /**
     * For testing only
     *
     * @param slideDocument document to inject
     */
    XmlUpdatablePackage(final SlideDocument slideDocument) {
        this.slideDocument = slideDocument;
    }

    public void saveChanges(final OutputStream outputStream) throws CopyNotesException {
        if (outputStream == null) {
            throw new CopyNotesException("Can not save changes to an OutputStream");
        }

        String xml = JAXBMarshaller.marshall(this.slideDocument);

        try {
            outputStream.write(xml.getBytes());
        } catch (IOException ioex) {
            throw new CopyNotesException("Unable to save raw lines of XML to OutputStream", ioex);
        }
    }

    /**
     * Key: First string in the slide
     * Value: Slide object
     *
     * @return Map
     */
    public Map<String, Slide> getSlides() {
        return this.slideDocument
                .getSlides()
                .stream()
                .collect(Collectors.toMap(Slide::getTitle, Function.identity()));
    }

    @Override
    public void updateStructure(Map<String, Slide> slidesPerPartName) throws CopyNotesException {
        //Assert that the slid
        slideDocument.getSlides().clear();
        //PLEASE NOTE: there is an implementation to sort a List<Slide> by partName
        SortedMap<String, Slide> sortedMap = new TreeMap<>(slidesPerPartName);
        slideDocument.getSlides().addAll(sortedMap.values());
    }

}
