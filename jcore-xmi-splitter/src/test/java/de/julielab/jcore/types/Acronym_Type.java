
/* First created by JCasGen Sat Jun 22 14:44:28 CEST 2019 */
package de.julielab.jcore.types;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.cas.impl.TypeImpl;
import org.apache.uima.cas.Type;

/** Abbreviation includes acronyms written as the initial letter or letters of words, and pronounced on the basis of this abbreviated written form.
 * Updated by JCasGen Sat Jun 22 14:44:28 CEST 2019
 * @generated */
public class Acronym_Type extends Abbreviation_Type {
  /** @generated */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = Acronym.typeIndexID;
  /** @generated 
     @modifiable */
  @SuppressWarnings ("hiding")
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("de.julielab.jcore.types.Acronym");



  /** initialize variables to correspond with Cas Type and Features
	 * @generated
	 * @param jcas JCas
	 * @param casType Type 
	 */
  public Acronym_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

  }
}



    