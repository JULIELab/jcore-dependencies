package edu.umass.cs.mallet.projects.seg_plus_coref.clustering;

import edu.umass.cs.mallet.base.types.Instance;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;

public class CitationCluster extends LinkedList //LinkedHashSet
{
	Object canonicalObj = null;

	String refNoMeta;
	String clusterNoMeta;

        String[] startTags;
        String[] endTags;
        double[] tagWeight;


	public CitationCluster(String refNoMeta, String clusterNoMeta, String[] startTags, String[] endTags, double[] tagWeight)
	{
		this.refNoMeta = refNoMeta;
		this.clusterNoMeta = clusterNoMeta;

		this.startTags = startTags;
		this.endTags = endTags;
		this.tagWeight = tagWeight;

	}

	public CitationCluster(Instance instance,
			String refNoMeta, String clusterNoMeta, String[] startTags, String[] endTags, double[] tagWeight)
	{
	//	this((CharSequence)instance.getSource());
		this.add(instance);
		this.canonicalObj = instance;

		this.refNoMeta = refNoMeta;
		this.clusterNoMeta = clusterNoMeta;

		this.startTags = startTags;
		this.endTags = endTags;
		this.tagWeight = tagWeight;

	}

	public CitationCluster(CharSequence instance,
			String refNoMeta, String clusterNoMeta, String[] startTags, String[] endTags, double[] tagWeight)
	{
		this.add(instance);
		this.canonicalObj = instance;

		this.refNoMeta = refNoMeta;
		this.clusterNoMeta = clusterNoMeta;

		this.startTags = startTags;
		this.endTags = endTags;
		this.tagWeight = tagWeight;

	}

	public boolean setCanonicalObj(Object newCanonicalObj){

		canonicalObj = newCanonicalObj;

		return true;
	}
	
	public Object getCanonicalObj(){
		return canonicalObj;
	}

	public Object updateCononicalObj(Instance instance)
	{
		//longest match
/*		String newSource = "";
		for(int i=0; i<startTags.length; i++){
			String field_instance = (String)instance.getProperty(startTags[i]);
			String field_canonical= (String)((Instance)canonicalObj).getProperty(startTags[i]);
//			System.out.println(field_instance + " : " + field_canonical);

			if(field_instance.length() > field_canonical.length()) 
				((Instance)canonicalObj).setProperty(startTags[i], field_instance);

			newSource += startTags[i];
			newSource += ((Instance)canonicalObj).getProperty(startTags[i]);
			newSource += endTags[i];

		}

*/


/*		//majority vote
		String newSource = "";
		HashMap[] m = new HashMap[startTags.length];
		for(int i=0; i<startTags.length; i++){
			m[i] = new HashMap();
		}

		Iterator iter = this.iterator();
		while (iter.hasNext()) {
			Instance inst = (Instance)iter.next();

			for(int i=0; i<startTags.length; i++){	
				String attribute = (String)inst.getProperty(startTags[i]);
				if(m[i].containsKey(attribute)){
					int value = ((Integer)m[i].get(attribute)).intValue() + 1;
					m[i].put(attribute, new Integer(value));
				}
				else{
					m[i].put(attribute, new Integer(0) );
				}
			}
		}

		for(int i=0; i<startTags.length; i++){	
			Set keys = m[i].keySet();
			iter = keys.iterator();
			Integer count = new Integer(0);
			String ss="";
			while(iter.hasNext()){
				String key = (String) iter.next();
				Integer cc = (Integer)m[i].get(key);	
	//			System.out.println(key + ":" + cc);
				if( cc.compareTo(count) > 0 ){
					count = cc;
					ss = key;	
				}
			}	

			((Instance)canonicalObj).setProperty(startTags[i], ss);	

			newSource += startTags[i];
			newSource += ((Instance)canonicalObj).getProperty(startTags[i]);
			newSource += endTags[i];
		}

	
		if(( (Instance)canonicalObj).isLocked() ){
			((Instance)canonicalObj).unLock();
		}

		((Instance)canonicalObj).setSource(newSource);
*/
		return canonicalObj;
	}

	public Object updateCononicalObj(CharSequence instance){
		
		return canonicalObj;
	}

	protected ArrayList print()
	{
		int totalInstance = 0;
		ArrayList clusterResponse = new ArrayList();
		if(canonicalObj instanceof CharSequence || canonicalObj instanceof Instance ){
//			print((CharSequence)canonicalObj);

			print_canonical();
		
			Iterator i = this.iterator();
			while (i.hasNext()) {
				Object instance = i.next();
//				CharSequence instance = (CharSequence)i.next();
				if(instance instanceof CharSequence) print((CharSequence)instance);
				else if(instance instanceof Instance){
					 print((Instance)instance);
					 clusterResponse.add(((Instance)instance).getProperty(clusterNoMeta));
				}

				totalInstance ++;
			}
		}
		else{
			throw new UnsupportedOperationException ("not supported type\n");
		}

//		return totalInstance;
		return clusterResponse;

	}

	protected void print(CharSequence instance)
	{
		System.out.println((CharSequence)instance);
	}

	protected void print(Instance instance)
	{
//		String refNo = (String)instance.getProperty("REFNO");
//		String clusterNo = (String)instance.getProperty("CLUSTERNO");

//		System.out.println(" reference no: =" + refNo + "/" + "cluster no: = " + clusterNo );

		System.out.println(" reference_no = " + instance.getProperty(refNoMeta) + " cluster_no = " + instance.getProperty(clusterNoMeta));
//		System.out.println((CharSequence)instance.getSource());

		for(int i=0; i<startTags.length; i++){
			String field_instance = (String)instance.getProperty(startTags[i]);
			System.out.println(startTags[i] + " : " + field_instance);
		}
	} 

	protected void print_canonical()
	{
		System.out.println("Canonical fields:");
		if(canonicalObj instanceof CharSequence) {
			print((CharSequence)canonicalObj);
		}
		else if(canonicalObj instanceof Instance){
			 print((Instance)canonicalObj);

			for(int i=0; i<startTags.length; i++){	
				System.out.println("CANONICAL_VALUE: " + startTags[i] + " : " + (String)(((Instance)canonicalObj).getProperty(startTags[i])) );
			}	
		}
	}

}
