package net.java.sip.communicator.impl.protocol.jabber;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.StandardCharsets;
import net.java.sip.communicator.service.protocol.AbstractFileTransfer;
import net.java.sip.communicator.service.protocol.Contact;
import net.java.sip.communicator.service.protocol.FileTransfer;
import net.java.sip.communicator.service.protocol.IncomingFileTransferRequest;
import net.java.sip.communicator.service.protocol.event.FileTransferStatusChangeEvent;
import net.java.sip.communicator.util.Logger;

public class IncomingFileTransferRequestHttpUploadImpl implements IncomingFileTransferRequest {

    private static final Logger logger
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

            try
            {
                fileName = URLDecoder.decode(fileName, StandardCharsets.UTF_8.name());
            } catch (UnsupportedEncodingException e)
            {
                logger.error("Can't decode file name from url",  e);
            }
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

        new DownloadFileTransferProgressThread(downloadUrl, file, httpUploadFileTransfer).start();

        return httpUploadFileTransfer;
    }

    @Override
    public void rejectFile() {

    }

    @Override
    public byte[] getThumbnail() {
        return new byte[0];
    }

    protected static class DownloadFileTransferProgressThread extends Thread
    {

        private final URL url;
        private final AbstractFileTransfer fileTransfer;
        private final File file;

        public DownloadFileTransferProgressThread(
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
        public void run()
        {
            try
            {
                fileTransfer.fireStatusChangeEvent(FileTransferStatusChangeEvent.IN_PROGRESS);

                try (
                    ReadableByteChannel readableByteChannel = Channels.newChannel(url.openStream());
                    FileOutputStream fileOutputStream = new FileOutputStream(file)
                ) {
                    long transferred = fileOutputStream.getChannel()
                        .transferFrom(readableByteChannel, 0, Long.MAX_VALUE);
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
