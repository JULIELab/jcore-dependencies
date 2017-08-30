package dragon.onlinedb;

/**
 * <p>Basic Parser for parsing and assembling a given article</p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class BasicArticleParser implements ArticleParser{
    public Article parse(String line){
        Article cur;
        String arrField[];

        try {
            if (line == null || line.trim().length() == 0)
                return null;
            arrField = line.split("\t");
            cur = new BasicArticle();
            cur.setKey(arrField[0]);
            if(arrField.length>=2)
                cur.setTitle(arrField[1]);
            if (arrField.length >= 3)
                cur.setMeta(arrField[2]);
            if (arrField.length >= 4)
                cur.setAbstract(arrField[3]);
            if (arrField.length >= 5)
                cur.setBody(arrField[4]);
            if (arrField.length >= 6)
                cur.setCategory(Integer.parseInt(arrField[5]));
            else
                cur.setCategory(-1);
            return cur;
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public String assemble(Article article){
        StringBuffer sb;

        try{
            sb=new StringBuffer(10240);
            sb.append(article.getKey());
            sb.append('\t');
            if (article.getTitle() != null)
                sb.append(processText(article.getTitle()));
            sb.append('\t');
            if (article.getMeta() != null)
                sb.append(processText(article.getMeta()));
            sb.append('\t');
            if (article.getAbstract() != null)
                sb.append(processText(article.getAbstract()));
            sb.append('\t');
            if (article.getBody() != null)
                sb.append(processText(article.getBody()));
            if(article.getCategory()>=0){
                sb.append('\t');
                 sb.append(article.getCategory());
            }
            return sb.toString();
        }
        catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }

    protected String processText(String text){
        text = text.replace('\t', ' ');
        text = text.replace('\r', ' ');
        return text.replace('\n', ' ');
    }
}