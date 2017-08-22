package martin.common.xml;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MyNodeList implements NodeList, Iterable<Node> {
	private List<Node> list;

	public MyNodeList(List<Node> list){
		this.list = list;
	}
	
	public MyNodeList(NodeList childNodes) {
		this.list = new ArrayList<Node>();
		
		for (int i = 0; i < childNodes.getLength(); i++)
			list.add(childNodes.item(i));
	}

	public int getLength() {
		return list.size();
	}

	public Node item(int index) {
		return list.get(index);
	}

	public Iterator<Node> iterator() {
		return list.iterator();
	}
}
