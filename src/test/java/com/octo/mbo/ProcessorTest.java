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
package com.octo.mbo;

import com.octo.mbo.data.Pptx4jUpdatablePackage;
import com.octo.mbo.data.XmlPackageFactory;
import com.octo.mbo.data.XmlUpdatablePackage;
import com.octo.mbo.io.Function;
import com.octo.mbo.domain.Merger;
import com.octo.mbo.exceptions.CopyNotesException;
import com.octo.mbo.io.Util;
import org.junit.Test;
import org.mockito.stubbing.Answer;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.HashMap;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class ProcessorTest {

    @Test
    public void processWithANewTargetFileTest() throws CopyNotesException {
        XmlPackageFactory xmlPackageFactoryMock = mock(XmlPackageFactory.class);
        XmlUpdatablePackage xmlUpdatablePackageMock = mock(XmlUpdatablePackage.class);
        Util utilMock = mock(Util.class);

        when(xmlPackageFactoryMock.buildFromSlides(any())).thenReturn(xmlUpdatablePackageMock);
        Answer<Void> answer = invocation -> {
            Function<OutputStream, Object> func = invocation.getArgumentAt(1, Function.class);
            func.apply(new ByteArrayOutputStream());
            return null;
        };
        when(utilMock.applyToOutputFile(any(), any())).then(answer);

        Processor processor = new Processor(null, null, null, xmlPackageFactoryMock, utilMock);
        processor.processWithANewTargetFile("toto.xml", "xml", new HashMap<>());

        verify(xmlPackageFactoryMock).buildFromSlides(any());
        verify(xmlUpdatablePackageMock).saveChanges(any());
    }

    @Test(expected = CopyNotesException.class)
    public void processWithANewTargetPptxFileFailTest() throws CopyNotesException {
        XmlPackageFactory xmlPackageFactoryMock = mock(XmlPackageFactory.class);
        XmlUpdatablePackage xmlUpdatablePackageMock = mock(XmlUpdatablePackage.class);
        Util utilMock = mock(Util.class);

        when(xmlPackageFactoryMock.buildFromSlides(any())).thenReturn(xmlUpdatablePackageMock);
        Answer<Void> answer = invocation -> {
            Function<OutputStream, Object> func = invocation.getArgumentAt(1, Function.class);
            func.apply(new ByteArrayOutputStream());
            return null;
        };
        when(utilMock.applyToOutputFile(any(), any())).then(answer);

        Processor processor = new Processor(null, null, null, xmlPackageFactoryMock, utilMock);
        processor.processWithANewTargetFile("toto.pptx", "pptx", new HashMap<>());

    }

    @Test
    public void processWithAnExistingXmlFileTest() throws CopyNotesException {
        XmlPackageFactory xmlPackageFactoryMock = mock(XmlPackageFactory.class);
        XmlUpdatablePackage xmlUpdatablePackageMock = mock(XmlUpdatablePackage.class);
        Util utilMock = mock(Util.class);
        Loader loaderMock = mock(Loader.class);
        Merger mergerMock = mock(Merger.class);

        when(xmlPackageFactoryMock.buildFromSlides(any())).thenReturn(xmlUpdatablePackageMock);
        Answer<Void> answer = invocation -> {
            Function<OutputStream, Object> func = invocation.getArgumentAt(1, Function.class);
            func.apply(new ByteArrayOutputStream());
            return null;
        };
        when(utilMock.applyToOutputFile(any(), any())).then(answer);
        when(loaderMock.loadXml(any())).thenReturn(xmlUpdatablePackageMock);

        Processor processor = new Processor(mergerMock, loaderMock, null, xmlPackageFactoryMock, utilMock);
        processor.processWithExistingTargetFile("toto.xml", "xml", new HashMap<>());

        verify(xmlUpdatablePackageMock).updateStructure(any());
        verify(xmlUpdatablePackageMock).saveChanges(any());
    }

    @Test
    public void processWithAnExistingPptxFileTest() throws CopyNotesException {
        XmlPackageFactory xmlPackageFactoryMock = mock(XmlPackageFactory.class);
        XmlUpdatablePackage xmlUpdatablePackageMock = mock(XmlUpdatablePackage.class);
        Util utilMock = mock(Util.class);
        Loader loaderMock = mock(Loader.class);
        Pptx4jUpdatablePackage updatablePackageMock = mock(Pptx4jUpdatablePackage.class);
        Merger mergerMock = mock(Merger.class);

        when(xmlPackageFactoryMock.loadXml(any())).thenReturn(xmlUpdatablePackageMock);
        Answer<Void> answer = invocation -> {
            Function<OutputStream, Object> func = invocation.getArgumentAt(1, Function.class);
            func.apply(new ByteArrayOutputStream());
            return null;
        };
        when(utilMock.applyToOutputFile(any(), any())).then(answer);
        when(loaderMock.loadPptx4Update(any(), any())).thenReturn(updatablePackageMock);

        Processor processor = new Processor(mergerMock, loaderMock, null, xmlPackageFactoryMock, utilMock);
        processor.processWithExistingTargetFile("toto.pptx", "pptx", new HashMap<>());

        verify(updatablePackageMock).saveChanges(any());
    }
}
