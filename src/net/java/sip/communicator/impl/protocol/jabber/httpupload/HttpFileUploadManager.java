package net.java.sip.communicator.impl.protocol.jabber.httpupload;

import net.java.sip.communicator.impl.protocol.jabber.httpupload.element.*;
import net.java.sip.communicator.util.Logger;
import org.jivesoftware.smack.*;
import org.jivesoftware.smackx.*;
import org.jivesoftware.smackx.packet.*;

import javax.net.ssl.*;
import java.io.*;
import java.net.*;
import java.util.*;

public class HttpFileUploadManager extends Manager {

    /**
     * Namespace of XEP-0363 v0.4 or higher. Constant value {@value #NAMESPACE}.
     *
     * @see <a href="https://xmpp.org/extensions/attic/xep-0363-0.4.0.html">XEP-0363 v0.4.0</a>
     */
    public static final String NAMESPACE = "urn:xmpp:http:upload:0";

    /**
     * Namespace of XEP-0363 v0.2 or lower. Constant value {@value #NAMESPACE_0_2}.
     *
     * @see <a href="https://xmpp.org/extensions/attic/xep-0363-0.2.5.html">XEP-0363 v0.2.5</a>
     */
    public static final String NAMESPACE_0_2 = "urn:xmpp:http:upload";

    private static final Logger LOGGER = Logger.getLogger(HttpFileUploadManager.class.getName());

    static {
        XMPPConnectionRegistry.addConnectionCreationListener(new ConnectionCreationListener() {
            @Override
            public void connectionCreated(Connection connection) {
                getInstanceFor((XMPPConnection) connection);

            }
        });
    }

    private static final Map<XMPPConnection, HttpFileUploadManager> INSTANCES = new WeakHashMap<>();

    private UploadService defaultUploadService;

    private SSLSocketFactory tlsSocketFactory;

    /**
     * Obtain the HttpFileUploadManager responsible for a connection.
     *
     * @param connection the connection object.
     * @return a HttpFileUploadManager instance
     */
    public static synchronized HttpFileUploadManager getInstanceFor(XMPPConnection connection) {
        HttpFileUploadManager httpFileUploadManager = INSTANCES.get(connection);
        if (httpFileUploadManager == null) {
            httpFileUploadManager = new HttpFileUploadManager(connection);
            INSTANCES.put(connection, httpFileUploadManager);
        }

        return httpFileUploadManager;
    }

    private HttpFileUploadManager(XMPPConnection connection) {
        super(connection);

        connection.addConnectionListener(new ConnectionListener() {
            @Override
            public void connectionClosed() {

            }

            @Override
            public void connectionClosedOnError(Exception e) {

            }

            @Override
            public void reconnectingIn(int i) {

            }

            @Override
            public void reconnectionSuccessful() {
                try {
                    discoverUploadService();
                } catch (XMPPException e) {
                    LOGGER.warn("Error during discovering HTTP File Upload service", e);
                }
            }

            @Override
            public void reconnectionFailed(Exception e) {

            }
        });
    }

    private static UploadService uploadServiceFrom(DiscoverInfo discoverInfo) {
        assert containsHttpFileUploadNamespace(discoverInfo);

        UploadService.Version version;
        if (discoverInfo.containsFeature(NAMESPACE)) {
            version = UploadService.Version.v0_3;
        } else if (discoverInfo.containsFeature(NAMESPACE_0_2)) {
            version = UploadService.Version.v0_2;
        } else {
            throw new AssertionError();
        }

        String address = discoverInfo.getFrom();

        return new UploadService(address, version);
    }

