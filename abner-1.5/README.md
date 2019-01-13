# ABNER

The is the ABNER code as downloaded from http://www.cs.wisc.edu/~bsettles/abner/.
To create the `abner.Scanner` class from the `scanner.jlex` file, `JLex` has been imported in the `pom.xml`. The `Main`
class in the `JLex` package has been used to create `abner.Scanner`.

Also, in the `abner.Input2TokenSequence` class, the `serialVersionUID` has been set to
`-2052454513695348353L` in order to be compatible with the CRF models of `Lingscope` which is the original reason
why we need an ABNER Maven artifact in the first place.