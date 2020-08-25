package net.java.sip.communicator.impl.protocol.jabber.httpupload.element;

import net.java.sip.communicator.impl.protocol.jabber.httpupload.*;

public class SlotRequest_V0_2 extends SlotRequest {

    public static final String NAMESPACE = HttpFileUploadManager.NAMESPACE_0_2;

    public SlotRequest_V0_2(String uploadServiceAddress, String filename, long size) {
        this(uploadServiceAddress, filename, size, null);
    }

    /**
     * Create new slot request.
     *
     * @param uploadServiceAddress the XMPP address of the service to request the slot from.
     * @param filename name of file
     * @param size size of file in bytes
     * @param contentType file content type or null
     * @throws IllegalArgumentException if size is less than or equal to zero
     */
    public SlotRequest_V0_2(String uploadServiceAddress, String filename, long size, String contentType) {
        super(uploadServiceAddress, filename, size, contentType, NAMESPACE);
    }

    //TODO: check string
    @Override
    public String getChildElementXML() {
        XmlStringBuilder xml = new XmlStringBuilder();

        xml.element("filename", filename);
        xml.element("size", String.valueOf(size));
        xml.optElement("content-type", contentType);

        return xml.toString();
    }
}