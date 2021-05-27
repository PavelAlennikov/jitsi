package net.java.sip.communicator.impl.protocol.jabber;

import java.io.File;
import net.java.sip.communicator.service.protocol.AbstractFileTransfer;
import net.java.sip.communicator.service.protocol.Contact;
import net.java.sip.communicator.service.protocol.event.FileTransferStatusChangeEvent;

public class HttpUploadFileTransferImpl extends AbstractFileTransfer {

    private final String id;
    private final Contact contact;
    private final File file;
    private final int fileTransferDirection;
    private long transferredBytes = -1;

    public HttpUploadFileTransferImpl(File file, int fileTransferDirection) {
        this(null, null, file, fileTransferDirection);
    }

    public HttpUploadFileTransferImpl(Contact contact, File file, int fileTransferDirection) {
        this(null, contact, file, fileTransferDirection);
    }

    public HttpUploadFileTransferImpl(String id, Contact contact, File file, int fileTransferDirection) {
        if (id != null) {
            this.id = id;
        } else {
            this.id = String.valueOf(System.currentTimeMillis()) + hashCode();
        }
        this.file = file;
        this.contact = contact;
        this.fileTransferDirection = fileTransferDirection;
    }

    @Override
    public String getID() {
        return id;
    }

    @Override
    public void cancel() {
        fireStatusChangeEvent(FileTransferStatusChangeEvent.CANCELED);
    }

    @Override
    public int getDirection() {
        return fileTransferDirection;
    }

    @Override
    public File getLocalFile() {
        return file;
    }

    @Override
    public Contact getContact() {
        return contact;
    }

    @Override
    public long getTransferedBytes() {
        return transferredBytes;
    }

    public void setTransferredBytes(long transferredBytes) {
        this.transferredBytes = transferredBytes;
    }
}
