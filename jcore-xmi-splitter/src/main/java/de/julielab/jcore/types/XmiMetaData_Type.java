
/* First created by JCasGen Thu Jun 29 12:11:09 CEST 2017 */
package de.julielab.jcore.types;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.cas.impl.TypeImpl;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.impl.FeatureImpl;
import org.apache.uima.cas.Feature;
import org.apache.uima.jcas.tcas.Annotation_Type;

/** This type stores required information for the appropiate handling of consistent XMI deserialization in the XMI reader and serialization in the XMI consumer with respect to the separate storage of annotations.
 * Updated by JCasGen Fri Jun 30 13:54:20 CEST 2017
 * @generated */
public class XmiMetaData_Type extends Annotation_Type {
  /** @generated */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = XmiMetaData.typeIndexID;
  /** @generated 
     @modifiable */
  @SuppressWarnings ("hiding")
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("de.julielab.jcore.types.XmiMetaData");
 
  /** @generated */
  final Feature casFeat_maxXmiId;
  /** @generated */
  final int     casFeatCode_maxXmiId;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public int getMaxXmiId(int addr) {
        if (featOkTst && casFeat_maxXmiId == null)
      jcas.throwFeatMissing("maxXmiId", "de.julielab.jcore.types.XmiMetaData");
    return ll_cas.ll_getIntValue(addr, casFeatCode_maxXmiId);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setMaxXmiId(int addr, int v) {
        if (featOkTst && casFeat_maxXmiId == null)
      jcas.throwFeatMissing("maxXmiId", "de.julielab.jcore.types.XmiMetaData");
    ll_cas.ll_setIntValue(addr, casFeatCode_maxXmiId, v);}
    
  
 
  /** @generated */
  final Feature casFeat_sofaIdMappings;
  /** @generated */
  final int     casFeatCode_sofaIdMappings;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public int getSofaIdMappings(int addr) {
        if (featOkTst && casFeat_sofaIdMappings == null)
      jcas.throwFeatMissing("sofaIdMappings", "de.julielab.jcore.types.XmiMetaData");
    return ll_cas.ll_getRefValue(addr, casFeatCode_sofaIdMappings);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setSofaIdMappings(int addr, int v) {
        if (featOkTst && casFeat_sofaIdMappings == null)
      jcas.throwFeatMissing("sofaIdMappings", "de.julielab.jcore.types.XmiMetaData");
    ll_cas.ll_setRefValue(addr, casFeatCode_sofaIdMappings, v);}
    
   /** @generated
   * @param addr low level Feature Structure reference
   * @param i index of item in the array
   * @return value at index i in the array 
   */
  public String getSofaIdMappings(int addr, int i) {
        if (featOkTst && casFeat_sofaIdMappings == null)
      jcas.throwFeatMissing("sofaIdMappings", "de.julielab.jcore.types.XmiMetaData");
    if (lowLevelTypeChecks)
      return ll_cas.ll_getStringArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_sofaIdMappings), i, true);
    jcas.checkArrayBounds(ll_cas.ll_getRefValue(addr, casFeatCode_sofaIdMappings), i);
  return ll_cas.ll_getStringArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_sofaIdMappings), i);
  }
   
  /** @generated
   * @param addr low level Feature Structure reference
   * @param i index of item in the array
   * @param v value to set
   */ 
  public void setSofaIdMappings(int addr, int i, String v) {
        if (featOkTst && casFeat_sofaIdMappings == null)
      jcas.throwFeatMissing("sofaIdMappings", "de.julielab.jcore.types.XmiMetaData");
    if (lowLevelTypeChecks)
      ll_cas.ll_setStringArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_sofaIdMappings), i, v, true);
    jcas.checkArrayBounds(ll_cas.ll_getRefValue(addr, casFeatCode_sofaIdMappings), i);
    ll_cas.ll_setStringArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_sofaIdMappings), i, v);
  }
 



  /** initialize variables to correspond with Cas Type and Features
	 * @generated
	 * @param jcas JCas
	 * @param casType Type 
	 */
  public XmiMetaData_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

 
    casFeat_maxXmiId = jcas.getRequiredFeatureDE(casType, "maxXmiId", "uima.cas.Integer", featOkTst);
    casFeatCode_maxXmiId  = (null == casFeat_maxXmiId) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_maxXmiId).getCode();

 
    casFeat_sofaIdMappings = jcas.getRequiredFeatureDE(casType, "sofaIdMappings", "uima.cas.StringArray", featOkTst);
    casFeatCode_sofaIdMappings  = (null == casFeat_sofaIdMappings) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_sofaIdMappings).getCode();

  }
}



    