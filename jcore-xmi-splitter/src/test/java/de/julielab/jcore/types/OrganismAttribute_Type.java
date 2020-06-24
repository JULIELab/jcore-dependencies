
/* First created by JCasGen Tue Sep 03 12:34:17 CEST 2019 */
package de.julielab.jcore.types;

import org.apache.uima.cas.Type;
import org.apache.uima.cas.impl.TypeImpl;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JCasRegistry;

/** attributes to an organism such as e.g. sex, developmental stage, transplantation type ...
 * Updated by JCasGen Tue Sep 03 12:34:17 CEST 2019
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



    