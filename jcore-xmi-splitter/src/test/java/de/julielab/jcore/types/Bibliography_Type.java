
/* First created by JCasGen Tue Sep 03 12:35:27 CEST 2019 */
package de.julielab.jcore.types;

import org.apache.uima.cas.Type;
import org.apache.uima.cas.impl.TypeImpl;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JCasRegistry;

/** The whole bibliography, beginning with a title like e.g. References or Literature and ending at the end of the last reference.
 * Updated by JCasGen Tue Sep 03 12:35:27 CEST 2019
 * @generated */
public class Bibliography_Type extends Zone_Type {
  /** @generated */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = Bibliography.typeIndexID;
  /** @generated 
     @modifiable */
  @SuppressWarnings ("hiding")
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("de.julielab.jcore.types.Bibliography");



  /** initialize variables to correspond with Cas Type and Features
	 * @generated
	 * @param jcas JCas
	 * @param casType Type 
	 */
  public Bibliography_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

  }
}



    