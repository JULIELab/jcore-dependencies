
/* First created by JCasGen Sun Jun 30 14:16:53 CEST 2019 */
package de.julielab.jcore.types;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.cas.impl.TypeImpl;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.impl.FeatureImpl;
import org.apache.uima.cas.Feature;

/** This type represents the span that some cue word like negation or hedging applies to.
 * Updated by JCasGen Sun Jun 30 14:16:53 CEST 2019
 * @generated */
public class Scope_Type extends Chunk_Type {
  /** @generated */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = Scope.typeIndexID;
  /** @generated 
     @modifiable */
  @SuppressWarnings ("hiding")
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("de.julielab.jcore.types.Scope");
 
  /** @generated */
  final Feature casFeat_cue;
  /** @generated */
  final int     casFeatCode_cue;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public int getCue(int addr) {
        if (featOkTst && casFeat_cue == null)
      jcas.throwFeatMissing("cue", "de.julielab.jcore.types.Scope");
    return ll_cas.ll_getRefValue(addr, casFeatCode_cue);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setCue(int addr, int v) {
        if (featOkTst && casFeat_cue == null)
      jcas.throwFeatMissing("cue", "de.julielab.jcore.types.Scope");
    ll_cas.ll_setRefValue(addr, casFeatCode_cue, v);}
    
  



  /** initialize variables to correspond with Cas Type and Features
	 * @generated
	 * @param jcas JCas
	 * @param casType Type 
	 */
  public Scope_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

 
    casFeat_cue = jcas.getRequiredFeatureDE(casType, "cue", "de.julielab.jcore.types.Annotation", featOkTst);
    casFeatCode_cue  = (null == casFeat_cue) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_cue).getCode();

  }
}



    