package net.java.sip.communicator.impl.protocol.jabber.httpupload;

import org.jivesoftware.smack.*;

import java.util.*;
import java.util.concurrent.*;

public class XMPPConnectionRegistry {

    /**
     * A set of listeners which will be invoked if a new connection is created.
     */
    private static final Set<ConnectionCreationListener> connectionEstablishedListeners =
            new CopyOnWriteArraySet<>();

    /**
     * Adds a new listener that will be notified when new Connections are created. Note
     * that newly created connections will not be actually connected to the server.
     *
     * @param connectionCreationListener a listener interested on new connections.
     */
    public static void addConnectionCreationListener(
            ConnectionCreationListener connectionCreationListener) {
        connectionEstablishedListeners.add(connectionCreationListener);
    }

    /**
     * Removes a listener that was interested in connection creation events.
     *
     * @param connectionCreationListener a listener interested on new connections.
     */
    public static void removeConnectionCreationListener(
            ConnectionCreationListener connectionCreationListener) {
        connectionEstablishedListeners.remove(connectionCreationListener);
    }

    /**
     * Get the collection of listeners that are interested in connection creation events.
     *
     * @return a collection of listeners interested on new connections.
     */
    protected static Collection<ConnectionCreationListener> getConnectionCreationListeners() {
        return Collections.unmodifiableCollection(connectionEstablishedListeners);
    }

}