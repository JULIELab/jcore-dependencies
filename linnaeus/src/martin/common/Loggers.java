package martin.common;

import java.io.File;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class Loggers {

	private static String millisToString(long millis){
		GregorianCalendar c = new GregorianCalendar();
		c.setTimeInMillis(millis);

		String time = c.get(Calendar.YEAR) + "-" + Misc.addzeros(c.get(Calendar.MONTH),2) + "-" + Misc.addzeros(c.get(Calendar.DAY_OF_MONTH),2) + " " + Misc.addzeros(c.get(Calendar.HOUR_OF_DAY),2) + ":" + Misc.addzeros(c.get(Calendar.MINUTE),2) + ":" + Misc.addzeros(c.get(Calendar.SECOND),2);
		return time;
	}

	private static Handler getDefaultConsoleHandler(){
		Handler h = new Handler(){
			@Override
			public void close() throws SecurityException {}
			@Override
			public void flush() {}

			@Override
			public void publish(LogRecord record) {
				String str = this.getFormatter().format(record);

				if (record.getLevel() == Level.SEVERE || record.getLevel() == Level.WARNING)
					System.err.print(str);
				else
					System.out.print(str);				
			}
		};
		h.setFormatter(getDefaultFormatter());
		return h;
	}	

	private static Formatter getDefaultFormatter(){
		return new Formatter(){
			@Override
			public String format(LogRecord arg0) {
				String message = arg0.getMessage();
				message = message.replace("%t", millisToString(arg0.getMillis()));
				if (!message.endsWith("\n"))
					message = message + "\n";
				
				return message;
			}
		};
	}	

	private static Formatter getDefaultFileFormatter(){
		return new Formatter(){
			@Override
			public String format(LogRecord arg0) {
				String time = millisToString(arg0.getMillis());

				String message = time + "," + arg0.getLevel() + "," + arg0.getMessage();

				if (!message.endsWith("\n"))
					message += "\n";
				
				message = message.replace("%t: ", "");
				message = message.replace("%t", "");
				
				return message;
			}
		};
	}

	public static Logger getDefaultFileLogger(File file){
		try{
			Logger logger = Logger.getAnonymousLogger();
			logger.setLevel(Level.INFO);
			logger.setUseParentHandlers(false);
			Handler h = new FileHandler(file.getAbsolutePath(), true);
			h.setFormatter(getDefaultFileFormatter());
			logger.addHandler(h);
			return logger;
		} catch (Exception e){
			System.err.println(e);
			e.printStackTrace();
			System.exit(-1);
		}
		return null; //dummy return 
	}

	public static Logger getDefaultLogger(ArgParser ap){
		if (ap != null && ap.containsKey("fileLogger"))
			return getDefaultFileLogger(ap.getFile("fileLogger"));
		
		Logger logger = Logger.getAnonymousLogger();
		configureLogger(logger, ap);
		return logger;
	}
	
	public static void configureLogger(Logger logger, ArgParser ap){
		logger.setLevel(Level.INFO);

		if (ap != null && (ap.containsKey("quiet")))
			logger.setLevel(Level.SEVERE);
		if (ap != null && ap.containsKey("veryquiet"))
			logger.setLevel(Level.OFF);

		logger.setUseParentHandlers(false);
		logger.addHandler(getDefaultConsoleHandler());
	}
}
