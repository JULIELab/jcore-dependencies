package dragon.config;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
/**
 * <p>Utility class for configuration which can parse XML document that contains application information </p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class ConfigUtil {
    protected ConfigureNode root;

    public ConfigUtil() {
        root = null;
    }

    public ConfigUtil(ConfigureNode root) {
        this.root = root;
    }

    public ConfigUtil(String configFile) {
        root=new BasicConfigureNode(configFile);
    }

    public ConfigureNode getConfigureNode(ConfigureNode curNode, String nodeType, int nodeID){
        ConfigureNode nextNode;

        if(curNode.getNodeType().equalsIgnoreCase(nodeType) && curNode.getNodeID()==nodeID)
            return curNode;

        //search child node;
        nextNode=curNode.getFirstChild();
        while(nextNode!=null){
            if(nextNode.getNodeType().equalsIgnoreCase(nodeType) && nextNode.getNodeID()==nodeID)
                return nextNode;
            nextNode=nextNode.getNextSibling();
        }

        //search parent node;
        nextNode=curNode.getParentNode();
        if(nextNode!=null){
            nextNode=nextNode.getFirstChild();
            while (nextNode != null) {
                if (nextNode.getNodeType().equalsIgnoreCase(nodeType) && nextNode.getNodeID() == nodeID)
                    return nextNode;
                nextNode = nextNode.getNextSibling();
            }
        }

        //search root node;
        if(root==null){
            nextNode=curNode.getParentNode();
            if(nextNode==null) //the current node is already the root node
                return null;
            while(nextNode!=null){
                root=nextNode;
                nextNode=nextNode.getParentNode();
            }
        }
        nextNode=root.getFirstChild();
        while (nextNode != null) {
            if (nextNode.getNodeType().equalsIgnoreCase(nodeType) && nextNode.getNodeID() == nodeID)
                return nextNode;
            nextNode = nextNode.getNextSibling();
        }
        return null;
    }

    public Object loadResource(ConfigureNode node){
        Class curClass, params[];
        Method method;
        Object[] objParams;
        String shortClassName, methodName;
        int modifierCode;

        try{
            curClass = node.getNodeClass();
            if (curClass == null)
                return null;
            shortClassName = curClass.getName();
            if (shortClassName.lastIndexOf('.') > 0)
                shortClassName = shortClassName.substring(shortClassName.lastIndexOf('.') + 1);
            methodName = "get" + shortClassName;
            params = new Class[1];
            params[0] = Class.forName("dragon.config.ConfigureNode");
            method=curClass.getMethod(methodName, params);
            if(method==null){
                System.out.println("Please define the method in class "+curClass.getName()+": public static "+shortClassName
                                   +" "+methodName+"(ConfigureNode node)");
                return null;
            }
            modifierCode=method.getModifiers();
            if(!Modifier.isPublic(modifierCode) || !Modifier.isStatic(modifierCode)){
                System.out.println("The method "+methodName+" should be defined as public and static");
                return null;
            }
            objParams=new Object[1];
            objParams[0]=node;
            return method.invoke(curClass,objParams);
        }
        catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }
}