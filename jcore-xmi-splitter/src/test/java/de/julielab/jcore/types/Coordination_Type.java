
/* First created by JCasGen Sat Jun 22 14:44:29 CEST 2019 */
package de.julielab.jcore.types;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.cas.impl.TypeImpl;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.impl.FeatureImpl;
import org.apache.uima.cas.Feature;

/** 
 * Updated by JCasGen Sat Jun 22 14:44:29 CEST 2019
 * @generated */
public class Coordination_Type extends Annotation_Type {
  /** @generated */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = Coordination.typeIndexID;
  /** @generated 
     @modifiable */
  @SuppressWarnings ("hiding")
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("de.julielab.jcore.types.Coordination");
 
  /** @generated */
  final Feature casFeat_resolved;
  /** @generated */
  final int     casFeatCode_resolved;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public String getResolved(int addr) {
        if (featOkTst && casFeat_resolved == null)
      jcas.throwFeatMissing("resolved", "de.julielab.jcore.types.Coordination");
    return ll_cas.ll_getStringValue(addr, casFeatCode_resolved);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setResolved(int addr, String v) {
        if (featOkTst && casFeat_resolved == null)
      jcas.throwFeatMissing("resolved", "de.julielab.jcore.types.Coordination");
    ll_cas.ll_setStringValue(addr, casFeatCode_resolved, v);}
    
  
 
  /** @generated */
  final Feature casFeat_elliptical;
  /** @generated */
  final int     casFeatCode_elliptical;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public boolean getElliptical(int addr) {
        if (featOkTst && casFeat_elliptical == null)
      jcas.throwFeatMissing("elliptical", "de.julielab.jcore.types.Coordination");
    return ll_cas.ll_getBooleanValue(addr, casFeatCode_elliptical);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setElliptical(int addr, boolean v) {
        if (featOkTst && casFeat_elliptical == null)
      jcas.throwFeatMissing("elliptical", "de.julielab.jcore.types.Coordination");
    ll_cas.ll_setBooleanValue(addr, casFeatCode_elliptical, v);}
    
  



  /** initialize variables to correspond with Cas Type and Features
	 * @generated
	 * @param jcas JCas
	 * @param casType Type 
	 */
  public Coordination_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

 
    casFeat_resolved = jcas.getRequiredFeatureDE(casType, "resolved", "uima.cas.String", featOkTst);
    casFeatCode_resolved  = (null == casFeat_resolved) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_resolved).getCode();

 
    casFeat_elliptical = jcas.getRequiredFeatureDE(casType, "elliptical", "uima.cas.Boolean", featOkTst);
    casFeatCode_elliptical  = (null == casFeat_elliptical) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_elliptical).getCode();

  }
}



    