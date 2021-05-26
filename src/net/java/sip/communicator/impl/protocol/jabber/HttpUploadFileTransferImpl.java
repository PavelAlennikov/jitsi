package net.java.sip.communicator.impl.protocol.jabber;

import java.io.File;
import net.java.sip.communicator.service.protocol.AbstractFileTransfer;
import net.java.sip.communicator.service.protocol.Contact;
import net.java.sip.communicator.service.protocol.FileTransfer;
import net.java.sip.communicator.service.protocol.event.FileTransferStatusChangeEvent;

public class HttpUploadFileTransferImpl extends AbstractFileTransfer {

    private final String id;
    private final Contact contact;
    private final File file;
    private long transferredBytes = -1;

    public HttpUploadFileTransferImpl(File file) {
        this(null, null, file);
    }

    public HttpUploadFileTransferImpl(Contact contact, File file) {
        this(null, contact, file);
    }

    public HttpUploadFileTransferImpl(String id, Contact contact, File file) {
        if (id != null) {
            this.id = id;
        } else {
            this.id = String.valueOf(System.currentTimeMillis()) + hashCode();
        }
        this.file = file;
        this.contact = contact;
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
        return FileTransfer.OUT;
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
