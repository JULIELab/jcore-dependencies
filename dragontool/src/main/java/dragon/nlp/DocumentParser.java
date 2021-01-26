package dragon.nlp;

import java.util.ArrayList;

/**
 * <p>Parse a text document into paragraphs, sentences and words</p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public interface DocumentParser {
    /**
     * Parses a text into a Document object
     * @param content the text for parsing
     * @return a document object
     */
    public Document parse(String content);

    /**
     * Parses a text into a Paragraph object
     * @param content the text for parsing
     * @return a paragraph object
     */
    public Paragraph parseParagraph(String content);

    /**
     * Parses a text into a Sentence object
     * @param content the text for parsing
     * @return a Sentence object
     */
    public Sentence parseSentence(String content);

    /**
     * Prases a text into a sequence of tokens. Tokens are in the original format and order. No stem is applied.
     * Duplicated tokens are not merged.
     * @param content the text for parsing
     * @return a list of tokens in the format of String
     */
    public ArrayList parseTokens(String content);
}