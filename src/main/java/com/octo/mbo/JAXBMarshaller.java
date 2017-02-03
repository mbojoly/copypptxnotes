package com.octo.mbo;


import javax.xml.bind.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

class JAXBMarshaller {
    private JAXBMarshaller() {
        //Prevent to instanciate
    }

    static String marshall( Object jaxbObject ) throws CopyCommentException {
        if(jaxbObject == null) {
            throw new CopyCommentException("The argument is null");
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
        }
        catch(JAXBException jaxbe) {
            throw new CopyCommentException("Error marshalling the object", jaxbe);
        }
    }

    static Object unmarshall(Class targetClass, String xml) throws CopyCommentException {
        if(targetClass == null || xml == null) {
            throw new CopyCommentException("One of the argument is null");
        }

        try {
            JAXBContext context = JAXBContext.newInstance(
                    targetClass);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            unmarshaller.setEventHandler(new javax.xml.bind.helpers.DefaultValidationEventHandler());
            ByteArrayInputStream bais = new ByteArrayInputStream(xml.getBytes());
            return unmarshaller.unmarshal(bais);
        }
        catch(JAXBException jaxbe) {
            throw new CopyCommentException("Error marshalling the object", jaxbe);
        }
    }
}
