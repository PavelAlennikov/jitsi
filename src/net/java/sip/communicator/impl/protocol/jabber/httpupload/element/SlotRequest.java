package net.java.sip.communicator.impl.protocol.jabber.httpupload.element;

import net.java.sip.communicator.impl.protocol.jabber.httpupload.*;
import org.jivesoftware.smack.packet.*;

public class SlotRequest extends IQ {
    public static final String ELEMENT = "request";
    public static final String NAMESPACE = HttpFileUploadManager.NAMESPACE;

    protected final String filename;
    protected final long size;
    protected final String contentType;

    public SlotRequest(String uploadServiceAddress, String filename, long size) {
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
    public SlotRequest(String uploadServiceAddress, String filename, long size, String contentType) {
        this(uploadServiceAddress, filename, size, contentType, NAMESPACE);
    }

    protected SlotRequest(String uploadServiceAddress, String filename, long size, String contentType, String namespace) {
        super();

        if (size <= 0) {
            throw new IllegalArgumentException("File fileSize must be greater than zero.");
        }

        this.filename = filename;
        this.size = size;
        this.contentType = contentType;

        setType(Type.GET);
        setTo(uploadServiceAddress);
    }

    public String getFilename() {
        return filename;
    }

    public long getSize() {
        return size;
    }

    public String getContentType() {
        return contentType;
    }

    @Override
    public String getChildElementXML() {
        XmlStringBuilder xml = new XmlStringBuilder();

        xml.halfOpenElement(ELEMENT);
        xml.xmlnsAttribute(NAMESPACE);
        xml.attribute("filename", filename);
        xml.attribute("size", String.valueOf(size));
        xml.optAttribute("content-type", contentType);
        xml.closeEmptyElement();

        return xml.toString();
    }
}