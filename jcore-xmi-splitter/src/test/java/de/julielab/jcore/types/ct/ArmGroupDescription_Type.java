
/* First created by JCasGen Tue Sep 03 12:34:17 CEST 2019 */
package de.julielab.jcore.types.ct;

import de.julielab.jcore.types.Zone_Type;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.impl.TypeImpl;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JCasRegistry;

/** 
 * Updated by JCasGen Tue Sep 03 12:34:17 CEST 2019
 * @generated */
public class ArmGroupDescription_Type extends Zone_Type {
  /** @generated */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = ArmGroupDescription.typeIndexID;
  /** @generated 
     @modifiable */
  @SuppressWarnings ("hiding")
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("de.julielab.jcore.types.ct.ArmGroupDescription");



  /** initialize variables to correspond with Cas Type and Features
	 * @generated
	 * @param jcas JCas
	 * @param casType Type 
	 */
  public ArmGroupDescription_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

  }
}



    