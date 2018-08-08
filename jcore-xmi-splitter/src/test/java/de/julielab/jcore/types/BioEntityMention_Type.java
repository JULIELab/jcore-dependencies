
/* First created by JCasGen Wed Aug 08 13:36:49 CEST 2018 */
package de.julielab.jcore.types;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.cas.impl.TypeImpl;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.impl.FeatureImpl;
import org.apache.uima.cas.Feature;

/** Biomedical Entity (e.g. gene, organism, cell type)
 * Updated by JCasGen Wed Aug 08 13:36:49 CEST 2018
 * @generated */
public class BioEntityMention_Type extends EntityMention_Type {
  /** @generated */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = BioEntityMention.typeIndexID;
  /** @generated 
     @modifiable */
  @SuppressWarnings ("hiding")
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("de.julielab.jcore.types.BioEntityMention");
 
  /** @generated */
  final Feature casFeat_species;
  /** @generated */
  final int     casFeatCode_species;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public int getSpecies(int addr) {
        if (featOkTst && casFeat_species == null)
      jcas.throwFeatMissing("species", "de.julielab.jcore.types.BioEntityMention");
    return ll_cas.ll_getRefValue(addr, casFeatCode_species);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setSpecies(int addr, int v) {
        if (featOkTst && casFeat_species == null)
      jcas.throwFeatMissing("species", "de.julielab.jcore.types.BioEntityMention");
    ll_cas.ll_setRefValue(addr, casFeatCode_species, v);}
    
   /** @generated
   * @param addr low level Feature Structure reference
   * @param i index of item in the array
   * @return value at index i in the array 
   */
  public String getSpecies(int addr, int i) {
        if (featOkTst && casFeat_species == null)
      jcas.throwFeatMissing("species", "de.julielab.jcore.types.BioEntityMention");
    if (lowLevelTypeChecks)
      return ll_cas.ll_getStringArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_species), i, true);
    jcas.checkArrayBounds(ll_cas.ll_getRefValue(addr, casFeatCode_species), i);
	return ll_cas.ll_getStringArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_species), i);
  }
   
  /** @generated
   * @param addr low level Feature Structure reference
   * @param i index of item in the array
   * @param v value to set
   */ 
  public void setSpecies(int addr, int i, String v) {
        if (featOkTst && casFeat_species == null)
      jcas.throwFeatMissing("species", "de.julielab.jcore.types.BioEntityMention");
    if (lowLevelTypeChecks)
      ll_cas.ll_setStringArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_species), i, v, true);
    jcas.checkArrayBounds(ll_cas.ll_getRefValue(addr, casFeatCode_species), i);
    ll_cas.ll_setStringArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_species), i, v);
  }
 



  /** initialize variables to correspond with Cas Type and Features
	 * @generated
	 * @param jcas JCas
	 * @param casType Type 
	 */
  public BioEntityMention_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

 
    casFeat_species = jcas.getRequiredFeatureDE(casType, "species", "uima.cas.StringArray", featOkTst);
    casFeatCode_species  = (null == casFeat_species) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_species).getCode();

  }
}



    