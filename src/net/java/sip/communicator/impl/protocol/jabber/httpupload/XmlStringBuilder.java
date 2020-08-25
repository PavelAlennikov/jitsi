package net.java.sip.communicator.impl.protocol.jabber.httpupload;


import java.io.*;

public class XmlStringBuilder implements Appendable, CharSequence {
    public static final String RIGHT_ANGLE_BRACKET = Character.toString('>');

    private final LazyStringBuilder sb;

    public XmlStringBuilder() {
        sb = new LazyStringBuilder();
    }

    public XmlStringBuilder escapedElement(String name, String escapedContent) {
        assert escapedContent != null;
        openElement(name);
        append(escapedContent);
        closeElement(name);
        return this;
    }

    /**
     * Add a new element to this builder.
     *
     * @param name    TODO javadoc me please
     * @param content TODO javadoc me please
     * @return the XmlStringBuilder
     */
    public XmlStringBuilder element(String name, String content) {
        if (content.isEmpty()) {
            return emptyElement(name);
        }
        openElement(name);
        escape(content);
        closeElement(name);
        return this;
    }

    /**
     * Add a new element to this builder.
     *
     * @param name    TODO javadoc me please
     * @param content TODO javadoc me please
     * @return the XmlStringBuilder
     */
    public XmlStringBuilder element(String name, CharSequence content) {
        return element(name, content.toString());
    }

    public XmlStringBuilder element(String name, Enum<?> content) {
        assert content != null;
        element(name, content.toString());
        return this;
    }

    public XmlStringBuilder optElement(String name, String content) {
        if (content != null) {
            element(name, content);
        }
        return this;
    }

    public XmlStringBuilder optElement(String name, CharSequence content) {
        if (content != null) {
            element(name, content.toString());
        }
        return this;
    }

    public XmlStringBuilder optElement(String name, Enum<?> content) {
        if (content != null) {
            element(name, content);
        }
        return this;
    }

    public XmlStringBuilder optElement(String name, Object object) {
        if (object != null) {
            element(name, object.toString());
        }
        return this;
    }

    public XmlStringBuilder optIntElement(String name, int value) {
        if (value >= 0) {
            element(name, String.valueOf(value));
        }
        return this;
    }

    public XmlStringBuilder halfOpenElement(String name) {
        assert StringUtils.isNotEmpty(name);
        sb.append('<').append(name);
        return this;
    }

    public XmlStringBuilder openElement(String name) {
        halfOpenElement(name).rightAngleBracket();
        return this;
    }

    public XmlStringBuilder closeElement(String name) {
        sb.append("</").append(name);
        rightAngleBracket();
        return this;
    }

    public XmlStringBuilder closeEmptyElement() {
        sb.append("/>");
        return this;
    }

    /**
     * Add a right angle bracket '&gt;'.
     *
     * @return a reference to this object.
     */
    public XmlStringBuilder rightAngleBracket() {
        sb.append(RIGHT_ANGLE_BRACKET);
        return this;
    }

    /**
     * Does nothing if value is null.
     *
     * @param name  TODO javadoc me please
     * @param value TODO javadoc me please
     * @return the XmlStringBuilder
     */
    public XmlStringBuilder attribute(String name, String value) {
        assert value != null;
        sb.append(' ').append(name).append("='");
        escapeAttributeValue(value);
        sb.append('\'');
        return this;
    }

    public XmlStringBuilder attribute(String name, boolean bool) {
        return attribute(name, Boolean.toString(bool));
    }

    public XmlStringBuilder attribute(String name, CharSequence value) {
        return attribute(name, value.toString());
    }

    public XmlStringBuilder attribute(String name, Enum<?> value) {
        assert value != null;
        // TODO: Should use toString() instead of name().
        attribute(name, value.name());
        return this;
    }

    public <E extends Enum<?>> XmlStringBuilder attribute(String name, E value, E implicitDefault) {
        if (value == null || value == implicitDefault) {
            return this;
        }

        attribute(name, value.toString());
        return this;
    }

    public XmlStringBuilder xmlnsAttribute(String value) {
        XmlNsAttribute xmlNsAttribute = new XmlNsAttribute(value);
        append(xmlNsAttribute);
        return this;
    }

    public XmlStringBuilder attribute(String name, int value) {
        assert name != null;
        return attribute(name, String.valueOf(value));
    }

    public XmlStringBuilder attribute(String name, long value) {
        assert name != null;
        return attribute(name, String.valueOf(value));
    }

    public XmlStringBuilder optAttribute(String name, String value) {
        if (value != null) {
            attribute(name, value);
        }
        return this;
    }

    public XmlStringBuilder optAttribute(String name, Long value) {
        if (value != null) {
            attribute(name, value);
        }
        return this;
    }

    public XmlStringBuilder optAttribute(String name, CharSequence value) {
        if (value != null) {
            attribute(name, value.toString());
        }
        return this;
    }

