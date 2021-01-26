
/* First created by JCasGen Tue Sep 03 12:35:27 CEST 2019 */
package de.julielab.jcore.types;

import org.apache.uima.cas.Type;
import org.apache.uima.cas.impl.TypeImpl;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JCasRegistry;

/** For general language German texts, cf. Anne Schiller, Simone Teufel, Christine Stöckert, and Christine Thielen. Guidelines für das Tagging deutscher Textcorpora mit STTS (Kleines und großes Tagset). Inst. für masch. Sprachverarbeitung, U. Stuttgart; Seminar für Sprachwissenschaft, U. Tübingen, 1999.
 * Updated by JCasGen Tue Sep 03 12:35:27 CEST 2019
 * @generated */
public class STTSPOSTag_Type extends POSTag_Type {
  /** @generated */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = STTSPOSTag.typeIndexID;
  /** @generated 
     @modifiable */
  @SuppressWarnings ("hiding")
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("de.julielab.jcore.types.STTSPOSTag");



  /** initialize variables to correspond with Cas Type and Features
	 * @generated
	 * @param jcas JCas
	 * @param casType Type 
	 */
  public STTSPOSTag_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

  }
}



    