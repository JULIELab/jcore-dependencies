
/* First created by JCasGen Wed Aug 08 13:36:50 CEST 2018 */
package de.julielab.jcore.types;

import org.apache.uima.cas.Type;
import org.apache.uima.cas.impl.TypeImpl;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JCasRegistry;

/** For German biomedical texts, cf. Joachim Wermter and Udo Hahn. An annotated German-language medical text corpus as language resource. In LREC 2004—Proceedings of the 4th International Conference on Language Resources and Evaluation, volume 2, pages 473–476, 2004.
 * Updated by JCasGen Wed Aug 08 13:36:50 CEST 2018
 * @generated */
public class STTSMedPOSTag_Type extends POSTag_Type {
  /** @generated */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = STTSMedPOSTag.typeIndexID;
  /** @generated 
     @modifiable */
  @SuppressWarnings ("hiding")
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("de.julielab.jcore.types.STTSMedPOSTag");



  /** initialize variables to correspond with Cas Type and Features
	 * @generated
	 * @param jcas JCas
	 * @param casType Type 
	 */
  public STTSMedPOSTag_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

  }
}



    