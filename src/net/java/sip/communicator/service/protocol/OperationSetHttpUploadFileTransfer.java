package net.java.sip.communicator.service.protocol;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import net.java.sip.communicator.service.protocol.event.FileTransferListener;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException.XMPPErrorException;

public interface OperationSetHttpUploadFileTransfer extends OperationSet {

    void sendFile(ChatRoom chatRoom,
                         File file)
        throws  IllegalStateException,
        IllegalArgumentException,
        OperationNotSupportedException;

    void sendFile(Contact toContact, File file) throws GeneralSecurityException, XMPPErrorException, SmackException, InterruptedException, IOException, OperationFailedException;


    /**
     * Adds the given <tt>FileTransferListener</tt> that would listen for
     * file transfer requests and created file transfers.
     *
     * @param listener the <tt>FileTransferListener</tt> to add
     */
    public void addFileTransferListener(
        FileTransferListener listener);

    /**
     * Removes the given <tt>FileTransferListener</tt> that listens for
     * file transfer requests and created file transfers.
     *
     * @param listener the <tt>FileTransferListener</tt> to remove
     */
    public void removeFileTransferListener(
        FileTransferListener listener);

    /**
     * Returns the maximum file length supported by the protocol in bytes.
     * @return the file length that is supported.
     */
    public long getMaximumFileLength();

}
