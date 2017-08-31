package dragon.util;

import java.io.*;

import org.apache.commons.io.FileUtils;

/**
 * <p>
 * A convenient class for basic file utility operations
 * </p>
 * <p>
 * </p>
 * <p>
 * Copyright: Copyright (c) 2003
 * </p>
 * <p>
 * Company:
 * </p>
 * 
 * @author not attributable
 * @version 1.0
 */

public class FileUtil {
	public FileUtil() {
	}

	public static BufferedReader getTextReader(String filename) {
		return getTextReader(new File(filename), null);
	}

	public static BufferedReader getTextReader(File file) {
		return getTextReader(file, null);
	}

	public static BufferedReader getTextReader(String filename, String charSet) {
		return getTextReader(new File(filename), charSet);
	}

	public static BufferedReader getTextReader(File file, String charSet) {
		try {
			InputStream is;
			if (file.exists()) {
				is = new FileInputStream(file);
			} else {
				String classPathResource = file.getPath();
				int exIndex = classPathResource.indexOf('!');
				if (exIndex != -1){
					// the ! means that this is relative to a JAR file; we only want the part after the !
					classPathResource = classPathResource.substring(exIndex+1);
				}
				if (!classPathResource.startsWith("/"))
					classPathResource = "/" + classPathResource;
				is = FileUtil.class.getResourceAsStream(classPathResource);
			}
			
			BufferedReader br;
			if (charSet == null)
				charSet = EnvVariable.getCharSet();
			if (charSet == null && file.exists())
				br = new BufferedReader(new FileReader(file));
			else if (file.exists())
				br = new BufferedReader(new InputStreamReader(is, charSet));
			else if (charSet != null) {
				br = new BufferedReader(
						new InputStreamReader(
								is,
								charSet));
			} else {
				br = new BufferedReader(new InputStreamReader(is));
			}
			return br;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static BufferedWriter getTextWriter(String filename) {
		return getTextWriter(filename, false, null);
	}

	public static BufferedWriter getTextWriter(String filename, String charSet) {
		return getTextWriter(filename, false, charSet);
	}

	public static BufferedWriter getTextWriter(String filename, boolean append) {
		return getTextWriter(filename, append, null);
	}

	public static BufferedWriter getTextWriter(String filename, boolean append, String charSet) {

		try {
			if (charSet == null)
				charSet = EnvVariable.getCharSet();
			if (charSet == null)
				return new BufferedWriter(new FileWriter(filename, append));
			else
				return new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filename, append), charSet));
		} catch (Exception e) {
			return null;
		}
	}

	public static boolean saveTextFile(String filename, String content) {
		return saveTextFile(filename, content, null);
	}

	public static boolean saveTextFile(String filename, String content, String charSet) {
		BufferedWriter out;

		try {
			out = getTextWriter(filename, charSet);
			out.write(content);
			out.close();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public static String readTextFile(String filename) {
		return readTextFile(new File(filename), null);
	}

	public static String readTextFile(File file) {
		return readTextFile(file, null);
	}

	public static String readTextFile(String filename, String charSet) {
		return readTextFile(new File(filename), charSet);
	}

	public static String readTextFile(File file, String charSet) {
		BufferedReader fr;
		char buf[];
		int len = 0, count = 128000;
		StringBuffer str;

		try {
			fr = getTextReader(file, charSet);
			buf = new char[count];
			str = new StringBuffer();

			len = fr.read(buf);
			while (true) {
				str.append(buf, 0, len);
				if (len < count)
					break;
				else
					len = fr.read(buf);
			}
			return str.toString();

		} catch (Exception e) {
			return "";
		}
	}

	public static PrintWriter getScreen() {
		String charSet;

		try {
			charSet = EnvVariable.getCharSet();
			if (charSet != null)
				return new PrintWriter(new OutputStreamWriter(System.out, charSet), true);
			else
				return new PrintWriter(new OutputStreamWriter(System.out), true);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static PrintWriter getPrintWriter(String filename) {
		return getPrintWriter(filename, false, null);
	}

	public static PrintWriter getPrintWriter(String filename, boolean append) {
		return getPrintWriter(filename, append, null);
	}

	public static PrintWriter getPrintWriter(String filename, boolean append, String charSet) {
		try {
			if (charSet == null)
				charSet = EnvVariable.getCharSet();
			if (charSet != null)
				return new PrintWriter(
						new OutputStreamWriter(new FileOutputStream(new File(filename), append), charSet));
			else
				return new PrintWriter(new FileOutputStream(new File(filename), append));
		} catch (Exception e) {
			return null;
		}
	}

	public static String getNewTempFilename(String folder, String prefix, String suffix) {
		File file;
		int i;

		i = 1;
		while (true) {
			file = new File(folder + prefix + String.valueOf(i) + "." + suffix);
			if (!file.exists())
				break;
			i++;
		}
		return folder + prefix + String.valueOf(i) + "." + suffix;
	}

	public static String getNewTempFilename(String prefix, String suffix) {
		File file;
		int i;

		i = 1;
		while (true) {
			file = new File(prefix + String.valueOf(i) + "." + suffix);
			if (!file.exists())
				break;
			i++;
		}
		return prefix + String.valueOf(i) + "." + suffix;
	}

	public static boolean exist(String filename) {
		File file;

		file = new File(filename);
		return file.exists();
	}

	public static long getSize(String filename) {
		File file;

		file = new File(filename);
		if (file.exists())
			return file.length();
		else
			return 0;
	}

	public static void changeTextFileEncoding(String file, String oldEncoding, String newEncoding) {
		changeTextFileEncoding(new File(file), oldEncoding, newEncoding);

	}

	public static void changeTextFileEncoding(File file, String oldEncoding, String newEncoding) {
		File[] subs;
		int i;

		try {
			if (file.isFile()) {
				if (!file.exists())
					return;
				saveTextFile(file.getAbsolutePath(), readTextFile(file, oldEncoding), newEncoding);
			} else {
				subs = file.listFiles();
				for (i = 0; i < subs.length; i++)
					changeTextFileEncoding(subs[i], oldEncoding, newEncoding);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}