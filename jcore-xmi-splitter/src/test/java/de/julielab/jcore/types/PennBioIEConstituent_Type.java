
/* First created by JCasGen Tue Sep 03 12:35:27 CEST 2019 */
package de.julielab.jcore.types;

import org.apache.uima.cas.Type;
import org.apache.uima.cas.impl.TypeImpl;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JCasRegistry;

/** Mark A. Mandel. Integrated Annotation of Biomedical Text: Creating the PennBioIE Corpus. March 2006. Text Mining, Ontologies and Natural Language Processing in Biomedicine.
 * Updated by JCasGen Tue Sep 03 12:35:27 CEST 2019
 * @generated */
public class PennBioIEConstituent_Type extends PTBConstituent_Type {
  /** @generated */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = PennBioIEConstituent.typeIndexID;
  /** @generated 
     @modifiable */
  @SuppressWarnings ("hiding")
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("de.julielab.jcore.types.PennBioIEConstituent");



  /** initialize variables to correspond with Cas Type and Features
	 * @generated
	 * @param jcas JCas
	 * @param casType Type 
	 */
  public PennBioIEConstituent_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

  }
}



    