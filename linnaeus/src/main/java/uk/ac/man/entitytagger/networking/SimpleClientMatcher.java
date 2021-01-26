package uk.ac.man.entitytagger.networking;

import uk.ac.man.documentparser.dataholders.Document;
import uk.ac.man.entitytagger.Mention;
import uk.ac.man.entitytagger.matching.Matcher;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;
import java.util.Random;

public class SimpleClientMatcher extends Matcher {

	private String[] hosts;
	private int[] ports;
	private Random rand;

	public SimpleClientMatcher(String[] hosts, int[] ports){
		if (hosts.length != ports.length)
			throw new IllegalStateException("number of hosts is not equal to nunber of ports");
		if (hosts.length == 0)
			throw new IllegalStateException("at least one server has to be specified");

		this.hosts = hosts;
		this.ports = ports;
		this.rand = new Random();
	}

	public SimpleClientMatcher(String str) {
		String[] fields = str.split("\\|");

		if (fields.length == 0)
			throw new IllegalStateException("at least one server has to be specified");

		String[] hosts = new String[fields.length];
		int[] ports = new int[fields.length];

		for (int i = 0; i < fields.length; i++){
			String[] f2 = fields[i].split(":");

			hosts[i] = f2[0];
			ports[i] = Integer.parseInt(f2[1]);
		}

		this.hosts = hosts;
		this.ports = ports;
		this.rand = new Random();
	}

	@Override
	public List<Mention> match(String text, Document doc) {
		int host = rand.nextInt(hosts.length);
		String docID = doc != null ? doc.getID() : null;
		doc = null;
		
		while (true){
		
		try {
			//new Socket()
			Socket s = new Socket(hosts[host], ports[host]);

			ObjectOutputStream outputStream = new ObjectOutputStream(new BufferedOutputStream(s.getOutputStream()));
			outputStream.flush();

			//outputStream.writeUTF(text);
			outputStream.writeObject(text);

			outputStream.writeBoolean(doc != null);

			if (doc != null)
				outputStream.writeObject(doc);

			outputStream.flush();

			ObjectInputStream inputStream = new ObjectInputStream(new BufferedInputStream(s.getInputStream()));

			List<Mention> matches = (List<Mention>) inputStream.readObject();
			
			s.close();			

			if ((docID != null) && (matches != null))
				for (Mention m : matches)
					m.setDocid(docID);
			
			return matches;
			
		} catch (Exception e) {
			if (e.toString().contains("Connection timed out"))
				System.err.println("Connection to " + hosts[host] + ":" + ports[host] +  " timed out. Trying again...");
			else
				System.err.println("Connection to " + hosts[host] + ":" + ports[host] +  " failed (" + e.toString() + "). Trying again...");
			try{
				Thread.sleep(2000);
			} catch (InterruptedException e2){
				System.err.println(e);
				e.printStackTrace();
				System.exit(-1);
			}
		}
		
		}
	}
	
	public int size(){
		return hosts.length;
	}
}