    /**
     * Discover upload service.
     * <p>
     * Called automatically when connection is authenticated.
     * <p>
     * Note that this is a synchronous call -- Smack must wait for the server response.
     *
     * @return true if upload service was discovered
     * @throws XMPPException if there was an XMPP error returned.
     */
    public boolean discoverUploadService() throws XMPPException {
        ServiceDiscoveryManager sdm = ServiceDiscoveryManager.getInstanceFor(connection());
        String serviceName = connection().getServiceName();

        DiscoverItems discoverItems = null;
        try {
            discoverItems = sdm.discoverItems(serviceName);
        } catch (XMPPException xmppe) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(
                        "Failed to discover the items associated with"
                                + " Jabber entity: " + serviceName,
                        xmppe);
            }
        }

        if (discoverItems != null) {
            Iterator<DiscoverItems.Item> discoverItemIter = discoverItems.getItems();

            while (discoverItemIter.hasNext()) {
                DiscoverItems.Item discoverItem = discoverItemIter.next();
                String entityID = discoverItem.getEntityID();
                DiscoverInfo discoverInfo = null;

                try {
                    discoverInfo = sdm.discoverInfo(entityID);
                } catch (XMPPException xmppe) {
                    LOGGER.warn(
                            "Failed to discover information about Jabber"
                                    + " entity: " + entityID,
                            xmppe);
                }
                if ((discoverInfo != null)
                        && (discoverInfo.containsFeature(NAMESPACE) || discoverInfo.containsFeature(NAMESPACE_0_2))) {
                    defaultUploadService = uploadServiceFrom(discoverInfo);
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Check if upload service was discovered.
     *
     * @return true if upload service was discovered
     */
    public boolean isUploadServiceDiscovered() {
        return defaultUploadService != null;
    }

    /**
     * Get default upload service if it was discovered.
     *
     * @return upload service JID or null if not available
     */
    public UploadService getDefaultUploadService() {
        return defaultUploadService;
    }

    /**
     * Request slot and uploaded file to HTTP file upload service.
     * <p>
     * You don't need to request slot and upload file separately, this method will do both.
     * Note that this is a synchronous call -- Smack must wait for the server response.
     *
     * @param file file to be uploaded
     * @return public URL for sharing uploaded file
     * @throws InterruptedException if the calling thread was interrupted.
     * @throws XMPPException        if there was an XMPP error returned.
     * @throws SmackException       if Smack detected an exceptional situation.
     * @throws IOException          in case of HTTP upload errors
     */
    public URL uploadFile(File file) throws InterruptedException, XMPPException, SmackException, IOException {
        return uploadFile(file, null);
    }

    /**
     * Request slot and uploaded file to HTTP file upload service with progress callback.
     * <p>
     * You don't need to request slot and upload file separately, this method will do both.
     * Note that this is a synchronous call -- Smack must wait for the server response.
     *
     * @param file     file to be uploaded
     * @param listener Upload progress listener or null
     * @return public URL for sharing uploaded file
     * @throws InterruptedException if the calling thread was interrupted.
     * @throws XMPPException        if there was an XMPP error returned.
     * @throws SmackException       if Smack detected an exceptional situation.
     * @throws IOException          if an I/O error occurred.
     */
    public URL uploadFile(File file, UploadProgressListener listener) throws InterruptedException,
            XMPPException, SmackException, IOException {
        if (!file.isFile()) {
            throw new FileNotFoundException("The path " + file.getAbsolutePath() + " is not a file");
        }
        final Slot slot = requestSlot(file.getName(), file.length(), "application/octet-stream");
        final long fileSize = file.length();
        // Construct the FileInputStream first to make sure we can actually read the file.
        final FileInputStream fis = new FileInputStream(file);
        upload(fis, fileSize, slot, listener);
        return slot.getGetUrl();
    }

    /**
     * Request slot and uploaded stream to HTTP upload service.
     * <p>
     * You don't need to request slot and upload input stream separately, this method will do both.
     * Note that this is a synchronous call -- Smack must wait for the server response.
     *
     * @param inputStream Input stream used for the upload.
     * @param fileName    Name of the file.
     * @param fileSize    Size of the file.
     * @return public URL for sharing uploaded file
     * @throws XMPPException        XMPPErrorException if there was an XMPP error returned.
     * @throws InterruptedException If the calling thread was interrupted.
     * @throws SmackException       If Smack detected an exceptional situation.
     * @throws IOException          If an I/O error occurred.
     */
    public URL uploadFile(InputStream inputStream, String fileName, long fileSize)
            throws XMPPException, InterruptedException, SmackException, IOException {
        return uploadFile(inputStream, fileName, fileSize, null);
    }

    /**
     * Request slot and uploaded stream to HTTP upload service.
     * <p>
     * You don't need to request slot and upload input stream separately, this method will do both.
     * Note that this is a synchronous call -- Smack must wait for the server response.
     *
     * @param inputStream Input stream used for the upload.
     * @param fileName    Name of the file.
     * @param fileSize    file size in bytes.
     * @param listener    upload progress listener or null.
     * @return public URL for sharing uploaded file
     * @throws XMPPException        XMPPErrorException if there was an XMPP error returned.
     * @throws InterruptedException If the calling thread was interrupted.
     * @throws SmackException       If Smack detected an exceptional situation.
     * @throws IOException          If an I/O error occurred.
     */
    public URL uploadFile(InputStream inputStream, String fileName, long fileSize, UploadProgressListener listener)
            throws XMPPException, InterruptedException, SmackException, IOException {
        Objects.requireNonNull(inputStream, "Input Stream cannot be null");
        Objects.requireNonNull(fileName, "Filename Stream cannot be null");
        if (fileSize < 0) {
            throw new IllegalArgumentException("File size cannot be negative");
        }
        final Slot slot = requestSlot(fileName, fileSize, "application/octet-stream");
        upload(inputStream, fileSize, slot, listener);
        return slot.getGetUrl();
    }

    /**
     * Request a new upload slot from default upload service (if discovered). When you get slot you should upload file
     * to PUT URL and share GET URL. Note that this is a synchronous call -- Smack must wait for the server response.
     *
     * @param filename name of file to be uploaded
     * @param fileSize file size in bytes.
     * @return file upload Slot in case of success
     * @throws IllegalArgumentException if fileSize is less than or equal to zero or greater than the maximum size
     *                                  supported by the service.
     * @throws InterruptedException     if the calling thread was interrupted.
     * @throws XMPPException            if there was an XMPP error returned.
     * @throws SmackException           if smack exception.
     */
    public Slot requestSlot(String filename, long fileSize) throws InterruptedException,
            XMPPException, SmackException {
        return requestSlot(filename, fileSize, null, null);
    }

    /**
     * Request a new upload slot with optional content type from default upload service (if discovered).
     * <p>
     * When you get slot you should upload file to PUT URL and share GET URL.
     * Note that this is a synchronous call -- Smack must wait for the server response.
     *
     * @param filename    name of file to be uploaded
     * @param fileSize    file size in bytes.
     * @param contentType file content-type or null
     * @return file upload Slot in case of success
     * @throws IllegalArgumentException             if fileSize is less than or equal to zero or greater than the maximum size
     *                                              supported by the service.
     * @throws SmackException.NotConnectedException if the XMPP connection is not connected.
     * @throws InterruptedException                 if the calling thread was interrupted.
     * @throws XMPPException                        if there was an XMPP error returned.
     * @throws SmackException                       if smack exception.
     */
    public Slot requestSlot(String filename, long fileSize, String contentType) throws SmackException,
            InterruptedException, XMPPException {
        return requestSlot(filename, fileSize, contentType, null);
    }

    /**
     * Request a new upload slot with optional content type from custom upload service.
     * <p>
     * When you get slot you should upload file to PUT URL and share GET URL.
     * Note that this is a synchronous call -- Smack must wait for the server response.
     *
     * @param filename             name of file to be uploaded
     * @param fileSize             file size in bytes.
     * @param contentType          file content-type or null
     * @param uploadServiceAddress the address of the upload service to use or null for default one
     * @return file upload Slot in case of success
     * @throws IllegalArgumentException if fileSize is less than or equal to zero or greater than the maximum size
     *                                  supported by the service.
     * @throws SmackException           if Smack detected an exceptional situation.
     * @throws InterruptedException     if the calling thread was interrupted.
     * @throws XMPPException            if there was an XMPP error returned.
     */
    public Slot requestSlot(String filename, long fileSize, String contentType, String uploadServiceAddress)
            throws SmackException, XMPPException {
        final XMPPConnection connection = connection();
        final UploadService defaultUploadService = this.defaultUploadService;

        // The upload service we are going to use.
        UploadService uploadService;

        if (uploadServiceAddress == null) {
            uploadService = defaultUploadService;
        } else {
            if (defaultUploadService != null && defaultUploadService.getAddress().equals(uploadServiceAddress)) {
                // Avoid performing a service discovery if we already know about the given service.
                uploadService = defaultUploadService;
            } else {
                DiscoverInfo discoverInfo = ServiceDiscoveryManager.getInstanceFor(connection).discoverInfo(uploadServiceAddress);
                if (!containsHttpFileUploadNamespace(discoverInfo)) {
                    throw new IllegalArgumentException("There is no HTTP upload service running at the given address '"
                            + uploadServiceAddress + '\'');
                }
                uploadService = uploadServiceFrom(discoverInfo);
            }
        }

        if (uploadService == null) {
            throw new SmackException("No upload service specified and also none discovered.");
        }

        if (!uploadService.acceptsFileOfSize(fileSize)) {
            throw new IllegalArgumentException(
                    "Requested file size " + fileSize + " is greater than max allowed size " + uploadService.getMaxFileSize());
        }

        SlotRequest slotRequest;
        switch (uploadService.getVersion()) {
            case v0_3:
                slotRequest = new SlotRequest(uploadService.getAddress(), filename, fileSize, contentType);
                break;
            case v0_2:
                slotRequest = new SlotRequest_V0_2(uploadService.getAddress(), filename, fileSize, contentType);
                break;
            default:
                throw new AssertionError();
        }

        return connection.createPacketCollectorAndSend(slotRequest).nextResultOrThrow();
    }

    public void setTlsContext(SSLContext tlsContext) {
        if (tlsContext == null) {
            return;
        }
        this.tlsSocketFactory = tlsContext.getSocketFactory();
    }

    private void upload(InputStream iStream, long fileSize, Slot slot, UploadProgressListener listener) throws IOException {
        final URL putUrl = slot.getPutUrl();

        final HttpURLConnection urlConnection = (HttpURLConnection) putUrl.openConnection();

        urlConnection.setRequestMethod("PUT");
        urlConnection.setUseCaches(false);
        urlConnection.setDoOutput(true);
        urlConnection.setFixedLengthStreamingMode(fileSize);
        urlConnection.setRequestProperty("Content-Type", "application/octet-stream");
        for (Map.Entry<String, String> header : slot.getHeaders().entrySet()) {
            urlConnection.setRequestProperty(header.getKey(), header.getValue());
        }

        final SSLSocketFactory tlsSocketFactory = this.tlsSocketFactory;
        if (tlsSocketFactory != null && urlConnection instanceof HttpsURLConnection) {
            HttpsURLConnection httpsUrlConnection = (HttpsURLConnection) urlConnection;
            httpsUrlConnection.setSSLSocketFactory(tlsSocketFactory);
        }

        try {
            OutputStream outputStream = urlConnection.getOutputStream();

            long bytesSend = 0;

            if (listener != null) {
                listener.onUploadProgress(0, fileSize);
            }

            BufferedInputStream inputStream = new BufferedInputStream(iStream);

            // TODO Factor in extra static method (and re-use e.g. in bytestream code).
            byte[] buffer = new byte[4096];
            int bytesRead;
            try {
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                    bytesSend += bytesRead;

                    if (listener != null) {
                        listener.onUploadProgress(bytesSend, fileSize);
                    }
                }
            } finally {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    LOGGER.warn("Exception while closing input stream", e);
                }
                try {
                    outputStream.close();
                } catch (IOException e) {
                    LOGGER.warn("Exception while closing output stream", e);
                }
            }

            int status = urlConnection.getResponseCode();
            switch (status) {
                case HttpURLConnection.HTTP_OK:
                case HttpURLConnection.HTTP_CREATED:
                case HttpURLConnection.HTTP_NO_CONTENT:
                    break;
                default:
                    throw new IOException("Error response " + status + " from server during file upload: "
                            + urlConnection.getResponseMessage() + ", file size: " + fileSize + ", put URL: "
                            + putUrl);
            }
        } finally {
            urlConnection.disconnect();
        }
    }

    public static UploadService.Version namespaceToVersion(String namespace) {
        UploadService.Version version;
        switch (namespace) {
            case NAMESPACE:
                version = UploadService.Version.v0_3;
                break;
            case NAMESPACE_0_2:
                version = UploadService.Version.v0_2;
                break;
            default:
                version = null;
                break;
        }
        return version;
    }

    private static boolean containsHttpFileUploadNamespace(DiscoverInfo discoverInfo) {
        return discoverInfo.containsFeature(NAMESPACE) || discoverInfo.containsFeature(NAMESPACE_0_2);
    }
}
