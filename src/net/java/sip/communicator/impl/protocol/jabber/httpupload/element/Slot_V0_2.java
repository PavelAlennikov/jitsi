package net.java.sip.communicator.impl.protocol.jabber.httpupload.element;

import net.java.sip.communicator.impl.protocol.jabber.httpupload.*;

import java.net.*;

public class Slot_V0_2 extends Slot {

    public static final String NAMESPACE = HttpFileUploadManager.NAMESPACE_0_2;

    public Slot_V0_2(URL putUrl, URL getUrl) {
        super(putUrl, getUrl, null, NAMESPACE);
    }

    @Override
    public String getChildElementXML() {
        XmlStringBuilder xml = new XmlStringBuilder();

        xml.element("put", putUrl.toString());
        xml.element("get", getUrl.toString());

        return xml.toString();
    }
}