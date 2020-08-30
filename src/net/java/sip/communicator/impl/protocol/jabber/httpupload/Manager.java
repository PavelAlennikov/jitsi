package net.java.sip.communicator.impl.protocol.jabber.httpupload;

import org.jivesoftware.smack.*;

import java.lang.ref.*;
import java.util.*;

abstract class Manager {

    final WeakReference<XMPPConnection> weakConnection;

    Manager(XMPPConnection connection) {
        Objects.requireNonNull(connection, "XMPPConnection must not be null");

        weakConnection = new WeakReference<>(connection);
    }

    protected final XMPPConnection connection() {
        return weakConnection.get();
    }

    /**
     * Get the XMPPConnection of this Manager if it's authenticated, i.e. logged in.
     * Otherwise throw a {@link SmackException.NotLoggedInException}.
     *
     * @return the XMPPConnection of this Manager.
     * @throws SmackException.NotLoggedInException if the connection is not authenticated.
     */
    protected final XMPPConnection getAuthenticatedConnectionOrThrow() throws SmackException.NotLoggedInException
    {
        XMPPConnection connection = connection();
        if (!connection.isAuthenticated()) {
            throw new SmackException.NotLoggedInException();
        }
        return connection;
    }
}
