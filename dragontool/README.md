# dragontools

This project is basically a copy from http://dragon.ischool.drexel.edu. Its only purpose for JCoRe is do deliver required dependencies for the BANNER gene tagger.
Unfortunately, a number of libraries for the dragontools are not available in public Maven repositories. As such, the *functionality of this project is restricted*.
Comonents that won't work include:
* LibSVM components
* SVMLight components
* Topic model writing to Excel
* Linkgrammar components
* The MedposTagger

Also, the CnSimpleDocumentParser won't work correctly because its String literals were containing illegal characters in this copy which have been deleted and will probably render the component buggy.

Classes with dependencies to libraries that are not available have just been commented out entirely. Thus, the files are still there, but thy only contain comments.
Classes that depend on such classes are also commented out.

If appropriate libraries are available in public Maven repositories, please feel free to open an issue with the Maven coordinates or to make the change yourself in a fork of the repository and issue a pull request.
