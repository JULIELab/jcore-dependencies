package martin.common;

import java.io.*;

public class  InputStreamDumper implements Runnable {
	private InputStream stream=null;
	private OutputStream outStream=null;
	private StringBuffer sb=null;

	public InputStreamDumper(InputStream stream){
		this.stream = stream;
	}

	public InputStreamDumper(InputStream stream, OutputStream outStream){
		this.stream = stream;
		this.outStream = outStream;
	}

	public InputStreamDumper(InputStream stream, File outFile){
		this.stream = stream;

		try {
			this.outStream = outFile != null ? new BufferedOutputStream(new FileOutputStream(outFile)) : null;
		} catch (FileNotFoundException e) {
			System.err.println(e);
			e.printStackTrace();
			System.exit(-1);
		}
	}

	public InputStreamDumper(InputStream stream, StringBuffer sb){
		this.stream = stream;
		this.sb = sb;
	}

	public void run() {
		if (sb == null){
			try{
				PrintWriter outStream = this.outStream != null ? new PrintWriter(this.outStream) : null;

				InputStreamReader isr = new InputStreamReader(this.stream);
				BufferedReader br = new BufferedReader(isr);
				String line=null;
				while ( (line = br.readLine()) != null)
				{
					if (outStream != null){
						outStream.println(line);
						outStream.flush();
					}
				}				
			} catch (Exception e){
				if (e.toString().toLowerCase().startsWith("java.io.ioexception: stream closed"))
					return;
				System.err.println(e);
				e.printStackTrace();
				System.exit(-1);
			}
		} else {
			try{
				BufferedReader br = new BufferedReader(new InputStreamReader(stream));
				String l = br.readLine();
				while (l != null){
					sb.append(l + "\n");
					l = br.readLine();
				}
			} catch (Exception e){
				if (e.toString().toLowerCase().startsWith("java.io.ioexception: stream closed"))
					return;
				System.err.println(e);
				e.printStackTrace();
				System.exit(-1);
			}
		}
	}

	public void closeAll(){
	}
}
