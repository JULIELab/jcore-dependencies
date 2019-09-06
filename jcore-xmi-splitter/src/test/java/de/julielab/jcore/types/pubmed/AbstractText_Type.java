
/* First created by JCasGen Tue Sep 03 12:34:17 CEST 2019 */
package de.julielab.jcore.types.pubmed;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.cas.impl.TypeImpl;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.impl.FeatureImpl;
import org.apache.uima.cas.Feature;

/** Annotation of the complete abstract.
 * Updated by JCasGen Tue Sep 03 12:34:17 CEST 2019
 * @generated */
public class AbstractText_Type extends de.julielab.jcore.types.AbstractText_Type {
  /** @generated */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = AbstractText.typeIndexID;
  /** @generated 
     @modifiable */
  @SuppressWarnings ("hiding")
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("de.julielab.jcore.types.pubmed.AbstractText");
 
  /** @generated */
  final Feature casFeat_abstractType;
  /** @generated */
  final int     casFeatCode_abstractType;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public String getAbstractType(int addr) {
        if (featOkTst && casFeat_abstractType == null)
      jcas.throwFeatMissing("abstractType", "de.julielab.jcore.types.pubmed.AbstractText");
    return ll_cas.ll_getStringValue(addr, casFeatCode_abstractType);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setAbstractType(int addr, String v) {
        if (featOkTst && casFeat_abstractType == null)
      jcas.throwFeatMissing("abstractType", "de.julielab.jcore.types.pubmed.AbstractText");
    ll_cas.ll_setStringValue(addr, casFeatCode_abstractType, v);}
    
  



  /** initialize variables to correspond with Cas Type and Features
	 * @generated
	 * @param jcas JCas
	 * @param casType Type 
	 */
  public AbstractText_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

 
    casFeat_abstractType = jcas.getRequiredFeatureDE(casType, "abstractType", "de.julielab.jcore.types.pubmed.AbstractType", featOkTst);
    casFeatCode_abstractType  = (null == casFeat_abstractType) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_abstractType).getCode();

  }
}



    