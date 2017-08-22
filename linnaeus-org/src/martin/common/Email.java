package martin.common;

import java.io.BufferedWriter;
import java.io.OutputStreamWriter;

public class Email {
	public void sendMail(String[] recepients, String subject, String body){
		for (String rcpt : recepients){
			try{
			
				Process p = Runtime.getRuntime().exec(new String[]{"mail","-s",subject,rcpt});
				
				//new Thread(new InputStreamDumper(p.getInputStream(),System.out)).start();
				//new Thread(new InputStreamDumper(p.getErrorStream(),System.err)).start();
				
				BufferedWriter outStream = new BufferedWriter(new OutputStreamWriter(p.getOutputStream()));
				outStream.write(body);
				outStream.write("\n");
				outStream.flush();
				outStream.close();
				
				p.waitFor();
			
			} catch (Exception e){
				System.err.println(e);
				e.printStackTrace();
				System.exit(-1);
			}
		}
	}
}
