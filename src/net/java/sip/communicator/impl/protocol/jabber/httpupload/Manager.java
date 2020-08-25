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
     * Get the XMPPConnection of this Manager if it's authenticated, i.e. logged in. Otherwise throw a {@link NotLoggedInException}.
     *
     * @return the XMPPConnection of this Manager.
     * @throws NotLoggedInException if the connection is not authenticated.
     */
    protected final XMPPConnection getAuthenticatedConnectionOrThrow() throws SmackException.NotLoggedInException {
        XMPPConnection connection = connection();
        if (!connection.isAuthenticated()) {
            throw new SmackException.NotLoggedInException();
        }
        return connection;
    }

//    protected static final ScheduledAction schedule(Runnable runnable, long delay, TimeUnit unit) {
//        return schedule(runnable, delay, unit, ScheduledAction.Kind.NonBlocking);
//    }
//
//    protected static final ScheduledAction scheduleBlocking(Runnable runnable, long delay, TimeUnit unit) {
//        return schedule(runnable, delay, unit, ScheduledAction.Kind.Blocking);
//    }
//
//    protected static final ScheduledAction schedule(Runnable runnable, long delay, TimeUnit unit, ScheduledAction.Kind scheduledActionKind) {
//        return AbstractXMPPConnection.SMACK_REACTOR.schedule(runnable, delay, unit, scheduledActionKind);
//    }
}
