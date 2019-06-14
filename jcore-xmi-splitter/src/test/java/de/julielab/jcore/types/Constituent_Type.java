
/* First created by JCasGen Wed Aug 08 13:36:49 CEST 2018 */
package de.julielab.jcore.types;

import org.apache.uima.cas.Feature;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.impl.FeatureImpl;
import org.apache.uima.cas.impl.TypeImpl;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JCasRegistry;

/** The super-type for the constituent annotation, see subtypes
 * Updated by JCasGen Wed Aug 08 13:36:49 CEST 2018
 * @generated */
public class Constituent_Type extends Annotation_Type {
  /** @generated */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = Constituent.typeIndexID;
  /** @generated 
     @modifiable */
  @SuppressWarnings ("hiding")
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("de.julielab.jcore.types.Constituent");
 
  /** @generated */
  final Feature casFeat_parent;
  /** @generated */
  final int     casFeatCode_parent;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public int getParent(int addr) {
        if (featOkTst && casFeat_parent == null)
      jcas.throwFeatMissing("parent", "de.julielab.jcore.types.Constituent");
    return ll_cas.ll_getRefValue(addr, casFeatCode_parent);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setParent(int addr, int v) {
        if (featOkTst && casFeat_parent == null)
      jcas.throwFeatMissing("parent", "de.julielab.jcore.types.Constituent");
    ll_cas.ll_setRefValue(addr, casFeatCode_parent, v);}
    
  
 
  /** @generated */
  final Feature casFeat_head;
  /** @generated */
  final int     casFeatCode_head;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public int getHead(int addr) {
        if (featOkTst && casFeat_head == null)
      jcas.throwFeatMissing("head", "de.julielab.jcore.types.Constituent");
    return ll_cas.ll_getRefValue(addr, casFeatCode_head);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setHead(int addr, int v) {
        if (featOkTst && casFeat_head == null)
      jcas.throwFeatMissing("head", "de.julielab.jcore.types.Constituent");
    ll_cas.ll_setRefValue(addr, casFeatCode_head, v);}
    
  
 
  /** @generated */
  final Feature casFeat_cat;
  /** @generated */
  final int     casFeatCode_cat;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public String getCat(int addr) {
        if (featOkTst && casFeat_cat == null)
      jcas.throwFeatMissing("cat", "de.julielab.jcore.types.Constituent");
    return ll_cas.ll_getStringValue(addr, casFeatCode_cat);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setCat(int addr, String v) {
        if (featOkTst && casFeat_cat == null)
      jcas.throwFeatMissing("cat", "de.julielab.jcore.types.Constituent");
    ll_cas.ll_setStringValue(addr, casFeatCode_cat, v);}
    
  



  /** initialize variables to correspond with Cas Type and Features
	 * @generated
	 * @param jcas JCas
	 * @param casType Type 
	 */
  public Constituent_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

 
    casFeat_parent = jcas.getRequiredFeatureDE(casType, "parent", "de.julielab.jcore.types.Constituent", featOkTst);
    casFeatCode_parent  = (null == casFeat_parent) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_parent).getCode();

 
    casFeat_head = jcas.getRequiredFeatureDE(casType, "head", "de.julielab.jcore.types.Token", featOkTst);
    casFeatCode_head  = (null == casFeat_head) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_head).getCode();

 
    casFeat_cat = jcas.getRequiredFeatureDE(casType, "cat", "uima.cas.String", featOkTst);
    casFeatCode_cat  = (null == casFeat_cat) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_cat).getCode();

  }
}



    