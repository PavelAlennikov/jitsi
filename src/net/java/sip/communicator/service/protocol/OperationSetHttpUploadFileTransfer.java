package net.java.sip.communicator.service.protocol;

import java.io.File;
import net.java.sip.communicator.service.protocol.event.FileTransferListener;

public interface OperationSetHttpUploadFileTransfer extends OperationSet {

    FileTransfer sendFile(ChatRoom chatRoom, File file) throws Exception;

    FileTransfer sendFile(Contact toContact, File file) throws Exception;

    /**
     * Returns the maximum file length supported by the protocol in bytes.
     * @return the file length that is supported.
     */
    long getMaximumFileLength();

}
