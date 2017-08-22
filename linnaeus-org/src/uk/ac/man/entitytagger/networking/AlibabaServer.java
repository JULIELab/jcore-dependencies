package uk.ac.man.entitytagger.networking;

import java.io.File;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Logger;

import uk.ac.man.entitytagger.EntityTagger;
import uk.ac.man.entitytagger.matching.Matcher;
import martin.common.ArgParser;

public class AlibabaServer {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		ArgParser ap = new ArgParser(args);
		int port = ap.getInt("port", 56100);
		Logger logger = martin.common.Loggers.getDefaultLogger(ap);
		Matcher matcher = EntityTagger.getMatcher(ap, logger);

		File pmcBaseDir = ap.containsKey("pmcTxtBaseDir") ? ap.getFile("pmcTxtBaseDir") : null;
		File medlineBaseDir = ap.containsKey("medlineTxtBaseDir") ? ap.getFile("medlineTxtBaseDir") : null;		
		
		int report = ap.getInt("report", -1);
		
		try {
			ServerSocket socket = new ServerSocket(port);
			logger.info("%t: Server: started, listening to port " + port + ".\n");

			int c = 0;
			while (true){
				Socket s = socket.accept();
				AlibabaWorker worker = new AlibabaWorker(s, matcher, logger, pmcBaseDir, medlineBaseDir);
				new Thread(worker).start();
				if (report != -1 && ++c % report == 0)
					logger.info("%t: Served " + c + " requests.\n");
			}
		} catch (Exception e){
			System.err.println(e);
			e.printStackTrace();
			System.exit(-1);
		}

	}

}
