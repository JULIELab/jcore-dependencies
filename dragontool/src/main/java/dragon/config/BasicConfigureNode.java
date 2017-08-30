package dragon.config;

import java.io.File;
import javax.xml.parsers.*;
import org.w3c.dom.*;

/**
 * <p>Basic data structure for XML configure node </p>
 * <p> </p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class BasicConfigureNode implements ConfigureNode{
    private Node node;

    public BasicConfigureNode(String configFile) {
        DocumentBuilder parser;
        DocumentBuilderFactory factory;
        Document doc;

        try{
            factory=DocumentBuilderFactory.newInstance();
            parser = factory.newDocumentBuilder();
            doc=parser.parse(new File(configFile));
            node=doc.getDocumentElement();
        }
        catch(Exception e)
        {
            e.printStackTrace();
            node=null;
        }
    }

    public BasicConfigureNode(Node node){
        if(node.getNodeName().equalsIgnoreCase("param"))
            this.node=null;
        else
            this.node=node;
    }

    public ConfigureNode getParentNode(){
        if(node.getParentNode()==null)
            return null;
        else if(node.getParentNode().getParentNode()==null)
            return null;
        else
            return new BasicConfigureNode(node.getParentNode());
    }

    public ConfigureNode getFirstChild(){
        Node curNode;

        curNode=node.getFirstChild();
        while(curNode!=null){
            if(curNode.getNodeName().equalsIgnoreCase("param"))
                curNode=curNode.getNextSibling();
            else
                return new BasicConfigureNode(curNode);
        }
        return null;
    }

    public ConfigureNode getNextSibling(){
        Node curNode;

        curNode=node.getNextSibling();
        while(curNode!=null){
            if(curNode.getNodeName().equalsIgnoreCase("param"))
                curNode=curNode.getNextSibling();
            else
                return new BasicConfigureNode(curNode);
        }
        return null;
    }

    public String getNodeName(){
        return node.getNodeName();
    }

    public int getNodeID(){
        Node curNode;

        if(!node.hasAttributes())
            return -1;
        curNode=node.getAttributes().getNamedItem("id");
        if(curNode==null)
            return -1;
        return Integer.parseInt(curNode.getNodeValue());
    }

    public Class getNodeClass(){
        String className;

        className=null;
        try{
            Node curNode;

            if (!node.hasAttributes()){
                System.out.println("Please specify the class name the resource corresponds to in the configuration flie!");
                return null;
            }
            curNode = node.getAttributes().getNamedItem("class");
            if (curNode == null){
                System.out.println("Please specify the class name the resource corresponds to in the configuration flie!");
                return null;
            }
            className=curNode.getNodeValue();
            return Class.forName(className);
        }
        catch(Exception e){
            if(className!=null){
                System.out.println("Can't load the class: "+className);
            }
            return null;
        }
    }

    public String getNodeType(){
        Node curNode;

        if(!node.hasAttributes())
            return "";
        curNode=node.getAttributes().getNamedItem("type");
        if(curNode==null)
            return "";
        return curNode.getNodeValue();
    }

    public String getString(String key){
        return getParam(key);
    }

    public String getString(String key, String def){
        String ret;

        ret=getParam(key);
        if(ret==null)
            return def;
        else
            return ret;
    }

    public int getInt(String key){
        String ret;

        ret=getParam(key);
        if(ret==null)
            return 0;
        else
            return Integer.parseInt(ret);
    }

    public int getInt(String key, int def){
        String ret;

        ret=getParam(key);
        if(ret==null)
            return def;
        else
            return Integer.parseInt(ret);
    }

    public boolean getBoolean(String key){
        String ret;

        ret=getParam(key);
        if(ret==null)
            return false;
        else
            return Boolean.getBoolean(ret);
    }

    public boolean getBoolean(String key, boolean def){
        String ret;

        ret=getParam(key);
        if(ret==null)
            return def;
        else
            return (new Boolean(ret).booleanValue());
    }

    public double getDouble(String key){
        String ret;

        ret=getParam(key);
        if(ret==null)
            return 0;
        else
            return Double.parseDouble(ret);

    }

    public double getDouble(String key, double def){
        String ret;

        ret=getParam(key);
        if(ret==null)
            return def;
        else
            return Double.parseDouble(ret);
    }

    public boolean exist(String key){
        return getParam(key)!=null;
    }

    private String getParam(String key){
        Node curNode, attribute;

        curNode=node.getFirstChild();
        while(curNode!=null){
            if(curNode.getNodeName().equalsIgnoreCase("param")){
                if(curNode.hasAttributes()){
                    attribute=curNode.getAttributes().getNamedItem("name");
                    if(attribute!=null && attribute.getNodeValue().equalsIgnoreCase(key)){
                        attribute=curNode.getAttributes().getNamedItem("value");
                        if(attribute==null)
                            return null;
                        else
                            return attribute.getNodeValue();
                    }
                }
            }
            curNode=curNode.getNextSibling();
        }
        return null;
    }

    public String getParameterType(String key){
        Node curNode, attribute;

        curNode=node.getFirstChild();
        while(curNode!=null){
            if(curNode.getNodeName().equalsIgnoreCase("param")){
                if(curNode.hasAttributes()){
                    attribute=curNode.getAttributes().getNamedItem("name");
                    if(attribute!=null && attribute.getNodeValue().equalsIgnoreCase(key)){
                        attribute=curNode.getAttributes().getNamedItem("type");
                        if(attribute==null)
                            return null;
                        else
                            return attribute.getNodeValue();
                    }
                }
            }
            curNode=curNode.getNextSibling();
        }
        return null;
    }
}