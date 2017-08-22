package uk.ac.man.entitytagger.networking;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;

import martin.common.compthreads.Problem;

import uk.ac.man.entitytagger.Mention;
import uk.ac.man.entitytagger.matching.Matcher;

public class SimpleServer implements Iterator<Problem<Object>> {
	private ServerSocket socket;
	private Matcher matcher;
	private Map<Integer,List<Mention>> cache = null;
	private Semaphore cacheSem;

	public SimpleServer(int port, Matcher matcher, boolean enableCache){
		try {
			this.socket = new ServerSocket(port);
			this.matcher = matcher;
			
			if (enableCache){
				this.cache = new HashMap<Integer,List<Mention>>();
				this.cacheSem = new Semaphore(1, true);
			}
			
		} catch (IOException e) {
			System.err.println(e);
			e.printStackTrace();
			System.exit(-1);
		}		
	}
	
	public boolean hasNext() {
		return true;
	}

	public SimpleServerWorker next() {
		try {
			Socket s = this.socket.accept();
			return new SimpleServerWorker(s, matcher, cache, cacheSem);
		} catch (IOException e) {
			System.err.println(e);
			e.printStackTrace();
			System.exit(-1);
		}
		
		return null;
	}

	public void remove() {
		throw new UnsupportedOperationException();		
	}
}
