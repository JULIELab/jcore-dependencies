
/* First created by JCasGen Wed Aug 08 13:36:50 CEST 2018 */
package de.julielab.jcore.types;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.cas.impl.TypeImpl;
import org.apache.uima.cas.Type;

/** Mark A. Mandel. Integrated Annotation of Biomedical Text: Creating the PennBioIE Corpus. March 2006. Text Mining, Ontologies and Natural Language Processing in Biomedicine.
 * Updated by JCasGen Wed Aug 08 13:36:50 CEST 2018
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



    