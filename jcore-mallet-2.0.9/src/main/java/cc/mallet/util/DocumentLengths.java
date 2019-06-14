package cc.mallet.util;

import cc.mallet.types.FeatureSequence;
import cc.mallet.types.Instance;
import cc.mallet.types.InstanceList;

import java.io.File;
import java.util.logging.Logger;

public class DocumentLengths {

	protected static Logger logger = MalletLogger.getLogger(DocumentLengths.class.getName());

	static cc.mallet.util.CommandOption.String inputFile = new cc.mallet.util.CommandOption.String
		(DocumentLengths.class, "input", "FILENAME", true, null,
		 "Filename for the input instance list", null);
		
	public static void main(String[] args) throws Exception {

		CommandOption.setSummary (DocumentLengths.class,
								  "Print the length of FeatureSequences in an instance list");
		CommandOption.process (DocumentLengths.class, args);

		InstanceList instances = InstanceList.load (new File(inputFile.value));
		for (Instance instance: instances) {
			if (! (instance.getData() instanceof FeatureSequence)) {
				System.err.println("DocumentLengths is only applicable to FeatureSequence objects (use --keep-sequence when importing)");
				System.exit(1);
			}
			
			FeatureSequence words = (FeatureSequence) instance.getData();
			System.out.println(words.size());
		}
	}
}