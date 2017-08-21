package uk.ac.man.entitytagger.networking;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;

import uk.ac.man.documentparser.dataholders.Document;
import uk.ac.man.entitytagger.Mention;
import uk.ac.man.entitytagger.matching.Matcher;
import martin.common.compthreads.Problem;

public class SimpleServerWorker implements Problem<Object> {

	private Socket s;
	private Matcher matcher;
	private Map<Integer, List<Mention>> cache;
	private Semaphore cacheSem;

	public SimpleServerWorker(Socket s, Matcher matcher, Map<Integer,List<Mention>> cache, Semaphore cacheSem) {
		this.s = s;
		this.matcher = matcher;
		this.cache = cache;
		this.cacheSem = cacheSem;
	}

	public Object compute(){
		try {
			ObjectInputStream inputStream = new ObjectInputStream(new BufferedInputStream(s.getInputStream()));

			//String text = inputStream.readUTF();
			String text = (String) inputStream.readObject();
			
			boolean hasDoc = inputStream.readBoolean();

			Document doc = hasDoc ? (Document) inputStream.readObject() : null;

			List<Mention> mentions = null;

			if (cache != null){
				int hash = text.hashCode();
				String docid = doc != null ? doc.getID() : null;

				try{
					cacheSem.acquire();

					if (cache.containsKey(hash) && cache.get(hash) != null){
						List<Mention> ms = cache.get(hash);
						mentions = new ArrayList<Mention>(ms.size());
						for (Mention m : ms){
							Mention m2 = m.clone();
							m2.setDocid(docid);
							mentions.add(m2);
						}
					}

					cacheSem.release();

					if (mentions == null){
						mentions = doc != null ? matcher.match(text, doc) : matcher.match(text, "tempid");

						cacheSem.acquire();
						
						if (mentions != null)
							cache.put(hash,mentions);
						
						cacheSem.release();
					}

				} catch (Exception e){
					System.err.println(e);
					e.printStackTrace();
					System.exit(-1);
				}
			} else {
				mentions = doc != null ? matcher.match(text, doc) : matcher.match(text, "tempid");
			}

			ObjectOutputStream outputStream = new ObjectOutputStream(new BufferedOutputStream(s.getOutputStream()));
			outputStream.writeObject(mentions);
			outputStream.flush();

			s.close();
		} catch (Exception e) {
			System.err.println(e);
			e.printStackTrace();
			//do not quit since we don't want an erroneous connection to kill the whole server
		}

		return null;
	}
}
