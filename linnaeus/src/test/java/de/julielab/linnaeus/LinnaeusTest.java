package de.julielab.linnaeus;

import static org.junit.Assert.*;

import java.util.List;
import java.util.logging.Logger;

import org.junit.Test;

import martin.common.ArgParser;
import uk.ac.man.entitytagger.EntityTagger;
import uk.ac.man.entitytagger.Mention;
import uk.ac.man.entitytagger.matching.Matcher;

public class LinnaeusTest {
	@Test
	public void testLinnaeus() {
		// Just a very small test to make sure the dictionary loading and a
		// simple species lookup are working.
		Logger log = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
		String configFile = "src/test/resources/resources-linnaeus/properties.conf";
		ArgParser ap = new ArgParser(new String[] { "--properties", configFile });

		Matcher matcher = EntityTagger.getMatcher(ap, log);

		List<Mention> match = matcher.match("There is a mouse on the floor!");
		assertEquals(1, match.size());
		assertEquals("species:ncbi:10090", match.get(0).getMostProbableID());
	}
}
