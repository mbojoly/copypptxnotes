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


import com.octo.mbo.exceptions.CopyNotesException;

import javax.xml.bind.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

class JAXBMarshaller {
    private JAXBMarshaller() {
        //Prevent to instanciate
    }

    static String marshall(Object jaxbObject) throws CopyNotesException {
        if (jaxbObject == null) {
            throw new CopyNotesException("The argument is null");
        }

        try {

            JAXBContext context = JAXBContext.newInstance(
                    jaxbObject.getClass());
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT,
                    Boolean.TRUE);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            marshaller.marshal(jaxbObject, baos);
            return baos.toString();
        } catch (JAXBException jaxbe) {
            throw new CopyNotesException("Error marshalling the object", jaxbe);
        }
    }

    static Object unmarshall(Class targetClass, String xml) throws CopyNotesException {
        if (targetClass == null || xml == null) {
            throw new CopyNotesException("One of the argument is null");
        }

        try {
            JAXBContext context = JAXBContext.newInstance(
                    targetClass);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            unmarshaller.setEventHandler(new javax.xml.bind.helpers.DefaultValidationEventHandler());
            ByteArrayInputStream bais = new ByteArrayInputStream(xml.getBytes());
            return unmarshaller.unmarshal(bais);
        } catch (JAXBException jaxbe) {
            throw new CopyNotesException("Error marshalling the object", jaxbe);
        }
    }
}
