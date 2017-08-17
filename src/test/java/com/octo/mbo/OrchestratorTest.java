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

import com.octo.mbo.data.Pptx4jPackage;
import com.octo.mbo.data.XmlUpdatablePackage;
import com.octo.mbo.exceptions.CopyNotesException;
import com.octo.mbo.exceptions.NotImplementedException;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.File;
import java.nio.file.FileSystem;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class OrchestratorTest {

    @Test
    public void runTestWithNewTargetFileAndXmlSrcFile() throws CopyNotesException, NotImplementedException {
        File existsFileMock = mock(java.io.File.class);
        when(existsFileMock.exists()).thenReturn(true);
        java.nio.file.Path existsPathMock = new OrchestratorPathMock(existsFileMock);

        File notExistsFileMock = mock(java.io.File.class);
        when(notExistsFileMock.exists()).thenReturn(false);

        java.nio.file.Path notExistsPathMock = new OrchestratorPathMock(notExistsFileMock);
        FileSystem fileSystemMock = mock(FileSystem.class);

        when(fileSystemMock.getPath("srcFilePath.xml")).thenReturn(existsPathMock);
        when(fileSystemMock.getPath("targetFilePath.xml")).thenReturn(notExistsPathMock);

        Loader loaderMock = mock(Loader.class);
        XmlUpdatablePackage xmlUpdatablePackageMock = mock(XmlUpdatablePackage.class);
        when(loaderMock.loadXml(any())).thenReturn(xmlUpdatablePackageMock);

        Processor processorMock = mock(Processor.class);

        Orchestrator orchestrator = new Orchestrator(loaderMock, processorMock);

        orchestrator.run("srcFilePath.xml", "targetFilePath.xml", fileSystemMock);

        Mockito.verify(loaderMock).loadXml(any());

        Mockito.verify(processorMock).processWithANewTargetFile(any(), any(), any());
    }

    @Test
    public void runTestWithNewTargetFileAndPptxSrcFile() throws CopyNotesException, NotImplementedException {
        File existsFileMock = mock(java.io.File.class);
        when(existsFileMock.exists()).thenReturn(true);
        java.nio.file.Path existsPathMock = new OrchestratorPathMock(existsFileMock);

        File notExistsFileMock = mock(java.io.File.class);
        when(notExistsFileMock.exists()).thenReturn(false);
        java.nio.file.Path notExistsPathMock = new OrchestratorPathMock(notExistsFileMock);
        FileSystem fileSystemMock = mock(FileSystem.class);

        when(fileSystemMock.getPath("srcFilePath.pptx")).thenReturn(existsPathMock);
        when(fileSystemMock.getPath("targetFilePath.xml")).thenReturn(notExistsPathMock);

        Loader loaderMock = mock(Loader.class);
        Pptx4jPackage pptx4jPackage = mock(Pptx4jPackage.class);
        when(loaderMock.loadPptx4ReadOnly(any())).thenReturn(pptx4jPackage);

        Processor processorMock = mock(Processor.class);

        Orchestrator orchestrator = new Orchestrator(loaderMock, processorMock);

        orchestrator.run("srcFilePath.pptx", "targetFilePath.xml", fileSystemMock);

        Mockito.verify(loaderMock).loadPptx4ReadOnly(any());

        Mockito.verify(processorMock).processWithANewTargetFile(any(), any(), any());
    }

    @Test
    public void runTestWithoutSrcFile() throws CopyNotesException, NotImplementedException {
        File notExistsFileMock = mock(java.io.File.class);
        when(notExistsFileMock.exists()).thenReturn(false);
        java.nio.file.Path notExistsPathMock = new OrchestratorPathMock(notExistsFileMock);

        FileSystem fileSystemMock = mock(FileSystem.class);
        when(fileSystemMock.getPath(anyString())).thenReturn(notExistsPathMock);

        Loader loaderMock = mock(Loader.class);
        Pptx4jPackage pptx4jPackage = mock(Pptx4jPackage.class);
        when(loaderMock.loadPptx4ReadOnly(any())).thenReturn(pptx4jPackage);

        Processor processorMock = mock(Processor.class);

        Orchestrator orchestrator = new Orchestrator(loaderMock, processorMock);

        orchestrator.run("dummyFilePath.pptx", "dummyFilePath.pptx", fileSystemMock);

        Mockito.verifyZeroInteractions(loaderMock);

        Mockito.verifyZeroInteractions(processorMock);
    }


    @Test
    public void getXmlExtensionTest() throws NotImplementedException {
        String path = "TOTO.XmL";
        Orchestrator orchestrator = new Orchestrator(null, null);
        Assert.assertEquals("xml", orchestrator.getExtension(path));

    }

    @Test
    public void getPptxExtensionTest() throws NotImplementedException {
        String path = "TOTO.PpTx";
        Orchestrator orchestrator = new Orchestrator(null, null);
        Assert.assertEquals("pptx", orchestrator.getExtension(path));

    }
}
