package dragon.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;

/**
 * <p>Simple Word Dictionary</p>
 * <p> </p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class SimpleDictionary {
	private SortedArray list;
	private boolean caseSensitive;
	
	public SimpleDictionary(){
		this(false);
	}
	
	public SimpleDictionary(boolean caseSensitive){
		this.caseSensitive=caseSensitive;
		list=new SortedArray();
	}
	
	public SimpleDictionary(String dictFile){
		this(dictFile,false);
	}
	
	public SimpleDictionary(String dictFile, boolean caseSensitive){
		list=loadList(dictFile,caseSensitive);
		this.caseSensitive=caseSensitive;
	}
	
	public void add(String word){
		if(caseSensitive)
			list.add(word);
		else
			list.add(word.toLowerCase());
	}
	
	public boolean exist(String word){
		if(word==null)
			return false;
		if(caseSensitive)
			return list.binarySearch(word)>=0;
		else
			return list.binarySearch(word.toLowerCase())>=0;	
	}
	
	public static void merge(String inputFolder, String outputFile, boolean caseSensitive){
		File folder, arrFile[];
		SortedArray list;
		BufferedWriter bw;
		int i;
		
		folder=new File(inputFolder);
		if(!folder.exists())
			return;
		if(!folder.isDirectory())
			return;
		arrFile=folder.listFiles();
		list=new SortedArray();
		for(i=0;i<arrFile.length;i++)
			loadList(inputFolder+"/"+arrFile[i].getName(),list,caseSensitive);
		
		//output
		try{
			bw=FileUtil.getTextWriter(outputFile);
			for(i=0;i<list.size();i++)
				bw.write((String)list.get(i)+"\n");
			list.clear();
			bw.close();
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public static SortedArray loadList(String filename, boolean caseSensitive){
		SortedArray list;
		
		list=new SortedArray();
		loadList(filename,list,caseSensitive);
		return list;
	}
	
	public static void loadList(String filename, SortedArray list, boolean caseSensitive) {
        BufferedReader br;
        String line;
        int pos;

        try {
            if (filename == null || filename.trim().length() == 0) {
                return;
            }
            if(!FileUtil.exist(filename) && FileUtil.exist(EnvVariable.getDragonHome()+"/"+filename))
                filename=EnvVariable.getDragonHome()+"/"+filename;

            br = FileUtil.getTextReader(filename);
            line = br.readLine().trim();
            try{
            	Integer.parseInt(line);
            	line="";
            }
            catch(Exception e){
            	//do nothing
            }

            while(line!=null) {
            	line=line.trim();
            	if(line.length()==0){
            		line=br.readLine();
            		continue;
            	}
            	pos=line.indexOf('\t');
                if(pos>0)
                    line=line.substring(0,pos);
                if(!caseSensitive)
                	list.add(line.toLowerCase());
                else
                	list.add(line);
                line=br.readLine();
            }
            br.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
