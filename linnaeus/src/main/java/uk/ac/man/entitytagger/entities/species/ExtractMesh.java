package uk.ac.man.entitytagger.entities.species;

import martin.common.ArgParser;
import uk.ac.man.documentparser.DocumentParser;
import uk.ac.man.documentparser.dataholders.Document;
import uk.ac.man.documentparser.input.DocumentIterator;
import uk.ac.man.entitytagger.Mention;

import java.io.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

public class ExtractMesh {

	public static HashMap<String,Integer> loadMeshToTaxFile(File file){
		HashMap<String,Integer> res = new HashMap<String,Integer>();
		try{
			BufferedReader inStream = new BufferedReader(new FileReader(file));

			String line = inStream.readLine();

			while (line != null){
				if (!line.startsWith("#")){
					String[] fields = line.split("\t");
					res.put(fields[2],Integer.parseInt(fields[1]));
				}
				line = inStream.readLine();
			}

			inStream.close();
		} catch (Exception e){
			System.err.println(e);
			e.printStackTrace();
			System.exit(-1);
		}
		return res;
	}

	private static void tag(ArgParser ap){
		try{
			DocumentIterator documents = DocumentParser.getDocuments(ap);
			HashMap<String,Integer> meshToTax = loadMeshToTaxFile(ap.getFile("meshToTax"));
			BufferedWriter outStream = new BufferedWriter(new FileWriter(ap.getFile("out")));

			while (documents.hasNext()){
				Document d = documents.next();
				HashSet<Integer> speciesSet = d.getMeshTaxIDs(meshToTax);
				Iterator<Integer> iter = speciesSet.iterator();
				String id = d.getID();
				while (iter.hasNext()){
					Mention m = new Mention(new String[]{""+iter.next()});
					m.setDocid(id);
					outStream.write(m.toString() + "\n");
				}
			}
			
			outStream.close();
		} catch (Exception e){
			System.err.println(e);
			e.printStackTrace();
			System.exit(-1);
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		ArgParser ap = new ArgParser(args);
		tag(ap);
	}
}
