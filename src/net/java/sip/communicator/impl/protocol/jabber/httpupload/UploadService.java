package net.java.sip.communicator.impl.protocol.jabber.httpupload;

import java.util.*;

public class UploadService {

    public enum Version {
        /**
         * Upload service as specified in XEP-0363 v0.2 or lower.
         *
         * @see <a href="https://xmpp.org/extensions/attic/xep-0363-0.2.5.html">XEP-0363 v0.2.5</a>
         */
        v0_2,

        /**
         * Upload service as specified in XEP-0363 v0.3 or higher.
         *
         * @see <a href="https://xmpp.org/extensions/attic/xep-0363-0.4.0.html">XEP-0363 v0.4.0</a>
         */
        v0_3,
    }

    private final String address;
    private final Version version;
    private final Long maxFileSize;

    UploadService(String address, Version version) {
        this(address, version, null);
    }

    UploadService(String address, Version version, Long maxFileSize) {
        this.address = Objects.requireNonNull(address);
        this.version = version;
        this.maxFileSize = maxFileSize;
    }

    public String getAddress() {
        return address;
    }

    public Version getVersion() {
        return version;
    }

    public boolean hasMaxFileSizeLimit() {
        return maxFileSize != null;
    }

    public Long getMaxFileSize() {
        return maxFileSize;
    }

    public boolean acceptsFileOfSize(long size) {
        if (!hasMaxFileSizeLimit()) {
            return true;
        }

        return size <= maxFileSize;
    }
}
