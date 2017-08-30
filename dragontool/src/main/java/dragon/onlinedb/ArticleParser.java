package dragon.onlinedb;

/**
 * <p>Interface of Article Parser</p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public interface ArticleParser {
    /**
     * Parse a sequence of text into an article
     * @param content the sequence of text
     * @return a parsed article
     */
    public Article parse(String content);

    /**
     * Assemble an article into a sequence of text which could be saved in files for future use.
     * @param article the article for assembling
     * @return a sequence of text
     */
    public String assemble(Article article);
}