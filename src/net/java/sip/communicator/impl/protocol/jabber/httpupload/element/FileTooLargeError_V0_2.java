package net.java.sip.communicator.impl.protocol.jabber.httpupload.element;

import net.java.sip.communicator.impl.protocol.jabber.httpupload.*;

public class FileTooLargeError_V0_2 extends FileTooLargeError {

    public static final String NAMESPACE = HttpFileUploadManager.NAMESPACE_0_2;

    public FileTooLargeError_V0_2(long maxFileSize) {
        super(maxFileSize, NAMESPACE);
    }
}