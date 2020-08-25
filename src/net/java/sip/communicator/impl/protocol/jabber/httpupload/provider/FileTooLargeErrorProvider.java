package net.java.sip.communicator.impl.protocol.jabber.httpupload.provider;

import net.java.sip.communicator.impl.protocol.jabber.httpupload.element.*;
import org.jivesoftware.smack.packet.*;
import org.jivesoftware.smack.provider.*;
import org.xmlpull.v1.*;

import static org.xmlpull.v1.XmlPullParser.END_TAG;
import static org.xmlpull.v1.XmlPullParser.START_TAG;

public class FileTooLargeErrorProvider implements PacketExtensionProvider {

    @Override
    public PacketExtension parseExtension(XmlPullParser parser) throws Exception {
        int initialDepth = parser.getDepth();
        final String namespace = parser.getNamespace();
        long maxFileSize = 0;

        outerloop: while (true) {
            int event = parser.next();

            switch (event) {
                case START_TAG:
                    String name = parser.getName();
                    switch (name) {
                        case "max-file-size":
                            maxFileSize = Long.parseLong(parser.nextText());
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

        switch (namespace) {
        case FileTooLargeError.NAMESPACE:
            return new FileTooLargeError(maxFileSize);
        case FileTooLargeError_V0_2.NAMESPACE:
            return new FileTooLargeError_V0_2(maxFileSize);
        default:
            throw new AssertionError();
        }
    }
}