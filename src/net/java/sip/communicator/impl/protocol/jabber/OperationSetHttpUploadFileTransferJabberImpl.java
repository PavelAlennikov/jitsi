package net.java.sip.communicator.impl.protocol.jabber;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.security.GeneralSecurityException;
import net.java.sip.communicator.impl.protocol.jabber.httpupload.HttpFileUploadManager;
import net.java.sip.communicator.impl.protocol.jabber.httpupload.UploadProgressListener;
import net.java.sip.communicator.impl.protocol.jabber.httpupload.UploadService;
import net.java.sip.communicator.service.protocol.AbstractFileTransfer;
import net.java.sip.communicator.service.protocol.ChatRoom;
import net.java.sip.communicator.service.protocol.Contact;
import net.java.sip.communicator.service.protocol.Message;
import net.java.sip.communicator.service.protocol.OperationFailedException;
import net.java.sip.communicator.service.protocol.OperationSetBasicInstantMessaging;
import net.java.sip.communicator.service.protocol.OperationSetHttpUploadFileTransfer;
import net.java.sip.communicator.service.protocol.event.FileTransferListener;
import net.java.sip.communicator.service.protocol.event.FileTransferStatusChangeEvent;
import net.java.sip.communicator.util.Logger;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;

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
        } catch (GeneralSecurityException | IOException | InterruptedException | SmackException
                | OperationFailedException | XMPPException generalSecurityException) {
            logger.error(generalSecurityException);
        }
    }

    @Override
    public void sendFile(Contact toContact, File file)
            throws GeneralSecurityException, SmackException, InterruptedException, IOException, XMPPException {
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

        return defaultUploadService != null && defaultUploadService.hasMaxFileSizeLimit()
                ? defaultUploadService.getMaxFileSize()
                : Long.MAX_VALUE;
    }

    private void sendMessage(ChatRoom chatRoom, String messageText) throws OperationFailedException {
        Message message = chatRoom.createMessage(messageText);
        chatRoom.sendMessage(message);
    }

    protected static class FileTransferProgressThread extends Thread {

        private final URL url;
        private final AbstractFileTransfer fileTransfer;
        private final File file;

        public FileTransferProgressThread(
            URL url,
            File file,
            AbstractFileTransfer transfer) {
            this.file = file;
            this.url = url;
            this.fileTransfer = transfer;
        }

        /**
         * Thread entry point.
         */
        @Override
        public void run() {
            try {
                fileTransfer.fireStatusChangeEvent(FileTransferStatusChangeEvent.IN_PROGRESS);

                try (
                    ReadableByteChannel readableByteChannel = Channels.newChannel(url.openStream());
                    FileOutputStream fileOutputStream = new FileOutputStream(file)
                ) {
                    long transferred = fileOutputStream.getChannel().transferFrom(readableByteChannel, 0, Long.MAX_VALUE);
                    ((HttpUploadFileTransferImpl) fileTransfer).setTransferredBytes(transferred);
                }

                fileTransfer.fireStatusChangeEvent(FileTransferStatusChangeEvent.COMPLETED);
            } catch (IOException e) {
                logger.error("Can't download http uploaded file", e);
                fileTransfer.fireStatusChangeEvent(FileTransferStatusChangeEvent.FAILED, e.getLocalizedMessage());
            }
        }
    }
}
