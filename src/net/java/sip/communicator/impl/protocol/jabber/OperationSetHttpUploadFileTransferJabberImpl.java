package net.java.sip.communicator.impl.protocol.jabber;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import net.java.sip.communicator.impl.protocol.jabber.httpupload.HttpFileUploadManager;
import net.java.sip.communicator.impl.protocol.jabber.httpupload.UploadProgressListener;
import net.java.sip.communicator.impl.protocol.jabber.httpupload.UploadService;
import net.java.sip.communicator.service.protocol.AbstractFileTransfer;
import net.java.sip.communicator.service.protocol.ChatRoom;
import net.java.sip.communicator.service.protocol.Contact;
import net.java.sip.communicator.service.protocol.FileTransfer;
import net.java.sip.communicator.service.protocol.Message;
import net.java.sip.communicator.service.protocol.OperationFailedException;
import net.java.sip.communicator.service.protocol.OperationSetBasicInstantMessaging;
import net.java.sip.communicator.service.protocol.OperationSetHttpUploadFileTransfer;
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
        = Logger.getLogger(OperationSetHttpUploadFileTransferJabberImpl.class);

    /**
     * The currently valid Jabber protocol provider service implementation.
     */
    private final ProtocolProviderServiceJabberImpl jabberProvider;

    /**
     * Instantiates the user operation set with a currently valid instance of the Jabber protocol provider.
     *
     * @param jabberProvider a currently valid instance of ProtocolProviderServiceJabberImpl.
     */
    OperationSetHttpUploadFileTransferJabberImpl(ProtocolProviderServiceJabberImpl jabberProvider) {
        this.jabberProvider = jabberProvider;
    }

    @Override
    public FileTransfer sendFile(ChatRoom chatRoom, File file) throws Exception {
        HttpUploadFileTransferImpl httpUploadFileTransfer = new HttpUploadFileTransferImpl(file);
        HttpFileUploadManager httpFileUploadManager = jabberProvider.getHttpFileUploadManager();

        new ChatRoomFileTransferUploadProgressThread(file, httpUploadFileTransfer, httpFileUploadManager, chatRoom).start();

        return httpUploadFileTransfer;
    }

    @Override
    public FileTransfer sendFile(Contact toContact, File file) throws Exception {
        HttpUploadFileTransferImpl httpUploadFileTransfer = new HttpUploadFileTransferImpl(toContact, file);
        HttpFileUploadManager httpFileUploadManager = jabberProvider.getHttpFileUploadManager();

        new FileTransferUploadProgressThread(file, httpUploadFileTransfer, httpFileUploadManager, toContact).start();

        return httpUploadFileTransfer;
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

    private static class ChatRoomFileTransferUploadProgressThread extends AbstractFileTransferUploadProgressThread {

        private final File file;
        private final HttpUploadFileTransferImpl fileTransfer;
        private final HttpFileUploadManager httpFileUploadManager;
        private final ChatRoom chatRoom;

        public ChatRoomFileTransferUploadProgressThread(File file,
                                                        HttpUploadFileTransferImpl fileTransfer,
                                                        HttpFileUploadManager httpFileUploadManager,
                                                        ChatRoom chatRoom) {
            this.file = file;
            this.fileTransfer = fileTransfer;
            this.httpFileUploadManager = httpFileUploadManager;
            this.chatRoom = chatRoom;
        }

        @Override
        public void run() {
            try {
                URL url = uploadFile(file, fileTransfer, httpFileUploadManager);
                sendMessage(chatRoom, url.toString());
            } catch (SmackException | IOException | XMPPException | InterruptedException | OperationFailedException e) {
                logger.error("Can't upload file via http", e);
                fileTransfer.fireStatusChangeEvent(FileTransferStatusChangeEvent.FAILED);
            }
        }

        private void sendMessage(ChatRoom chatRoom, String messageText) throws OperationFailedException {
            Message message = chatRoom.createMessage(messageText);
            chatRoom.sendMessage(message);
        }
    }

    private static class FileTransferUploadProgressThread extends AbstractFileTransferUploadProgressThread {

        private final File file;
        private final HttpUploadFileTransferImpl fileTransfer;
        private final HttpFileUploadManager httpFileUploadManager;
        private final Contact toContact;

        public FileTransferUploadProgressThread(File file,
                                                HttpUploadFileTransferImpl fileTransfer,
                                                HttpFileUploadManager httpFileUploadManager,
                                                Contact toContact) {
            this.file = file;
            this.fileTransfer = fileTransfer;
            this.httpFileUploadManager = httpFileUploadManager;
            this.toContact = toContact;
        }

        @Override
        public void run() {
            try {
                URL url = uploadFile(file, fileTransfer, httpFileUploadManager);
                sendMessage(toContact, url.toString());
            } catch (SmackException | IOException | XMPPException | InterruptedException e) {
                logger.error("Can't upload file via http", e);
                fileTransfer.fireStatusChangeEvent(FileTransferStatusChangeEvent.FAILED);
            }
        }

        private void sendMessage(Contact toContact, String url) {
            OperationSetBasicInstantMessaging imOpSet
                = toContact
                .getProtocolProvider()
                .getOperationSet(OperationSetBasicInstantMessaging.class);

            Message message = imOpSet.createMessage(url);
            imOpSet.sendInstantMessage(toContact, message);
        }
    }

    private abstract static class AbstractFileTransferUploadProgressThread extends Thread {

        protected URL uploadFile(File file, final HttpUploadFileTransferImpl fileTransfer,
                                 HttpFileUploadManager httpFileUploadManager)
            throws XMPPException, SmackException, IOException, InterruptedException {
            fileTransfer.fireStatusChangeEvent(FileTransferStatusChangeEvent.IN_PROGRESS);

            httpFileUploadManager.discoverUploadService();

            URL url = httpFileUploadManager.uploadFile(file, new UploadProgressListener() {
                @Override
                public void onUploadProgress(long uploadedBytes, long totalBytes) {
                    fileTransfer.setTransferredBytes(uploadedBytes);
                    fileTransfer.fireProgressChangeEvent(System.currentTimeMillis(), uploadedBytes);
                }
            });
            fileTransfer.fireStatusChangeEvent(FileTransferStatusChangeEvent.COMPLETED);

            return url;
        }
    }
}
