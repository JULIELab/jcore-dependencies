
/* First created by JCasGen Sun Jun 30 14:16:53 CEST 2019 */
package de.julielab.jcore.types;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.cas.impl.TypeImpl;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.impl.FeatureImpl;
import org.apache.uima.cas.Feature;

/** Penn Treebank constituent annotation (see Penn Treebank)
 * Updated by JCasGen Sun Jun 30 14:16:53 CEST 2019
 * @generated */
public class PTBConstituent_Type extends Constituent_Type {
  /** @generated */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = PTBConstituent.typeIndexID;
  /** @generated 
     @modifiable */
  @SuppressWarnings ("hiding")
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("de.julielab.jcore.types.PTBConstituent");
 
  /** @generated */
  final Feature casFeat_formFuncDisc;
  /** @generated */
  final int     casFeatCode_formFuncDisc;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public String getFormFuncDisc(int addr) {
        if (featOkTst && casFeat_formFuncDisc == null)
      jcas.throwFeatMissing("formFuncDisc", "de.julielab.jcore.types.PTBConstituent");
    return ll_cas.ll_getStringValue(addr, casFeatCode_formFuncDisc);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setFormFuncDisc(int addr, String v) {
        if (featOkTst && casFeat_formFuncDisc == null)
      jcas.throwFeatMissing("formFuncDisc", "de.julielab.jcore.types.PTBConstituent");
    ll_cas.ll_setStringValue(addr, casFeatCode_formFuncDisc, v);}
    
  
 
  /** @generated */
  final Feature casFeat_gramRole;
  /** @generated */
  final int     casFeatCode_gramRole;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public String getGramRole(int addr) {
        if (featOkTst && casFeat_gramRole == null)
      jcas.throwFeatMissing("gramRole", "de.julielab.jcore.types.PTBConstituent");
    return ll_cas.ll_getStringValue(addr, casFeatCode_gramRole);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setGramRole(int addr, String v) {
        if (featOkTst && casFeat_gramRole == null)
      jcas.throwFeatMissing("gramRole", "de.julielab.jcore.types.PTBConstituent");
    ll_cas.ll_setStringValue(addr, casFeatCode_gramRole, v);}
    
  
 
  /** @generated */
  final Feature casFeat_adv;
  /** @generated */
  final int     casFeatCode_adv;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public String getAdv(int addr) {
        if (featOkTst && casFeat_adv == null)
      jcas.throwFeatMissing("adv", "de.julielab.jcore.types.PTBConstituent");
    return ll_cas.ll_getStringValue(addr, casFeatCode_adv);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setAdv(int addr, String v) {
        if (featOkTst && casFeat_adv == null)
      jcas.throwFeatMissing("adv", "de.julielab.jcore.types.PTBConstituent");
    ll_cas.ll_setStringValue(addr, casFeatCode_adv, v);}
    
  
 
  /** @generated */
  final Feature casFeat_misc;
  /** @generated */
  final int     casFeatCode_misc;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public String getMisc(int addr) {
        if (featOkTst && casFeat_misc == null)
      jcas.throwFeatMissing("misc", "de.julielab.jcore.types.PTBConstituent");
    return ll_cas.ll_getStringValue(addr, casFeatCode_misc);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setMisc(int addr, String v) {
        if (featOkTst && casFeat_misc == null)
      jcas.throwFeatMissing("misc", "de.julielab.jcore.types.PTBConstituent");
    ll_cas.ll_setStringValue(addr, casFeatCode_misc, v);}
    
  
 
  /** @generated */
  final Feature casFeat_nullElement;
  /** @generated */
  final int     casFeatCode_nullElement;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public String getNullElement(int addr) {
        if (featOkTst && casFeat_nullElement == null)
      jcas.throwFeatMissing("nullElement", "de.julielab.jcore.types.PTBConstituent");
    return ll_cas.ll_getStringValue(addr, casFeatCode_nullElement);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setNullElement(int addr, String v) {
        if (featOkTst && casFeat_nullElement == null)
      jcas.throwFeatMissing("nullElement", "de.julielab.jcore.types.PTBConstituent");
    ll_cas.ll_setStringValue(addr, casFeatCode_nullElement, v);}
    
  
 
  /** @generated */
  final Feature casFeat_ref;
  /** @generated */
  final int     casFeatCode_ref;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public int getRef(int addr) {
        if (featOkTst && casFeat_ref == null)
      jcas.throwFeatMissing("ref", "de.julielab.jcore.types.PTBConstituent");
    return ll_cas.ll_getRefValue(addr, casFeatCode_ref);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setRef(int addr, int v) {
        if (featOkTst && casFeat_ref == null)
      jcas.throwFeatMissing("ref", "de.julielab.jcore.types.PTBConstituent");
    ll_cas.ll_setRefValue(addr, casFeatCode_ref, v);}
    
  
 
  /** @generated */
  final Feature casFeat_map;
  /** @generated */
  final int     casFeatCode_map;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public int getMap(int addr) {
        if (featOkTst && casFeat_map == null)
      jcas.throwFeatMissing("map", "de.julielab.jcore.types.PTBConstituent");
    return ll_cas.ll_getRefValue(addr, casFeatCode_map);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setMap(int addr, int v) {
        if (featOkTst && casFeat_map == null)
      jcas.throwFeatMissing("map", "de.julielab.jcore.types.PTBConstituent");
    ll_cas.ll_setRefValue(addr, casFeatCode_map, v);}
    
  
 
  /** @generated */
  final Feature casFeat_tpc;
  /** @generated */
  final int     casFeatCode_tpc;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public boolean getTpc(int addr) {
        if (featOkTst && casFeat_tpc == null)
      jcas.throwFeatMissing("tpc", "de.julielab.jcore.types.PTBConstituent");
    return ll_cas.ll_getBooleanValue(addr, casFeatCode_tpc);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setTpc(int addr, boolean v) {
        if (featOkTst && casFeat_tpc == null)
      jcas.throwFeatMissing("tpc", "de.julielab.jcore.types.PTBConstituent");
    ll_cas.ll_setBooleanValue(addr, casFeatCode_tpc, v);}
    
  



  /** initialize variables to correspond with Cas Type and Features
	 * @generated
	 * @param jcas JCas
	 * @param casType Type 
	 */
  public PTBConstituent_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

 
    casFeat_formFuncDisc = jcas.getRequiredFeatureDE(casType, "formFuncDisc", "uima.cas.String", featOkTst);
    casFeatCode_formFuncDisc  = (null == casFeat_formFuncDisc) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_formFuncDisc).getCode();

 
    casFeat_gramRole = jcas.getRequiredFeatureDE(casType, "gramRole", "uima.cas.String", featOkTst);
    casFeatCode_gramRole  = (null == casFeat_gramRole) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_gramRole).getCode();

 
    casFeat_adv = jcas.getRequiredFeatureDE(casType, "adv", "uima.cas.String", featOkTst);
    casFeatCode_adv  = (null == casFeat_adv) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_adv).getCode();

 
    casFeat_misc = jcas.getRequiredFeatureDE(casType, "misc", "uima.cas.String", featOkTst);
    casFeatCode_misc  = (null == casFeat_misc) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_misc).getCode();

 
    casFeat_nullElement = jcas.getRequiredFeatureDE(casType, "nullElement", "uima.cas.String", featOkTst);
    casFeatCode_nullElement  = (null == casFeat_nullElement) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_nullElement).getCode();

 
    casFeat_ref = jcas.getRequiredFeatureDE(casType, "ref", "de.julielab.jcore.types.Constituent", featOkTst);
    casFeatCode_ref  = (null == casFeat_ref) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_ref).getCode();

 
    casFeat_map = jcas.getRequiredFeatureDE(casType, "map", "de.julielab.jcore.types.Constituent", featOkTst);
    casFeatCode_map  = (null == casFeat_map) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_map).getCode();

 
    casFeat_tpc = jcas.getRequiredFeatureDE(casType, "tpc", "uima.cas.Boolean", featOkTst);
    casFeatCode_tpc  = (null == casFeat_tpc) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_tpc).getCode();

  }
}



    