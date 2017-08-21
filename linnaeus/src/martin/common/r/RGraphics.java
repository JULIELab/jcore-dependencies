package martin.common.r;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.List;
import java.util.Random;

/**
 * Utility class for producing R graphs from Java, utilizing a set of R scripts (in the martin.common.r.scripts folder) 
 * @author Martin
 */
public class RGraphics {
	private File scriptDirectory, tempDir;
	private File RScriptPath = new File("/usr/bin/Rscript");
	private Random random = new Random();

	/**
	 * 
	 * @param scriptDirectory the directory where the required R scripts are located. These should be copied from the martin.common.r.scripts source folder.
	 * @param RScriptPath path to the Rscript executable. If null, will default to "/usr/bin/Rscript".
	 */
	public RGraphics(File scriptDirectory, File RScriptPath, File tempDir){
		this.scriptDirectory = scriptDirectory;
		this.tempDir = tempDir;

		if (RScriptPath != null)
			this.RScriptPath = RScriptPath;
	}

	public void boxPlot(List<Double>[] values, String[] labels, File outFile, Integer width, Integer height){
		if (width == null)
			width = 400;
		if (height == null)
			height = 400;
		
		//File tempFile = getTempFile();
		File tempFile = new File(outFile.getAbsolutePath() + ".tmp");
		saveTempFile(values,labels,tempFile,false);
		runScript("boxplot.R", tempFile, outFile, "" + width + " " + height);		
	}
	
	private void saveTempFile(List<Double>[] values, String[] setTitles, File tempFile, boolean indexes){
		try {
			BufferedWriter outStream = new BufferedWriter(new FileWriter(tempFile));

			if (indexes)
				outStream.write("index,");
			
			outStream.write("data,set\n");

			for (int i = 0; i < values.length; i++)
				for (int j = 0; j < values[i].size(); j++){
					if (indexes)
						outStream.write(j + ",");
					outStream.write(values[i].get(j) + "," + setTitles[i] + "\n");
				}

			outStream.close();
		} catch (Exception e){
			System.err.println(e);
			e.printStackTrace();
			System.exit(-1);			
		}
	}

	private void saveTempFileBars(double[] values, String[] names, File tempFile){
		if (values.length != names.length)
			throw new IllegalStateException("values.length != names.length");
		
		try {
			BufferedWriter outStream = new BufferedWriter(new FileWriter(tempFile));
			
			outStream.write("data,set\n");

			for (int i = 0; i < values.length; i++){
				outStream.write(values[i] + ",0\n");
				outStream.write(names[i] + ",-1\n");
			}

			outStream.close();
		} catch (Exception e){
			System.err.println(e);
			e.printStackTrace();
			System.exit(-1);			
		}
	}
	
	private File getTempFile(){
		return new File(tempDir, Math.abs(random.nextInt()) + ".temp");
	}
	
	public void bars(double[] values, String[] names, File outFilePNG){
		if (values.length != names.length)
			throw new IllegalStateException("values.length != names.length");
		
		File tempFile = getTempFile();
		saveTempFileBars(values, names, tempFile);
		runScript("bars.R", tempFile, outFilePNG);
	}
	
	private void runScript(String script, File tempFile, File outFile){
		runScript(script,tempFile,outFile,"");
		
	}
	
	private void runScript(String script, File tempFile, File outFile, String extra){
		File scriptF = new File(scriptDirectory, script);

		String command = RScriptPath + " " + scriptF.getAbsolutePath() + " "+ tempFile.getAbsolutePath() + " " + outFile.getAbsolutePath() + " " + extra;

		try {
			Runtime.getRuntime().exec(command).waitFor();
			if (outFile.exists() && outFile.length() > 0)
				tempFile.delete();
			else
				System.err.println("Error occured when creating " + outFile.getAbsolutePath());
		} catch (Exception e) {
			System.err.println(e);
			e.printStackTrace();
			System.exit(-1);
		}
	}

	public void lines(List<Double>[] values, String[] setTitles, File outFilePNG, String customScript){
		if (setTitles.length != values.length)
			throw new IllegalStateException("The number of titles have to match the number of value sets");

		File tempFile = getTempFile();

		saveTempFile(values, setTitles, tempFile, true);

		runScript(customScript, tempFile, outFilePNG, outFilePNG.getName().split("\\.")[0]);
	}
	
	public void lines(List<Double>[] values, String[] setTitles, File outFilePNG){
		lines(values, setTitles, outFilePNG, "lines.R");
	}

	public void histogramDistribution(List<Double>[] values, String[] setTitles, File outFilePNG){
		if (setTitles.length != values.length)
			throw new IllegalStateException("The number of titles have to match the number of value sets");

		File tempFile = getTempFile();

		saveTempFile(values, setTitles, tempFile, false);

		runScript("histDist.R", tempFile, outFilePNG);
	}
}