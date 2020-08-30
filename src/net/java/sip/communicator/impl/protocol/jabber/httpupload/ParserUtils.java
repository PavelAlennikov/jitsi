package net.java.sip.communicator.impl.protocol.jabber.httpupload;

import org.xmlpull.v1.*;

import java.io.*;
import java.util.*;

public class ParserUtils {

    /**
     * The constant String "jid".
     */
    public static final String JID = "jid";

    public static void assertAtStartTag(XmlPullParser parser) throws XmlPullParserException {
        assert parser.getEventType() == XmlPullParser.START_TAG;
    }

    public static void assertAtStartTag(XmlPullParser parser, String name) throws XmlPullParserException {
        assertAtStartTag(parser);
        assert name.equals(parser.getName());
    }

    public static void assertAtEndTag(XmlPullParser parser) throws XmlPullParserException {
        assert parser.getEventType() == XmlPullParser.END_TAG;
    }

    public static void forwardToStartElement(XmlPullParser parser) throws XmlPullParserException, IOException {
         // Wind the parser forward to the first start tag
        int event = parser.getEventType();
        while (event != XmlPullParser.START_TAG) {
            if (event == XmlPullParser.END_DOCUMENT) {
                throw new IllegalArgumentException("Document contains no start tag");
            }
            event = parser.next();
        }
    }

    public static void forwardToEndTagOfDepth(XmlPullParser parser, int depth)
                    throws XmlPullParserException, IOException {
        int event = parser.getEventType();
        while (!(event == XmlPullParser.END_TAG && parser.getDepth() == depth)) {
            event = parser.next();
        }
    }

    /**
     * Prase a string to a boolean value as per "xs:boolean". Valid input strings are "true", "1" for true, and "false", "0" for false.
     *
     * @param booleanString the input string.
     * @return the boolean representation of the input string
     * @throws IllegalArgumentException if the input string is not valid.
     * @since 4.3.2
     */
    public static boolean parseXmlBoolean(String booleanString) {
        switch (booleanString) {
        case "true":
        case "1":
            return true;
        case "false":
        case "0":
            return false;
        default:
            throw new IllegalArgumentException(booleanString + " is not a valid boolean string");
        }
    }

    /**
     * Get the boolean value of an argument.
     *
     * @param parser TODO javadoc me please
     * @param name TODO javadoc me please
     * @return the boolean value or null of no argument of the given name exists
     */
    public static Boolean getBooleanAttribute(XmlPullParser parser, String name) {
        String valueString = parser.getAttributeValue("", name);
        if (valueString == null)
            return null;
        valueString = valueString.toLowerCase(Locale.US);
        return parseXmlBoolean(valueString);
    }

    public static boolean getBooleanAttribute(XmlPullParser parser, String name,
                    boolean defaultValue) {
        Boolean bool = getBooleanAttribute(parser, name);
        if (bool == null) {
            return defaultValue;
        }
        else {
            return bool;
        }
    }

    public static Byte getByteAttributeFromNextText(XmlPullParser parser) throws IOException, XmlPullParserException {
        String nextText = parser.nextText();
        return Byte.valueOf(nextText);
    }

    public static int getIntegerAttributeOrThrow(XmlPullParser parser, String name, String throwMessage)
                    throws IOException {
        Integer res = getIntegerAttribute(parser, name);
        if (res == null) {
            // TODO Should be SmackParseException.
            throw new IOException(throwMessage);
        }
        return res;
    }

    public static Integer getIntegerAttribute(XmlPullParser parser, String name) {
        String valueString = parser.getAttributeValue("", name);
        if (valueString == null)
            return null;
        return Integer.valueOf(valueString);
    }

    public static int getIntegerAttribute(XmlPullParser parser, String name, int defaultValue) {
        Integer integer = getIntegerAttribute(parser, name);
        if (integer == null) {
            return defaultValue;
        }
        else {
            return integer;
        }
    }

    public static int getIntegerFromNextText(XmlPullParser parser) throws XmlPullParserException, IOException {
        String intString = parser.nextText();
        return Integer.valueOf(intString);
    }

    public static Long getLongAttribute(XmlPullParser parser, String name) {
        String valueString = parser.getAttributeValue("", name);
        if (valueString == null)
            return null;
        return Long.valueOf(valueString);
    }

    public static long getLongAttribute(XmlPullParser parser, String name, long defaultValue) {
        Long l = getLongAttribute(parser, name);
        if (l == null) {
            return defaultValue;
        }
        else {
            return l;
        }
    }

    public static double getDoubleFromNextText(XmlPullParser parser) throws XmlPullParserException, IOException {
        String doubleString = parser.nextText();
        return Double.valueOf(doubleString);
    }

    public static Double getDoubleAttribute(XmlPullParser parser, String name) {
        String valueString = parser.getAttributeValue("", name);
        if (valueString == null)
            return null;
        return Double.valueOf(valueString);
    }

    public static double getDoubleAttribute(XmlPullParser parser, String name, long defaultValue) {
        Double d = getDoubleAttribute(parser, name);
        if (d == null) {
            return defaultValue;
        }
        else {
            return d;
        }
    }

    public static Short getShortAttribute(XmlPullParser parser, String name) {
        String valueString = parser.getAttributeValue("", name);
        if (valueString == null) {
            return null;
        }
        return Short.valueOf(valueString);
    }

    public static short getShortAttribute(XmlPullParser parser, String name, short defaultValue) {
        Short s = getShortAttribute(parser, name);
        if (s == null) {
            return defaultValue;
        }
        return s;
    }

    public static String getRequiredAttribute(XmlPullParser parser, String name) throws IOException {
        String value = parser.getAttributeValue("", name);
        if (StringUtils.isNullOrEmpty(value)) {
            throw new IOException("Attribute " + name + " is null or empty (" + value + ')');
        }
        return value;
    }

    public static String getRequiredNextText(XmlPullParser parser) throws XmlPullParserException, IOException {
        String text = parser.nextText();
        if (StringUtils.isNullOrEmpty(text)) {
            throw new IOException("Next text is null or empty (" + text + ')');
        }
        return text;
    }
}