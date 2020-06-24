
/* First created by JCasGen Tue Sep 03 12:34:17 CEST 2019 */
package de.julielab.jcore.types;

import org.apache.uima.cas.Type;
import org.apache.uima.cas.impl.TypeImpl;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JCasRegistry;

/** Binary Relation between Entitiy Mentions. While quite generally called "Relation", this type is commonly used for dynamic, i.e. eventive, propositional relations such as the regulation between genes.
 * Updated by JCasGen Tue Sep 03 12:34:17 CEST 2019
 * @generated */
public class RelationMention_Type extends GeneralEventMention_Type {
  /** @generated */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = RelationMention.typeIndexID;
  /** @generated 
     @modifiable */
  @SuppressWarnings ("hiding")
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("de.julielab.jcore.types.RelationMention");



  /** initialize variables to correspond with Cas Type and Features
	 * @generated
	 * @param jcas JCas
	 * @param casType Type 
	 */
  public RelationMention_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

  }
}



    