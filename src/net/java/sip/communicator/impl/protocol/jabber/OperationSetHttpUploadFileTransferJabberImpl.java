package net.java.sip.communicator.impl.protocol.jabber;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.Vector;
import net.java.sip.communicator.impl.protocol.jabber.httpupload.HttpFileUploadManager;
import net.java.sip.communicator.impl.protocol.jabber.httpupload.UploadProgressListener;
import net.java.sip.communicator.impl.protocol.jabber.httpupload.UploadService;
import net.java.sip.communicator.service.protocol.ChatRoom;
import net.java.sip.communicator.service.protocol.Contact;
import net.java.sip.communicator.service.protocol.FileTransfer;
import net.java.sip.communicator.service.protocol.Message;
import net.java.sip.communicator.service.protocol.OperationFailedException;
import net.java.sip.communicator.service.protocol.OperationNotSupportedException;
import net.java.sip.communicator.service.protocol.OperationSetBasicInstantMessaging;
import net.java.sip.communicator.service.protocol.OperationSetHttpUploadFileTransfer;
import net.java.sip.communicator.service.protocol.event.FileTransferCreatedEvent;
import net.java.sip.communicator.service.protocol.event.FileTransferListener;
import net.java.sip.communicator.service.protocol.event.FileTransferRequestEvent;
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
     * A list of listeners registered for file transfer events.
     */
    private final Vector<FileTransferListener> fileTransferListeners = new Vector<>();

    /**
     * Instantiates the user operation set with a currently valid instance of the Jabber protocol provider.
     *
     * @param jabberProvider a currently valid instance of ProtocolProviderServiceJabberImpl.
     */
    OperationSetHttpUploadFileTransferJabberImpl(ProtocolProviderServiceJabberImpl jabberProvider)
    {
        this.jabberProvider = jabberProvider;
    }

    @Override
    public FileTransfer sendFile(ChatRoom chatRoom, File file) throws Exception
    {
        HttpFileUploadManager httpFileUploadManager = jabberProvider.getHttpFileUploadManager();
        HttpUploadFileTransferImpl httpUploadFileTransfer = new HttpUploadFileTransferImpl(file, FileTransfer.OUT);
        fireFileTransferCreated(new FileTransferCreatedEvent(httpUploadFileTransfer, new Date()));

        new ChatRoomFileTransferUploadProgressThread(file, httpUploadFileTransfer, httpFileUploadManager, chatRoom).start();

        return httpUploadFileTransfer;
    }

    @Override
    public FileTransfer sendFile(Contact toContact, File file) throws GeneralSecurityException {
        HttpFileUploadManager httpFileUploadManager = jabberProvider.getHttpFileUploadManager();
        HttpUploadFileTransferImpl httpUploadFileTransfer = new HttpUploadFileTransferImpl(toContact, file, FileTransfer.OUT);
        fireFileTransferCreated(new FileTransferCreatedEvent(httpUploadFileTransfer, new Date()));

        new FileTransferUploadProgressThread(file, httpUploadFileTransfer, httpFileUploadManager, toContact).start();

        return httpUploadFileTransfer;
    }

    @Override
    public FileTransfer sendFile(Contact toContact, Contact fromContact, String remotePath, String localPath)
        throws IllegalStateException, IllegalArgumentException, OperationNotSupportedException {

        throw new OperationNotSupportedException("This operation is not supported");
    }

    /**
     * Delivers the file transfer to all registered listeners.
     *
     * @param event the <tt>FileTransferEvent</tt> that we'd like delivered to
     * all registered file transfer listeners.
     */
    public void fireFileTransferCreated(FileTransferCreatedEvent event)
    {
        Iterator<FileTransferListener> listeners;
        synchronized (fileTransferListeners)
        {
            listeners = new ArrayList<>(fileTransferListeners).iterator();
        }

        while (listeners.hasNext())
        {
            FileTransferListener listener = listeners.next();

            listener.fileTransferCreated(event);
        }
    }

    /**
     * Delivers the specified event to all registered file transfer listeners.
     *
     * @param event the <tt>EventObject</tt> that we'd like delivered to all
     * registered file transfer listeners.
     */
    public void fireFileTransferRequestRejected(FileTransferRequestEvent event)
    {
        Iterator<FileTransferListener> listeners = null;
        synchronized (fileTransferListeners)
        {
            listeners = new ArrayList<FileTransferListener>
                (fileTransferListeners).iterator();
        }

        while (listeners.hasNext())
        {
            FileTransferListener listener = listeners.next();

            listener.fileTransferRequestRejected(event);
        }
    }


    /**
     * Delivers the specified event to all registered file transfer listeners.
     *
     * @param event the <tt>EventObject</tt> that we'd like delivered to all
     * registered file transfer listeners.
     */
    void fireFileTransferRequest(FileTransferRequestEvent event)
    {
        Iterator<FileTransferListener> listeners = null;
        synchronized (fileTransferListeners)
        {
            listeners = new ArrayList<FileTransferListener>
                (fileTransferListeners).iterator();
        }

        while (listeners.hasNext())
        {
            FileTransferListener listener = listeners.next();

            listener.fileTransferRequestReceived(event);
        }
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

    @Override
    public void addFileTransferListener(FileTransferListener listener)
    {
        synchronized(fileTransferListeners)
        {
            if(!fileTransferListeners.contains(listener))
            {
                this.fileTransferListeners.add(listener);
            }
        }
    }

    @Override
    public void removeFileTransferListener(FileTransferListener listener)
    {
        synchronized(fileTransferListeners)
        {
            this.fileTransferListeners.remove(listener);
        }
    }

    protected URL uploadFile(File file, final HttpUploadFileTransferImpl fileTransfer,
                             HttpFileUploadManager httpFileUploadManager)
        throws XMPPException, SmackException, IOException, InterruptedException {
        fileTransfer.fireStatusChangeEvent(FileTransferStatusChangeEvent.IN_PROGRESS);

        httpFileUploadManager.discoverUploadService();

        return httpFileUploadManager.uploadFile(file, new UploadProgressListener() {
            @Override
            public void onUploadProgress(long uploadedBytes, long totalBytes) {
                fileTransfer.setTransferredBytes(uploadedBytes);
                fileTransfer.fireProgressChangeEvent(System.currentTimeMillis(), uploadedBytes);
            }
        });
    }

    private class ChatRoomFileTransferUploadProgressThread extends Thread
    {

        private final File file;
        private final HttpUploadFileTransferImpl fileTransfer;
        private final HttpFileUploadManager httpFileUploadManager;
        private final ChatRoom chatRoom;

        public ChatRoomFileTransferUploadProgressThread(File file,
                                                        HttpUploadFileTransferImpl fileTransfer,
                                                        HttpFileUploadManager httpFileUploadManager,
                                                        ChatRoom chatRoom)
        {
            this.file = file;
            this.fileTransfer = fileTransfer;
            this.httpFileUploadManager = httpFileUploadManager;
            this.chatRoom = chatRoom;
        }

        @Override
        public void run()
        {
            try
            {
                Thread.sleep(15);

                URL url = uploadFile(file, fileTransfer, httpFileUploadManager);
                sendMessage(chatRoom, url.toString());
                fileTransfer.fireStatusChangeEvent(FileTransferStatusChangeEvent.COMPLETED);
            } catch (Exception e) {
                logger.error("Can't upload file via http", e);
                fileTransfer.fireStatusChangeEvent(FileTransferStatusChangeEvent.FAILED);
            }
        }

        private void sendMessage(ChatRoom chatRoom, String messageText) throws OperationFailedException
        {
            Message message = chatRoom.createMessage(messageText);
            chatRoom.sendMessage(message);
        }
    }

    private class FileTransferUploadProgressThread extends Thread {

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
                Thread.sleep(15);

                URL url = uploadFile(file, fileTransfer, httpFileUploadManager);
                sendMessage(toContact, url.toString());
                fileTransfer.fireStatusChangeEvent(FileTransferStatusChangeEvent.COMPLETED);
            } catch (Exception e) {
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
}
