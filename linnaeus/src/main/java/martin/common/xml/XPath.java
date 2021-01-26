package martin.common.xml;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;

public class XPath {
	public static Node getNode(String query, Node root){
		String[] nodesToFind = query.split("/");
		String nodeToFind = nodesToFind[0];
		
		NodeList nodes = root.getChildNodes();
		
		for (int i = 0; i < nodes.getLength(); i++)
			if (nodes.item(i).getNodeName().equals(nodeToFind)){
				if (nodesToFind.length > 1)
					return getNode(query.substring(query.indexOf("/")+1), nodes.item(i));
				else
					return nodes.item(i);
			}
				
		//throw new IllegalStateException("The query " + query + " could not be found at node with name " + root.getNodeName());
		return null;
	}
	
	public static ArrayList<Node> getArrayNodeList(String query, Node root){
		String[] nodesToFind = query.split("/");
		String nodeToFind = nodesToFind[0];
		
		NodeList nodes = root.getChildNodes();
		
		ArrayList<Node> nodeList = new ArrayList<Node>();
		
		for (int i = 0; i < nodes.getLength(); i++)
			if (nodes.item(i).getNodeName().equals(nodeToFind)){
				if (nodesToFind.length > 1)
					nodeList.addAll(getArrayNodeList(query.substring(query.indexOf("/")+1), nodes.item(i)));
				else
					nodeList.add(nodes.item(i));
			}
				
		return nodeList;
	}
	
	public static MyNodeList getNodeList(String query, Node root){
		return new MyNodeList(getArrayNodeList(query, root));
	}
}
