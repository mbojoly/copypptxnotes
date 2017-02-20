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


import com.octo.mbo.data.Pptx4jPackageFactory;
import com.octo.mbo.data.XmlPackageFactory;
import com.octo.mbo.data.XmlUpdatablePackage;
import com.octo.mbo.io.Function;
import com.octo.mbo.data.updater.SlideUpdator;
import com.octo.mbo.exceptions.CopyNotesException;
import com.octo.mbo.io.Util;
import org.junit.Test;
import org.mockito.stubbing.Answer;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class LoaderTest {

    @Test
    public void loadXmlTest() throws CopyNotesException {
        Util utilMock = mock(Util.class);
        XmlPackageFactory xmlPackageFactoryMock = mock(XmlPackageFactory.class);

        Loader loader = new Loader(xmlPackageFactoryMock, null, utilMock);

        Answer<XmlUpdatablePackage> answer = invocation -> {
            Function<InputStream, XmlUpdatablePackage> func = invocation.getArgumentAt(1, Function.class);
            return func.apply(new ByteArrayInputStream(new byte[]{}));
        };
        when(utilMock.applyToInputFile(any(), any())).then(answer);

        loader.loadXml("xmlFilePath");

        verify(xmlPackageFactoryMock).loadXml(any());
    }

    @Test
    public void doLoadPptxWithoutSlideUpdatorTest() throws CopyNotesException {

        Util utilMock = mock(Util.class);
        Pptx4jPackageFactory pptx4jPackageFactoryMock = mock(Pptx4jPackageFactory.class);

        Loader loader = new Loader(null, pptx4jPackageFactoryMock, utilMock);

        Answer<XmlUpdatablePackage> answer = invocation -> {
            Function<InputStream, XmlUpdatablePackage> func = invocation.getArgumentAt(1, Function.class);
            return func.apply(new ByteArrayInputStream(new byte[]{}));
        };
        when(utilMock.applyToInputFile(any(), any())).then(answer);

        loader.doloadPptx(null, "filePath");

        verify(pptx4jPackageFactoryMock).loadPackage(any(), any());

    }

    @Test
    public void doLoadPptxWithSlideUpdatorTest() throws CopyNotesException {

        Util utilMock = mock(Util.class);
        Pptx4jPackageFactory pptx4jPackageFactoryMock = mock(Pptx4jPackageFactory.class);
        SlideUpdator slideUpdatorMock = mock(SlideUpdator.class);

        Loader loader = new Loader(null, pptx4jPackageFactoryMock, utilMock);

        Answer<XmlUpdatablePackage> answer = invocation -> {
            Function<InputStream, XmlUpdatablePackage> func = invocation.getArgumentAt(1, Function.class);
            return func.apply(new ByteArrayInputStream(new byte[]{}));
        };
        when(utilMock.applyToInputFile(any(), any())).then(answer);

        loader.doloadPptx(slideUpdatorMock, "filePath");

        verify(pptx4jPackageFactoryMock).loadPackage(any(SlideUpdator.class), any(), any());

    }
}
