
/* First created by JCasGen Sun Jun 30 14:16:53 CEST 2019 */
package de.julielab.jcore.types;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.cas.impl.TypeImpl;
import org.apache.uima.cas.Type;

/** Annotation for all elements in the CAS that belong to figures, e.g. figure title, figure caption etc.
 * Updated by JCasGen Sun Jun 30 14:16:53 CEST 2019
 * @generated */
public class Figure_Type extends TextObject_Type {
  /** @generated */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = Figure.typeIndexID;
  /** @generated 
     @modifiable */
  @SuppressWarnings ("hiding")
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("de.julielab.jcore.types.Figure");



  /** initialize variables to correspond with Cas Type and Features
	 * @generated
	 * @param jcas JCas
	 * @param casType Type 
	 */
  public Figure_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

  }
}



    