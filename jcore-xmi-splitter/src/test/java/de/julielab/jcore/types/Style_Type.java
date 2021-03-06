
/* First created by JCasGen Tue Sep 03 12:35:27 CEST 2019 */
package de.julielab.jcore.types;

import org.apache.uima.cas.Feature;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.impl.FeatureImpl;
import org.apache.uima.cas.impl.TypeImpl;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JCasRegistry;

/** Text-markup information (italic, bold etc.) on any (e.g. character) level. Allows to keep the original style markup of the text, several style types might be set to same (or overlapping) range, when different styles are set to the same text region.
 * Updated by JCasGen Tue Sep 03 12:35:27 CEST 2019
 * @generated */
public class Style_Type extends Annotation_Type {
  /** @generated */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = Style.typeIndexID;
  /** @generated 
     @modifiable */
  @SuppressWarnings ("hiding")
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("de.julielab.jcore.types.Style");
 
  /** @generated */
  final Feature casFeat_styleName;
  /** @generated */
  final int     casFeatCode_styleName;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public String getStyleName(int addr) {
        if (featOkTst && casFeat_styleName == null)
      jcas.throwFeatMissing("styleName", "de.julielab.jcore.types.Style");
    return ll_cas.ll_getStringValue(addr, casFeatCode_styleName);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setStyleName(int addr, String v) {
        if (featOkTst && casFeat_styleName == null)
      jcas.throwFeatMissing("styleName", "de.julielab.jcore.types.Style");
    ll_cas.ll_setStringValue(addr, casFeatCode_styleName, v);}
    
  
 
  /** @generated */
  final Feature casFeat_encoding;
  /** @generated */
  final int     casFeatCode_encoding;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public String getEncoding(int addr) {
        if (featOkTst && casFeat_encoding == null)
      jcas.throwFeatMissing("encoding", "de.julielab.jcore.types.Style");
    return ll_cas.ll_getStringValue(addr, casFeatCode_encoding);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setEncoding(int addr, String v) {
        if (featOkTst && casFeat_encoding == null)
      jcas.throwFeatMissing("encoding", "de.julielab.jcore.types.Style");
    ll_cas.ll_setStringValue(addr, casFeatCode_encoding, v);}
    
  



  /** initialize variables to correspond with Cas Type and Features
	 * @generated
	 * @param jcas JCas
	 * @param casType Type 
	 */
  public Style_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

 
    casFeat_styleName = jcas.getRequiredFeatureDE(casType, "styleName", "de.julielab.jcore.types.StyleName", featOkTst);
    casFeatCode_styleName  = (null == casFeat_styleName) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_styleName).getCode();

 
    casFeat_encoding = jcas.getRequiredFeatureDE(casType, "encoding", "de.julielab.jcore.types.Encoding", featOkTst);
    casFeatCode_encoding  = (null == casFeat_encoding) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_encoding).getCode();

  }
}



    