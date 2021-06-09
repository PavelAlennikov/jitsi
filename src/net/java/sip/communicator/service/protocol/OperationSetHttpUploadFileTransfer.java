package net.java.sip.communicator.service.protocol;

import java.io.File;
import net.java.sip.communicator.service.protocol.event.FileTransferListener;

public interface OperationSetHttpUploadFileTransfer extends OperationSetFileTransfer {

    FileTransfer sendFile(ChatRoom chatRoom, File file) throws Exception;

    /**
     * Returns the maximum file length supported by the protocol in bytes.
     * @return the file length that is supported.
     */
    long getMaximumFileLength();

    /**
     * Adds the given <tt>FileTransferListener</tt> that would listen for
     * file transfer requests and created file transfers.
     *
     * @param listener the <tt>FileTransferListener</tt> to add
     */
    void addFileTransferListener(
        FileTransferListener listener);

    /**
     * Removes the given <tt>FileTransferListener</tt> that listens for
     * file transfer requests and created file transfers.
     *
     * @param listener the <tt>FileTransferListener</tt> to remove
     */
    void removeFileTransferListener(
        FileTransferListener listener);

}