    public XmlStringBuilder optAttribute(String name, Enum<?> value) {
        if (value != null) {
            attribute(name, value.toString());
        }
        return this;
    }

    public XmlStringBuilder optAttribute(String name, Number number) {
        if (number != null) {
            attribute(name, number.toString());
        }
        return this;
    }

    /**
     * Add the given attribute if {@code value => 0}.
     *
     * @param name  TODO javadoc me please
     * @param value TODO javadoc me please
     * @return a reference to this object
     */
    public XmlStringBuilder optIntAttribute(String name, int value) {
        if (value >= 0) {
            attribute(name, Integer.toString(value));
        }
        return this;
    }

    /**
     * Add the given attribute if value not null and {@code value => 0}.
     *
     * @param name  TODO javadoc me please
     * @param value TODO javadoc me please
     * @return a reference to this object
     */
    public XmlStringBuilder optLongAttribute(String name, Long value) {
        if (value != null && value >= 0) {
            attribute(name, Long.toString(value));
        }
        return this;
    }

    public XmlStringBuilder optBooleanAttribute(String name, boolean bool) {
        if (bool) {
            sb.append(' ').append(name).append("='true'");
        }
        return this;
    }

    public XmlStringBuilder optBooleanAttributeDefaultTrue(String name, boolean bool) {
        if (!bool) {
            sb.append(' ').append(name).append("='false'");
        }
        return this;
    }

    private static final class XmlNsAttribute implements CharSequence {
        private final String value;
        private final String xmlFragment;

        private XmlNsAttribute(String value) {
            this.value = StringUtils.requireNotNullNorEmpty(value, "Value must not be null");
            this.xmlFragment = " xmlns='" + value + '\'';
        }

        @Override
        public String toString() {
            return xmlFragment;
        }

        @Override
        public int length() {
            return xmlFragment.length();
        }

        @Override
        public char charAt(int index) {
            return xmlFragment.charAt(index);
        }

        @Override
        public CharSequence subSequence(int start, int end) {
            return xmlFragment.subSequence(start, end);
        }
    }

    public XmlStringBuilder xmllangAttribute(String value) {
        // TODO: This should probably be attribute(), not optAttribute().
        optAttribute("xml:lang", value);
        return this;
    }

    public XmlStringBuilder optXmlLangAttribute(String lang) {
        if (!StringUtils.isNullOrEmpty(lang)) {
            xmllangAttribute(lang);
        }
        return this;
    }

    public XmlStringBuilder escape(String text) {
        assert text != null;
        sb.append(StringUtils.escapeForXml(text));
        return this;
    }

    public XmlStringBuilder escapeAttributeValue(String value) {
        assert value != null;
        sb.append(StringUtils.escapeForXmlAttributeApos(value));
        return this;
    }

    public XmlStringBuilder optEscape(CharSequence text) {
        if (text == null) {
            return this;
        }
        return escape(text);
    }

    public XmlStringBuilder escape(CharSequence text) {
        return escape(text.toString());
    }


    public XmlStringBuilder append(XmlStringBuilder xsb) {
        assert xsb != null;
        sb.append(xsb.sb);
        return this;
    }

    public XmlStringBuilder emptyElement(Enum<?> element) {
        // Use Enum.toString() instead Enum.name() here, since some enums override toString() in order to replace
        // underscores ('_') with dash ('-') for example (name() is declared final in Enum).
        return emptyElement(element.toString());
    }

    public XmlStringBuilder emptyElement(String element) {
        halfOpenElement(element);
        return closeEmptyElement();
    }

    public XmlStringBuilder condEmptyElement(boolean condition, String element) {
        if (condition) {
            emptyElement(element);
        }
        return this;
    }

    public XmlStringBuilder condAttribute(boolean condition, String name, String value) {
        if (condition) {
            attribute(name, value);
        }
        return this;
    }

    @Override
    public XmlStringBuilder append(CharSequence csq) {
        assert csq != null;
        sb.append(csq);
        return this;
    }

    @Override
    public XmlStringBuilder append(CharSequence csq, int start, int end) {
        assert csq != null;
        sb.append(csq, start, end);
        return this;
    }

    @Override
    public XmlStringBuilder append(char c) {
        sb.append(c);
        return this;
    }

    @Override
    public int length() {
        return sb.length();
    }

    @Override
    public char charAt(int index) {
        return sb.charAt(index);
    }

    @Override
    public CharSequence subSequence(int start, int end) {
        return sb.subSequence(start, end);
    }

    @Override
    public String toString() {
        return sb.toString();
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof CharSequence)) {
            return false;
        }
        CharSequence otherCharSequenceBuilder = (CharSequence) other;
        return toString().equals(otherCharSequenceBuilder.toString());
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    private static final class WrappedIoException extends RuntimeException {

        private static final long serialVersionUID = 1L;

        private final IOException wrappedIoException;

        private WrappedIoException(IOException wrappedIoException) {
            this.wrappedIoException = wrappedIoException;
        }
    }

}
