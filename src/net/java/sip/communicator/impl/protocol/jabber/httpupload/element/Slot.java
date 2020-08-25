package net.java.sip.communicator.impl.protocol.jabber.httpupload.element;

import net.java.sip.communicator.impl.protocol.jabber.httpupload.*;
import org.jivesoftware.smack.packet.*;

import java.net.*;
import java.util.*;

public class Slot extends IQ {

    public static final String ELEMENT = "slot";
    public static final String NAMESPACE = SlotRequest.NAMESPACE;

    protected final URL putUrl;
    protected final URL getUrl;

    private final Map<String, String> headers;

    public Slot(URL putUrl, URL getUrl) {
        this(putUrl, getUrl, null);
    }

    public Slot(URL putUrl, URL getUrl, Map<String, String> headers) {
        this(putUrl, getUrl, headers, NAMESPACE);
    }

    protected Slot(URL putUrl, URL getUrl, Map<String, String> headers, String namespace) {
        super();
        setDefaultXmlns(namespace);
        setType(Type.RESULT);
        this.putUrl = putUrl;
        this.getUrl = getUrl;
        if (headers == null) {
            this.headers = Collections.emptyMap();
        } else {
            this.headers = Collections.unmodifiableMap(headers);
        }
    }

    public URL getPutUrl() {
        return putUrl;
    }

    public URL getGetUrl() {
        return getUrl;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    @Override
    public String getChildElementXML() {
        XmlStringBuilder xml = new XmlStringBuilder();

        xml.halfOpenElement("put").attribute("url", putUrl.toString());
        if (headers.isEmpty()) {
            xml.closeEmptyElement();
        } else {
            xml.rightAngleBracket();
            for (Map.Entry<String, String> entry : getHeaders().entrySet()) {
                xml.halfOpenElement("header").attribute("name", entry.getKey()).rightAngleBracket();
                xml.escape(entry.getValue());
                xml.closeElement("header");
            }
            xml.closeElement("put");
        }

        xml.halfOpenElement("get").attribute("url", getUrl.toString()).closeEmptyElement();

        return xml.toString();
    }
}
