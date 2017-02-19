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
import com.octo.mbo.exceptions.CopyNotesException;

import java.io.OutputStream;
import java.util.Map;

/**
 * Allow to have one common behaviour for every format used to save the document
 */
public interface UpdatablePackage {
    void updateStructure(Map<String, Slide> slidesPerPartName) throws CopyNotesException;

    void saveChanges(OutputStream outputStream) throws CopyNotesException;

    Map<String, Slide> getSlides() throws CopyNotesException;
}
