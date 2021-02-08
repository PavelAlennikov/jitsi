package net.java.sip.communicator.impl.protocol.jabber;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import net.java.sip.communicator.service.protocol.Contact;
import net.java.sip.communicator.service.protocol.FileTransfer;
import net.java.sip.communicator.service.protocol.IncomingFileTransferRequest;
import net.java.sip.communicator.util.Logger;

public class IncomingFileTransferRequestHttpUploadImpl implements IncomingFileTransferRequest {

    private final Logger logger
        = Logger.getLogger(IncomingFileTransferRequestHttpUploadImpl.class);

    private final Contact sender;
    private final String id;
    private final URL downloadUrl;
    private Long fileSize;
    private String fileName;

    public IncomingFileTransferRequestHttpUploadImpl(Contact sender, URL downloadUrl) {
        this.downloadUrl = downloadUrl;
        this.sender = sender;
        this.id = String.valueOf(System.currentTimeMillis()) + hashCode();
    }

    @Override
    public String getID() {
        return id;
    }

    @Override
    public String getFileName() {
        if (fileName == null) {
            String url = downloadUrl.toString();
            fileName = url.substring(url.lastIndexOf("/") + 1);
        }

        return fileName;
    }

    @Override
    public String getFileDescription() {
        return null;
    }

    @Override
    public long getFileSize() {
        if (fileSize == null) {
            HttpURLConnection connection;

            try {
                HttpURLConnection.setFollowRedirects(false);

                connection = (HttpURLConnection) downloadUrl.openConnection();
                connection.setRequestMethod("HEAD");

                fileSize = connection.getContentLengthLong();
            } catch (IOException e) {
                logger.error("Can't get file size", e);
            }
        }

        return fileSize;
    }

    @Override
    public Contact getSender() {
        return sender;
    }

    @Override
    public FileTransfer acceptFile(File file) {
        HttpUploadFileTransferImpl httpUploadFileTransfer = new HttpUploadFileTransferImpl(id, sender, file);

        new OperationSetHttpUploadFileTransferJabberImpl.FileTransferProgressThread(
            downloadUrl, file, httpUploadFileTransfer).start();

        return httpUploadFileTransfer;
    }

    @Override
    public void rejectFile() {

    }

    @Override
    public byte[] getThumbnail() {
        return new byte[0];
    }
}
