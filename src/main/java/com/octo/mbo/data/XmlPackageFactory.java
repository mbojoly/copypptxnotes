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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * To allow mocks
 */
public class XmlPackageFactory {
    public XmlUpdatablePackage loadXml(final InputStream inputStream) throws CopyNotesException {
        if (inputStream == null) {
            throw new CopyNotesException("Can not load from a null InputStream");
        }

        StringBuilder xml = new StringBuilder();
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                xml.append(line);
                xml.append(System.lineSeparator());
            }
        } catch (IOException ioex) {
            throw new CopyNotesException("Unable to row read lines from XML InputStream", ioex);
        }

        XmlUpdatablePackage xmlUpdatablePackage = new XmlUpdatablePackage(new SlideDocument());
        xmlUpdatablePackage.slideDocument = (SlideDocument) JAXBMarshaller.unmarshall(SlideDocument.class, xml.toString());
        return xmlUpdatablePackage;
    }

    public XmlUpdatablePackage buildFromSlides(final Collection<Slide> slides) {
        List<Slide> slidesSorttedByPartName
                = slides
                .stream()
                .sorted((slide1, slide2) -> {
                    //PLEASE NOTE: there is an implementation to sort Map<String, Slide> by part name (key is part name)
                    if (slide1 == null || slide2 == null) {
                        //slide not null > slide null
                        //if slide1 and slide2 == nul ==> slide1 > slide2 (convention)
                        if (slide2 == null) {
                            return 1;
                        } else {
                            return -1;
                        }
                    } else {
                        return Comparator.comparing(Slide::getPartName).compare(slide1, slide2);
                    }
                })
                .collect(Collectors.toList());
        XmlUpdatablePackage xmlUpdatablePackage = new XmlUpdatablePackage(new SlideDocument());
        xmlUpdatablePackage.slideDocument.getSlides().addAll(slidesSorttedByPartName);
        return xmlUpdatablePackage;
    }
}
