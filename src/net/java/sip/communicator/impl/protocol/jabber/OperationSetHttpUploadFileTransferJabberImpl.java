package net.java.sip.communicator.impl.protocol.jabber;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.security.GeneralSecurityException;
import net.java.sip.communicator.service.protocol.ChatRoom;
import net.java.sip.communicator.service.protocol.Contact;
import net.java.sip.communicator.service.protocol.Message;
import net.java.sip.communicator.service.protocol.OperationFailedException;
import net.java.sip.communicator.service.protocol.OperationSetBasicInstantMessaging;
import net.java.sip.communicator.service.protocol.OperationSetHttpUploadFileTransfer;
import net.java.sip.communicator.service.protocol.event.FileTransferListener;
import net.java.sip.communicator.util.Logger;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException.XMPPErrorException;
import org.jivesoftware.smackx.httpfileupload.HttpFileUploadManager;
import org.jivesoftware.smackx.httpfileupload.UploadProgressListener;
import org.jivesoftware.smackx.httpfileupload.UploadService;

public class OperationSetHttpUploadFileTransferJabberImpl
    implements OperationSetHttpUploadFileTransfer {

    /**
     * This class logger.
     */
    private static final Logger logger
        = Logger.getLogger(OperationSetMultiUserChatJabberImpl.class);

    /**
     * The currently valid Jabber protocol provider service implementation.
     */
    private final ProtocolProviderServiceJabberImpl jabberProvider;

    /**
     * Instantiates the user operation set with a currently valid instance of the Jabber protocol provider.
     *
     * @param jabberProvider a currently valid instance of ProtocolProviderServiceJabberImpl.
     */
    OperationSetHttpUploadFileTransferJabberImpl(
        ProtocolProviderServiceJabberImpl jabberProvider) {
        this.jabberProvider = jabberProvider;
    }

    @Override
    public void sendFile(ChatRoom chatRoom, File file) {
        try {
            HttpFileUploadManager httpFileUploadManager = jabberProvider.getHttpFileUploadManager();
            httpFileUploadManager.discoverUploadService();
            URL url = httpFileUploadManager.uploadFile(file, new UploadProgressListener() {
                @Override
                public void onUploadProgress(long uploadedBytes, long totalBytes) {
                    //TODO: add listener here
                }
            });

            sendMessage(chatRoom, url.toString());
        } catch (GeneralSecurityException | IOException | InterruptedException | XMPPErrorException | SmackException | OperationFailedException generalSecurityException) {
            generalSecurityException.printStackTrace();
        }
    }

    @Override
    public void sendFile(Contact toContact, File file)
        throws GeneralSecurityException, XMPPErrorException, SmackException, InterruptedException, IOException,
        OperationFailedException {
        OperationSetBasicInstantMessaging imOpSet
            = toContact
            .getProtocolProvider()
            .getOperationSet(OperationSetBasicInstantMessaging.class);

        HttpFileUploadManager httpFileUploadManager = jabberProvider.getHttpFileUploadManager();
        httpFileUploadManager.discoverUploadService();
        URL url = httpFileUploadManager.uploadFile(file, new UploadProgressListener() {
            @Override
            public void onUploadProgress(long uploadedBytes, long totalBytes) {
                //TODO: add file upload listener here if needed
            }
        });
        //TODO: return message?
        Message message = imOpSet.createMessage(url.toString());
        imOpSet.sendInstantMessage(toContact, message);
    }

    @Override
    public void addFileTransferListener(FileTransferListener listener) {
    }

    @Override
    public void removeFileTransferListener(FileTransferListener listener) {

    }

    @Override
    public long getMaximumFileLength() {
        HttpFileUploadManager httpFileUploadManager = HttpFileUploadManager
            .getInstanceFor((XMPPConnection) jabberProvider.getConnection());
        UploadService defaultUploadService = httpFileUploadManager.getDefaultUploadService();

        return defaultUploadService != null && defaultUploadService.hasMaxFileSizeLimit() ? defaultUploadService.getMaxFileSize() : Long.MAX_VALUE;
    }
    
    private void sendMessage(ChatRoom chatRoom, String messageText) throws OperationFailedException {
        Message message = chatRoom.createMessage(messageText);
        chatRoom.sendMessage(message);
    }
}
