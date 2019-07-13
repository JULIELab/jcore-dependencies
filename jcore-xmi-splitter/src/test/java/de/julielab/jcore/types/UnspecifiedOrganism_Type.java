
/* First created by JCasGen Sat Jul 13 15:41:00 CEST 2019 */
package de.julielab.jcore.types;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.cas.impl.TypeImpl;
import org.apache.uima.cas.Type;

/** 
 * Updated by JCasGen Sat Jul 13 15:41:00 CEST 2019
 * @generated */
public class UnspecifiedOrganism_Type extends Organism_Type {
  /** @generated */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = UnspecifiedOrganism.typeIndexID;
  /** @generated 
     @modifiable */
  @SuppressWarnings ("hiding")
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("de.julielab.jcore.types.UnspecifiedOrganism");



  /** initialize variables to correspond with Cas Type and Features
	 * @generated
	 * @param jcas JCas
	 * @param casType Type 
	 */
  public UnspecifiedOrganism_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

  }
}



    