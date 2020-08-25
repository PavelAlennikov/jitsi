package net.java.sip.communicator.impl.protocol.jabber.httpupload.provider;

import net.java.sip.communicator.impl.protocol.jabber.httpupload.*;
import net.java.sip.communicator.impl.protocol.jabber.httpupload.element.*;
import org.jivesoftware.smack.packet.*;
import org.jivesoftware.smack.provider.*;
import org.xmlpull.v1.*;

import java.io.*;
import java.net.*;
import java.util.*;

import static org.xmlpull.v1.XmlPullParser.END_TAG;
import static org.xmlpull.v1.XmlPullParser.START_TAG;

public class SlotProvider implements IQProvider {

    @Override
    public IQ parseIQ(XmlPullParser xmlPullParser) throws Exception {
        final String namespace = xmlPullParser.getNamespace();

        int initialDepth = xmlPullParser.getDepth();

        final UploadService.Version version = HttpFileUploadManager.namespaceToVersion(namespace);
        assert version != null;

        URL putUrl = null;
        URL getUrl = null;
        PutElement_V0_4_Content putElementV04Content = null;

        outerloop: while (true) {
            int event = xmlPullParser.next();

            switch (event) {
                case START_TAG:
                    String name = xmlPullParser.getName();
                    switch (name) {
                        case "put": {
                            switch (version) {
                                case v0_2:
                                    String putUrlString = xmlPullParser.nextText();
                                    putUrl = new URL(putUrlString);
                                    break;
                                case v0_3:
                                    putElementV04Content = parsePutElement_V0_4(xmlPullParser);
                                    break;
                                default:
                                    throw new AssertionError();
                            }
                            break;
                        }
                        case "get":
                            String getUrlString;
                            switch (version) {
                                case v0_2:
                                    getUrlString = xmlPullParser.nextText();
                                    break;
                                case v0_3:
                                    getUrlString = xmlPullParser.getAttributeValue(null, "url");
                                    break;
                                default:
                                    throw new AssertionError();
                            }
                            getUrl = new URL(getUrlString);
                            break;
                    }
                    break;
                case END_TAG:
                    if (xmlPullParser.getDepth() == initialDepth) {
                        break outerloop;
                    }
                    break;
                default:
                    // Catch all for incomplete switch (MissingCasesInEnumSwitch) statement.
                    break;
            }
        }

        switch (version) {
            case v0_3:
                return new Slot(putElementV04Content.putUrl, getUrl, putElementV04Content.headers);
            case v0_2:
                return new Slot_V0_2(putUrl, getUrl);
            default:
                throw new AssertionError();
        }
    }

    public static PutElement_V0_4_Content parsePutElement_V0_4(XmlPullParser parser) throws XmlPullParserException, IOException {
        final int initialDepth = parser.getDepth();

        String putUrlString = parser.getAttributeValue(null, "url");
        URL putUrl = new URL(putUrlString);

        Map<String, String> headers = null;
        outerloop: while (true) {
            int next = parser.next();
            switch (next) {
            case START_TAG:
                String name = parser.getName();
                switch (name) {
                case "header":
                    String headerName = ParserUtils.getRequiredAttribute(parser, "name");
                    String headerValue = ParserUtils.getRequiredNextText(parser);
                    if (headers == null) {
                        headers = new HashMap<>();
                    }
                    headers.put(headerName, headerValue);
                    break;
                default:
                    break;
                }
                break;
            case END_TAG:
                if (parser.getDepth() == initialDepth) {
                    break outerloop;
                }
                break;
            default:
                // Catch all for incomplete switch (MissingCasesInEnumSwitch) statement.
                break;
            }
        }

        return new PutElement_V0_4_Content(putUrl, headers);
    }

    public static final class PutElement_V0_4_Content {
        private final URL putUrl;
        private final Map<String, String> headers;

        private PutElement_V0_4_Content(URL putUrl, Map<String, String> headers) {
            this.putUrl = putUrl;
            this.headers = headers;
        }

        public URL getPutUrl() {
            return putUrl;
        }

        public Map<String, String> getHeaders() {
            return headers;
        }
    }
}