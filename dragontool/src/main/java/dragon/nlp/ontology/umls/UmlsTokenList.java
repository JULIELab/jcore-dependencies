package dragon.nlp.ontology.umls;

import dragon.nlp.Token;
import dragon.util.*;
import java.io.*;
import java.util.*;

/**
 * <p>List for storing UMLS tokens</p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class UmlsTokenList extends SortedArray{
	private static final long serialVersionUID = 1L;

	public UmlsTokenList(){
    }

    public UmlsTokenList(String tokenFile) {
        loadTokenList(tokenFile);
    }
    
    public UmlsTokenList(String tokenFile,boolean binary) {
        loadTokenList(tokenFile,binary);
    }

    public int addToken(Token token){
        token.setIndex(size());
        if (this.add(token)) {
            token.setFrequency(1);
            return (token.getIndex());
        }
        else {
            token = (Token) get(insertedPos());
            token.addFrequency(1);
            return (token.getIndex());
        }
    }

    public Token tokenAt(int index){
        return (Token)get(index);
    }

    public Token lookup(String token){
        int pos;

        pos=binarySearch(new Token(token));
        if(pos<0)
            return null;
        else
            return (Token)get(pos);
    }

    public Token lookup(Token token){
        int pos;

        pos=binarySearch(token);
        if(pos<0)
            return null;
        else
            return (Token)get(pos);
    }

    private void loadTokenList(String tokenFilename) {
        int len, freq;
        String line;
        ArrayList tokenList;
        BufferedReader br;
        Token token;

        try {
            System.out.println(new java.util.Date() + " Loading Token List...");
            br = FileUtil.getTextReader(tokenFilename);
            line = br.readLine();
            len = Integer.parseInt(line);
            tokenList = new ArrayList(len);

            for (int i = 0; i < len; i++) {
                line = br.readLine();
                String[] strArr = line.split("\t");
                token = new Token(strArr[1]);
                token.setIndex(Integer.parseInt(strArr[0]));
                freq = Integer.parseInt(strArr[2]);
                token.setFrequency(freq);
                token.setWeight(1.0/freq);
                tokenList.add(token);
            }
            br.close();
            Collections.sort(tokenList);
            this.addAll(tokenList);
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    private void loadTokenList(String tokenFilename, boolean binary) {
    	FastBinaryReader fbr;
        ArrayList tokenList;
        Token token;
        byte[] buf;
        int total, freq, index, len, i;

        if(!binary){
        	loadTokenList(tokenFilename);
        	return;
        }
        
        try {
            System.out.println(new java.util.Date() + " Loading Token List...");
            fbr = new FastBinaryReader(tokenFilename);
            buf=new byte[1024];
            total = fbr.readInt();
            tokenList = new ArrayList(total);

            for (i = 0; i < total; i++) {
            	index=fbr.readInt();
            	freq=fbr.readShort();
            	len=fbr.readShort();
            	len=fbr.read(buf,0, len);
                token = new Token(new String(buf,0,len));
                token.setIndex(index);
                token.setFrequency(freq);
                token.setWeight(1.0/freq);
                tokenList.add(token);
            }
            fbr.close();
            Collections.sort(tokenList);
            this.addAll(tokenList);
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void saveTo(String filename) {
        BufferedWriter bw;
        Token t;
        int i;

        try {
            System.out.println(new java.util.Date() + " Saving Token List...");
            bw = FileUtil.getTextWriter(filename);
            bw.write(size() + "");
            bw.write("\n");
            for (i = 0; i < size(); i++) {
                t = (Token) get(i);
                bw.write(t.getIndex() + "\t" + t.getValue() + "\t" + t.getFrequency() + "\n");
                bw.flush();
            }
            bw.close();
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    public void saveTo(String filename, boolean binary){
    	FastBinaryWriter fbw;
    	Token token;
    	int i;
    	
    	if(!binary){
    		saveTo(filename);
    		return;
    	}
    	
    	try{
	    	fbw=new FastBinaryWriter(filename);
	    	fbw.writeInt(size());
	    	for(i=0;i<size();i++){
	    		token=tokenAt(i);
	    		fbw.writeInt(token.getIndex());
	    		fbw.writeShort(token.getFrequency());
	    		fbw.writeShort(token.getValue().length());
	    		fbw.writeBytes(token.getValue());
	    	}
	    	fbw.close();
    	}
    	catch(Exception e){
    		e.printStackTrace();
    	}
    }
}