
/* First created by JCasGen Wed Aug 08 13:36:50 CEST 2018 */
package de.julielab.jcore.types;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.cas.impl.TypeImpl;
import org.apache.uima.cas.Type;

/** attributes to an organism such as e.g. sex, developmental stage, transplantation type ...
 * Updated by JCasGen Wed Aug 08 13:36:50 CEST 2018
 * @generated */
public class OrganismAttribute_Type extends BioEntityMention_Type {
  /** @generated */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = OrganismAttribute.typeIndexID;
  /** @generated 
     @modifiable */
  @SuppressWarnings ("hiding")
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("de.julielab.jcore.types.OrganismAttribute");



  /** initialize variables to correspond with Cas Type and Features
	 * @generated
	 * @param jcas JCas
	 * @param casType Type 
	 */
  public OrganismAttribute_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

  }
}



    