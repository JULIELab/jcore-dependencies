package dragon.config;

import dragon.nlp.DocumentParser;
import dragon.nlp.extract.EngDocumentParser;

/**
 * <p>Document parser configuration</p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class DocumentParserConfig extends ConfigUtil{
    public DocumentParserConfig() {
       super();
    }

    public DocumentParserConfig(ConfigureNode root){
       super(root);
    }

    public DocumentParserConfig(String configFile){
        super(configFile);
    }

    public DocumentParser getDocumentParser(int parserID){
        return getDocumentParser(root,parserID);
    }

    public DocumentParser getDocumentParser(ConfigureNode node, int parserID){
        return loadDocumentParser(node,parserID);
    }

    private DocumentParser loadDocumentParser(ConfigureNode node, int parserID){
        ConfigureNode parserNode;
        String parserName;

        parserNode=getConfigureNode(node,"documentparser",parserID);
        if(parserNode==null)
            return null;
        parserName=parserNode.getNodeName();
        return loadDocumentParser(parserName,parserNode);
    }

    protected DocumentParser loadDocumentParser(String parserName, ConfigureNode parserNode){
        if(parserName.equalsIgnoreCase("EngDocumentParser"))
            return loadEngDocumentParser(parserNode);
        else
            return (DocumentParser)loadResource(parserNode);
    }

    private DocumentParser loadEngDocumentParser(ConfigureNode node){
        return new EngDocumentParser(getWordDelimitor(node.getString("notworddelimitor","")));
    }

    private String getWordDelimitor(String notWordDelimitor){
        String delimitors;
        StringBuffer sb;
        int i;

        sb=new StringBuffer();
        delimitors=EngDocumentParser.defWordDelimitor;
        if(notWordDelimitor==null && notWordDelimitor.length()==0)
            return delimitors;
        for(i=0;i<delimitors.length();i++){
            if(notWordDelimitor.indexOf(delimitors.charAt(i))<0)
                sb.append(delimitors.charAt(i));
        }
        return sb.toString();
    }
}
