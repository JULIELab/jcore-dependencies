## LINNAEUS

This project is just a copy of LINNAEUS 2.0 from http://linnaeus.sourceforge.net by Martin Gerner. However, the original LINNAEUS species tagger doesn't seem to be available via Maven Central which is required for our JCoRe components. This copy of LINNAEUS has been changed to to Maven directory layout and dependency management. Refer to the file MODIFICATIONS to see details about further modifications.

## Modifications

The original LINNEAUS version for this project is the 2.0 archive as downloaded from http://linnaeus.sourceforge.net.

Structural changes:

1. Moved the src/ folder to src/main/java to comply with Maven standards.
2. Moved lib/resources-linnaeus to src/test/resources
3. Removed the lib/ directory and declared the requried libraries as Maven dependencies instead.

Code changes:

1. ArgParser
In order to compress dictionaries for the storage in version control, all the getInputStream()
methods have been changed to open a GZIPInputStream if the requested resource files ends on
.gz or .gzip.
