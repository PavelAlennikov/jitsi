package net.java.sip.communicator.impl.protocol.jabber.httpupload;

public interface UploadProgressListener {

    /**
     * Callback for displaying upload progress.
     *
     * @param uploadedBytes the number of bytes uploaded at the moment
     * @param totalBytes the total number of bytes to be uploaded
     */
    void onUploadProgress(long uploadedBytes, long totalBytes);

}