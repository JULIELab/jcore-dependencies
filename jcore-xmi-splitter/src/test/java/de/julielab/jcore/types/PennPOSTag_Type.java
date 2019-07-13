
/* First created by JCasGen Sat Jul 13 15:41:00 CEST 2019 */
package de.julielab.jcore.types;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.cas.impl.TypeImpl;
import org.apache.uima.cas.Type;

/** The Penn Treebank Tag Set* in Mitchell P. Marcus, Beatrice Santorini, and Mary Ann Marcinkiewicz: Building a Large Annotated Corpus of English: The Penn Treebank, in Computational Linguistics, Volume 19, Number 2 (June 1993), pp. 313--330 (Special Issue on Using Large Corpora). TagsetId 0
 * Updated by JCasGen Sat Jul 13 15:41:00 CEST 2019
 * @generated */
public class PennPOSTag_Type extends POSTag_Type {
  /** @generated */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = PennPOSTag.typeIndexID;
  /** @generated 
     @modifiable */
  @SuppressWarnings ("hiding")
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("de.julielab.jcore.types.PennPOSTag");



  /** initialize variables to correspond with Cas Type and Features
	 * @generated
	 * @param jcas JCas
	 * @param casType Type 
	 */
  public PennPOSTag_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

  }
}



    