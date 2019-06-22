
/* First created by JCasGen Sat Jun 22 14:44:29 CEST 2019 */
package de.julielab.jcore.types;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.cas.impl.TypeImpl;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.impl.FeatureImpl;
import org.apache.uima.cas.Feature;

/** This encodes a complex noun phrase (CNP) which is usually an extension of a ChunkNP (see, e.g., jcore-cnp-extractor-ae). A CNP is currently defined to be the top-modes noun phrase of a constituency parse tree (excluding some special phrases such as appositions and SBARs).
 * Updated by JCasGen Sat Jun 22 14:44:29 CEST 2019
 * @generated */
public class ChunkComplexNP_Type extends Annotation_Type {
  /** @generated */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = ChunkComplexNP.typeIndexID;
  /** @generated 
     @modifiable */
  @SuppressWarnings ("hiding")
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("de.julielab.jcore.types.ChunkComplexNP");
 
  /** @generated */
  final Feature casFeat_chunkID;
  /** @generated */
  final int     casFeatCode_chunkID;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public String getChunkID(int addr) {
        if (featOkTst && casFeat_chunkID == null)
      jcas.throwFeatMissing("chunkID", "de.julielab.jcore.types.ChunkComplexNP");
    return ll_cas.ll_getStringValue(addr, casFeatCode_chunkID);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setChunkID(int addr, String v) {
        if (featOkTst && casFeat_chunkID == null)
      jcas.throwFeatMissing("chunkID", "de.julielab.jcore.types.ChunkComplexNP");
    ll_cas.ll_setStringValue(addr, casFeatCode_chunkID, v);}
    
  
 
  /** @generated */
  final Feature casFeat_sentenceID;
  /** @generated */
  final int     casFeatCode_sentenceID;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public String getSentenceID(int addr) {
        if (featOkTst && casFeat_sentenceID == null)
      jcas.throwFeatMissing("sentenceID", "de.julielab.jcore.types.ChunkComplexNP");
    return ll_cas.ll_getStringValue(addr, casFeatCode_sentenceID);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setSentenceID(int addr, String v) {
        if (featOkTst && casFeat_sentenceID == null)
      jcas.throwFeatMissing("sentenceID", "de.julielab.jcore.types.ChunkComplexNP");
    ll_cas.ll_setStringValue(addr, casFeatCode_sentenceID, v);}
    
  



  /** initialize variables to correspond with Cas Type and Features
	 * @generated
	 * @param jcas JCas
	 * @param casType Type 
	 */
  public ChunkComplexNP_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

 
    casFeat_chunkID = jcas.getRequiredFeatureDE(casType, "chunkID", "uima.cas.String", featOkTst);
    casFeatCode_chunkID  = (null == casFeat_chunkID) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_chunkID).getCode();

 
    casFeat_sentenceID = jcas.getRequiredFeatureDE(casType, "sentenceID", "uima.cas.String", featOkTst);
    casFeatCode_sentenceID  = (null == casFeat_sentenceID) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_sentenceID).getCode();

  }
}



    