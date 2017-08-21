package uk.ac.man.entitytagger.networking;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import uk.ac.man.documentparser.dataholders.Document;
import uk.ac.man.documentparser.input.TextFile;
import uk.ac.man.entitytagger.doc.TaggedDocument;
import uk.ac.man.entitytagger.doc.TaggedDocument.Format;
import uk.ac.man.entitytagger.matching.MatchOperations;
import uk.ac.man.entitytagger.matching.Matcher;
import uk.ac.man.entitytagger.Mention;

public class AlibabaWorker implements Runnable {
	private Socket s;
	private Matcher matcher;
	private Logger logger;
	private int privid;
	private File pmcBaseDir;
	private File medlineBaseDir;
	private static int id = 0;

	public AlibabaWorker(Socket s, Matcher matcher, Logger logger, File pmcBaseDir, File medlineBaseDir) {
		this.s = s;
		this.matcher = matcher;
		this.logger = logger;
		this.privid = id++;
		this.pmcBaseDir = pmcBaseDir;
		this.medlineBaseDir = medlineBaseDir;
	}

	public void run(){
		try {
			BufferedReader inStream = new BufferedReader(new InputStreamReader(s.getInputStream()));
			BufferedWriter outStream = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));

			int i = 0;
			while (!inStream.ready() && i++ < 20)
				Thread.sleep(50);

			String response = null;

			while (inStream.ready() && response == null){
				String input = inStream.readLine();

				//logger.info("%t: Worker[" + privid + "]: " + input);

				if (input.contains("type=text")){
					//logger.info("%t: Worker[" + privid +"]: received " + input.getBytes().length + " bytes of text data.\n");
					response = runText(input);
				} else if (input.contains("type=pmid")){
					//logger.info("%t: Worker[" + privid +"]: received PMID request for " + input + ".\n");
					response = runPMID(input);
				}
			}

			if (response == null){
				logger.info("%t: Worker[" + privid + "]: did not receive any request, disconnecting.\n");
				s.close();
			} else {
				outStream.write("HTTP/1.1 200 OK\r\n");
				outStream.write("Server: LINNAEUS\r\n");
				outStream.write("Content-Length: " + response.getBytes().length + "\r\n");
				outStream.write("Date: Tue 01 Sep 2009 13:00:00 GMT\r\n\r\n");
				outStream.write(response);

				outStream.flush();

				//logger.info("%t: Worker[" + privid +"]: Sent " + response.getBytes().length + " bytes of data, closing connection.\n");

				s.close();
			}

		} catch (Exception e){
			System.err.println(e);
			e.printStackTrace();
			System.exit(-1);
		}	
	}

	private String runPMID(String input) {
		int s = input.indexOf("&query=") + 7;
		int e = input.indexOf(" ", s);

		if (s == 6 || e == -1)
			throw new IllegalStateException("Invalid PMID request: " + input);

		String id = input.substring(s,e);

		String first = ("0000" + id).substring(id.length()+4-2,id.length()+4);
		String second = ("0000" + id).substring(id.length()+4-4,id.length()+4-2);

		File file;

		if (id.startsWith("000000")){
			file = new File(new File(pmcBaseDir, first), id + ".txt");
		} else {
			file = new File(new File(new File(medlineBaseDir, first), second), id + ".txt");
		}

		Document doc = new TextFile(new File[]{file}).next();
		TaggedDocument tdoc = MatchOperations.matchDocument(matcher, doc);
		ArrayList<Mention> matches = tdoc.getAllMatches();

		String text = tdoc.getOriginal().toString();

		text = check(text, matches);

		String res = TaggedDocument.toHTML(text, matches, Format.Alibaba, true, null).toString();

		return res;
	}

	private String check(String text, List<Mention> matches) {
		//this is to circumvent a GNAT bug which is triggered if no species are present in a document
		if (matches.size() == 0){
			matches.add(new Mention(new String[]{"species:ncbi:9606"},0,1,""));
			matches.add(new Mention(new String[]{"species:ncbi:10090"},1,2,""));
			if (text.length() < 2)
				text = "..";
		}

		for (Mention m : matches){
			if (m.getEnd() > text.length()){
				System.err.println("'" + text + "'");
				System.err.println(m.toString());
				throw new IllegalStateException("Found match with end coordinate > text length");
			}
		}

		return text;
	}

	private String runText(String input) {
		int s = input.indexOf("&query=") + 7;
		int e = input.indexOf(" HTTP");

		if (s == 6 || e == -1)
			return null;

		String text = input.substring(s,e);

		text = text.replaceAll("%20", " ");

		List<Mention> matches = matcher.match(text);

		text = check(text, matches);

		try{
			String res = TaggedDocument.toHTML(text, matches, Format.Alibaba, true, null).toString();
			return res;
		} catch (Exception ex){
			System.err.println("Error detected: " + ex);
			System.err.println("Text: " + text);
			for (Mention m : matches)
				System.err.println(m.toString());
			ex.printStackTrace();
		}
		return null;
	}
}
