package dragon.nlp.ontology.mesh;

import java.io.*;
import java.util.*;
import dragon.util.*;

/**
 * <p>List of mesh nodes</p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2006</p>
 * <p>Company: Drexel University</p>
 * @author Xiaodan Zhang
 * @version 1.0
 */

public class MeshNodeList extends SortedArray {
	private static final long serialVersionUID = 1L;

	public MeshNodeList(String fileName) {
        loadMeshNodeList(fileName);
    }

    public MeshNode lookup(String path) {
        int pos;

        pos = binarySearch(new MeshNode(path));
        if (pos < 0) {
            return null;
        } else {
            return (MeshNode) get(pos);
        }
    }

    public MeshNode lookup(MeshNode node) {
        int pos;

        pos = binarySearch(node.getPath());
        if (pos < 0) {
            return null;
        } else {
            return (MeshNode) get(pos);
        }
    }

    private boolean loadMeshNodeList(String filename) {
        MeshNode cur, parent;
        BufferedReader br;
        String line, path;
        String[] arrField;
        int i, total, startPos;
        ArrayList list;

        try {
            br = FileUtil.getTextReader(filename);
            line = br.readLine();
            total = Integer.parseInt(line);
            list = new ArrayList(total);

            for (i = 0; i < total; i++) {
                line = br.readLine();
                arrField = line.split(";");
                cur = new MeshNode(arrField[0], arrField[1]);
                list.add(cur);
            }
            br.close();
            Collections.sort(list);
            this.addAll(list);

            //calculate the number of descendants for each node
            for(i=0;i<list.size();i++){
                cur=(MeshNode)list.get(i);
                path=cur.getPath();
                startPos=path.indexOf('.');
                while(startPos>=0){
                    parent=lookup(path.substring(0,startPos));
                    if(parent!=null)
                        parent.setDescendantNum(parent.getDescendantNum()+1);
                    startPos=path.indexOf('.',startPos+1);
                }
            }
            return true;
        }
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}