package net.java.sip.communicator.impl.protocol.jabber.httpupload.element;

import org.jivesoftware.smack.packet.*;

public class FileTooLargeError implements PacketExtension {
    public static final String ELEMENT = "file-too-large";
    public static final String NAMESPACE = SlotRequest.NAMESPACE;

    private final long maxFileSize;
    private final String namespace;

    public FileTooLargeError(long maxFileSize) {
        this(maxFileSize, NAMESPACE);
    }

    protected FileTooLargeError(long maxFileSize, String namespace) {
        this.maxFileSize = maxFileSize;
        this.namespace = namespace;
    }

    public long getMaxFileSize() {
        return maxFileSize;
    }

    @Override
    public String getElementName() {
        return ELEMENT;
    }

    @Override
    public String getNamespace() {
        return namespace;
    }

    @Override
    public String toXML() {
        return null;
    }

//    @Override
//    public XmlStringBuilder toXML(org.jivesoftware.smack.packet.XmlEnvironment enclosingNamespace) {
//        XmlStringBuilder xml = new XmlStringBuilder(this);
//        xml.rightAngleBracket();
//        xml.element("max-file-size", String.valueOf(maxFileSize));
//        xml.closeElement(this);
//        return xml;
//    }

    public static FileTooLargeError from(IQ iq) {
        XMPPError error = iq.getError();
        if (error == null) {
            return null;
        }
        return (FileTooLargeError) error.getExtension(ELEMENT, NAMESPACE);
    }
}