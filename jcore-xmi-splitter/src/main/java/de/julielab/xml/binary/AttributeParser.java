package de.julielab.xml.binary;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class AttributeParser {
    public XmlStartTag parse(byte[] elementData) {
        final PosDisclosingByteArrayInputStream posBaos = new PosDisclosingByteArrayInputStream(elementData);
        final InputStreamReader r = new InputStreamReader(posBaos);
        int c;
        try {
            State s = State.START;
            int tagEnd = -1;
            int elementNameBegin = -1;
            int elementNameEnd = -1;
            int attrNameBegin = -1;
            int attrNameEnd = -1;
            int attrValBegin = -1;
            int attrValEnd = -1;
            int lastPos = -2;
            List<XmlAttribute> xmlAttributes = new ArrayList<>();
            int bytesRead = 0;
            while ((c = r.read()) != -1) {
                s = doStateTransition(c, s);

                bytesRead += Math.ceil(c / 256d);

                if (s == State.IN_ELEM_PREFIX && elementNameBegin == -1)
                    elementNameBegin = lastPos;
                if (s == State.ELEM_NAME_END && elementNameEnd == -1)
                    elementNameEnd = lastPos;
                if (s == State.IN_ATTR_NAME && attrNameBegin == -1)
                    attrNameBegin = lastPos;
                if (s == State.ATTR_NAME_END && attrNameEnd == -1)
                    attrNameEnd = lastPos;
                if (s == State.IN_ATTR_VALUE && attrValBegin == -1)
                    attrValBegin = bytesRead;
                if (s == State.ATTR_VALUE_END && attrValBegin != -1 && attrValEnd == -1)
                    attrValEnd = lastPos;
                if (s == State.TAG_END && tagEnd == -1)
                    tagEnd = bytesRead;

                lastPos = bytesRead;

                if (attrValEnd != -1) {
                    final XmlAttribute xmlAttribute = new XmlAttribute(attrNameBegin, attrNameEnd, attrValBegin, attrValEnd, elementData);
                    xmlAttributes.add(xmlAttribute);
                    attrNameBegin = -1;
                    attrNameEnd = -1;
                    attrValBegin = -1;
                    attrValEnd = -1;
                }

            }
            return new XmlStartTag(0, tagEnd, xmlAttributes, elementData);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private State doStateTransition(int c, State s) {
        if (c == '"' && s != State.IN_ATTR_VALUE)
            return State.IN_ATTR_VALUE;
        else if (c == '"')
            return State.ATTR_VALUE_END;
        if (!Character.isWhitespace(c) && c != '<') {
            if (s == State.START)
                return State.IN_ELEM_PREFIX;
            if (s == State.COLON_PASSED)
                return State.IN_ELEM_NAME;
            if (s == State.ELEM_NAME_END || s == State.ATTR_VALUE_END)
                return State.IN_ATTR_NAME;
        } else if (c != '<') {
            if (s == State.IN_ELEM_NAME)
                return State.ELEM_NAME_END;
        }
        if (c == ':' && s == State.IN_ELEM_PREFIX)
            return State.COLON_PASSED;
        if (c == '=' && s == State.IN_ATTR_NAME)
            return State.ATTR_NAME_END;
        if (c == '>' && s != State.IN_ATTR_VALUE)
            return State.TAG_END;
        return s;
    }

    private enum State {
        START, COLON_PASSED, IN_ELEM_PREFIX, IN_ELEM_NAME, ELEM_NAME_END, IN_ATTR_NAME, ATTR_NAME_END, ATTR_VALUE_END, IN_ATTR_VALUE, TAG_END
    }
}
