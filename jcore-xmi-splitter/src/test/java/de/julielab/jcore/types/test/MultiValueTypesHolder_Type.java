
/* First created by JCasGen Sun Jun 30 14:16:53 CEST 2019 */
package de.julielab.jcore.types.test;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.cas.impl.TypeImpl;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.impl.FeatureImpl;
import org.apache.uima.cas.Feature;
import de.julielab.jcore.types.Annotation_Type;

/** 
 * Updated by JCasGen Sun Jun 30 14:16:53 CEST 2019
 * @generated */
public class MultiValueTypesHolder_Type extends Annotation_Type {
  /** @generated */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = MultiValueTypesHolder.typeIndexID;
  /** @generated 
     @modifiable */
  @SuppressWarnings ("hiding")
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("de.julielab.jcore.types.test.MultiValueTypesHolder");
 
  /** @generated */
  final Feature casFeat_daNoRef;
  /** @generated */
  final int     casFeatCode_daNoRef;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public int getDaNoRef(int addr) {
        if (featOkTst && casFeat_daNoRef == null)
      jcas.throwFeatMissing("daNoRef", "de.julielab.jcore.types.test.MultiValueTypesHolder");
    return ll_cas.ll_getRefValue(addr, casFeatCode_daNoRef);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setDaNoRef(int addr, int v) {
        if (featOkTst && casFeat_daNoRef == null)
      jcas.throwFeatMissing("daNoRef", "de.julielab.jcore.types.test.MultiValueTypesHolder");
    ll_cas.ll_setRefValue(addr, casFeatCode_daNoRef, v);}
    
   /** @generated
   * @param addr low level Feature Structure reference
   * @param i index of item in the array
   * @return value at index i in the array 
   */
  public double getDaNoRef(int addr, int i) {
        if (featOkTst && casFeat_daNoRef == null)
      jcas.throwFeatMissing("daNoRef", "de.julielab.jcore.types.test.MultiValueTypesHolder");
    if (lowLevelTypeChecks)
      return ll_cas.ll_getDoubleArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_daNoRef), i, true);
    jcas.checkArrayBounds(ll_cas.ll_getRefValue(addr, casFeatCode_daNoRef), i);
	return ll_cas.ll_getDoubleArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_daNoRef), i);
  }
   
  /** @generated
   * @param addr low level Feature Structure reference
   * @param i index of item in the array
   * @param v value to set
   */ 
  public void setDaNoRef(int addr, int i, double v) {
        if (featOkTst && casFeat_daNoRef == null)
      jcas.throwFeatMissing("daNoRef", "de.julielab.jcore.types.test.MultiValueTypesHolder");
    if (lowLevelTypeChecks)
      ll_cas.ll_setDoubleArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_daNoRef), i, v, true);
    jcas.checkArrayBounds(ll_cas.ll_getRefValue(addr, casFeatCode_daNoRef), i);
    ll_cas.ll_setDoubleArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_daNoRef), i, v);
  }
 
 
  /** @generated */
  final Feature casFeat_da;
  /** @generated */
  final int     casFeatCode_da;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public int getDa(int addr) {
        if (featOkTst && casFeat_da == null)
      jcas.throwFeatMissing("da", "de.julielab.jcore.types.test.MultiValueTypesHolder");
    return ll_cas.ll_getRefValue(addr, casFeatCode_da);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setDa(int addr, int v) {
        if (featOkTst && casFeat_da == null)
      jcas.throwFeatMissing("da", "de.julielab.jcore.types.test.MultiValueTypesHolder");
    ll_cas.ll_setRefValue(addr, casFeatCode_da, v);}
    
   /** @generated
   * @param addr low level Feature Structure reference
   * @param i index of item in the array
   * @return value at index i in the array 
   */
  public double getDa(int addr, int i) {
        if (featOkTst && casFeat_da == null)
      jcas.throwFeatMissing("da", "de.julielab.jcore.types.test.MultiValueTypesHolder");
    if (lowLevelTypeChecks)
      return ll_cas.ll_getDoubleArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_da), i, true);
    jcas.checkArrayBounds(ll_cas.ll_getRefValue(addr, casFeatCode_da), i);
	return ll_cas.ll_getDoubleArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_da), i);
  }
   
  /** @generated
   * @param addr low level Feature Structure reference
   * @param i index of item in the array
   * @param v value to set
   */ 
  public void setDa(int addr, int i, double v) {
        if (featOkTst && casFeat_da == null)
      jcas.throwFeatMissing("da", "de.julielab.jcore.types.test.MultiValueTypesHolder");
    if (lowLevelTypeChecks)
      ll_cas.ll_setDoubleArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_da), i, v, true);
    jcas.checkArrayBounds(ll_cas.ll_getRefValue(addr, casFeatCode_da), i);
    ll_cas.ll_setDoubleArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_da), i, v);
  }
 
 
  /** @generated */
  final Feature casFeat_sa;
  /** @generated */
  final int     casFeatCode_sa;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public int getSa(int addr) {
        if (featOkTst && casFeat_sa == null)
      jcas.throwFeatMissing("sa", "de.julielab.jcore.types.test.MultiValueTypesHolder");
    return ll_cas.ll_getRefValue(addr, casFeatCode_sa);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setSa(int addr, int v) {
        if (featOkTst && casFeat_sa == null)
      jcas.throwFeatMissing("sa", "de.julielab.jcore.types.test.MultiValueTypesHolder");
    ll_cas.ll_setRefValue(addr, casFeatCode_sa, v);}
    
   /** @generated
   * @param addr low level Feature Structure reference
   * @param i index of item in the array
   * @return value at index i in the array 
   */
  public short getSa(int addr, int i) {
        if (featOkTst && casFeat_sa == null)
      jcas.throwFeatMissing("sa", "de.julielab.jcore.types.test.MultiValueTypesHolder");
    if (lowLevelTypeChecks)
      return ll_cas.ll_getShortArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_sa), i, true);
    jcas.checkArrayBounds(ll_cas.ll_getRefValue(addr, casFeatCode_sa), i);
	return ll_cas.ll_getShortArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_sa), i);
  }
   
  /** @generated
   * @param addr low level Feature Structure reference
   * @param i index of item in the array
   * @param v value to set
   */ 
  public void setSa(int addr, int i, short v) {
        if (featOkTst && casFeat_sa == null)
      jcas.throwFeatMissing("sa", "de.julielab.jcore.types.test.MultiValueTypesHolder");
    if (lowLevelTypeChecks)
      ll_cas.ll_setShortArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_sa), i, v, true);
    jcas.checkArrayBounds(ll_cas.ll_getRefValue(addr, casFeatCode_sa), i);
    ll_cas.ll_setShortArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_sa), i, v);
  }
 
 
  /** @generated */
  final Feature casFeat_saNoRef;
  /** @generated */
  final int     casFeatCode_saNoRef;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public int getSaNoRef(int addr) {
        if (featOkTst && casFeat_saNoRef == null)
      jcas.throwFeatMissing("saNoRef", "de.julielab.jcore.types.test.MultiValueTypesHolder");
    return ll_cas.ll_getRefValue(addr, casFeatCode_saNoRef);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setSaNoRef(int addr, int v) {
        if (featOkTst && casFeat_saNoRef == null)
      jcas.throwFeatMissing("saNoRef", "de.julielab.jcore.types.test.MultiValueTypesHolder");
    ll_cas.ll_setRefValue(addr, casFeatCode_saNoRef, v);}
    
   /** @generated
   * @param addr low level Feature Structure reference
   * @param i index of item in the array
   * @return value at index i in the array 
   */
  public short getSaNoRef(int addr, int i) {
        if (featOkTst && casFeat_saNoRef == null)
      jcas.throwFeatMissing("saNoRef", "de.julielab.jcore.types.test.MultiValueTypesHolder");
    if (lowLevelTypeChecks)
      return ll_cas.ll_getShortArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_saNoRef), i, true);
    jcas.checkArrayBounds(ll_cas.ll_getRefValue(addr, casFeatCode_saNoRef), i);
	return ll_cas.ll_getShortArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_saNoRef), i);
  }
   
  /** @generated
   * @param addr low level Feature Structure reference
   * @param i index of item in the array
   * @param v value to set
   */ 
  public void setSaNoRef(int addr, int i, short v) {
        if (featOkTst && casFeat_saNoRef == null)
      jcas.throwFeatMissing("saNoRef", "de.julielab.jcore.types.test.MultiValueTypesHolder");
    if (lowLevelTypeChecks)
      ll_cas.ll_setShortArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_saNoRef), i, v, true);
    jcas.checkArrayBounds(ll_cas.ll_getRefValue(addr, casFeatCode_saNoRef), i);
    ll_cas.ll_setShortArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_saNoRef), i, v);
  }
 
 
  /** @generated */
  final Feature casFeat_ilNoRef;
  /** @generated */
  final int     casFeatCode_ilNoRef;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public int getIlNoRef(int addr) {
        if (featOkTst && casFeat_ilNoRef == null)
      jcas.throwFeatMissing("ilNoRef", "de.julielab.jcore.types.test.MultiValueTypesHolder");
    return ll_cas.ll_getRefValue(addr, casFeatCode_ilNoRef);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setIlNoRef(int addr, int v) {
        if (featOkTst && casFeat_ilNoRef == null)
      jcas.throwFeatMissing("ilNoRef", "de.julielab.jcore.types.test.MultiValueTypesHolder");
    ll_cas.ll_setRefValue(addr, casFeatCode_ilNoRef, v);}
    
  
 
  /** @generated */
  final Feature casFeat_il;
  /** @generated */
  final int     casFeatCode_il;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public int getIl(int addr) {
        if (featOkTst && casFeat_il == null)
      jcas.throwFeatMissing("il", "de.julielab.jcore.types.test.MultiValueTypesHolder");
    return ll_cas.ll_getRefValue(addr, casFeatCode_il);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setIl(int addr, int v) {
        if (featOkTst && casFeat_il == null)
      jcas.throwFeatMissing("il", "de.julielab.jcore.types.test.MultiValueTypesHolder");
    ll_cas.ll_setRefValue(addr, casFeatCode_il, v);}
    
  
 
  /** @generated */
  final Feature casFeat_fsNoRef;
  /** @generated */
  final int     casFeatCode_fsNoRef;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public int getFsNoRef(int addr) {
        if (featOkTst && casFeat_fsNoRef == null)
      jcas.throwFeatMissing("fsNoRef", "de.julielab.jcore.types.test.MultiValueTypesHolder");
    return ll_cas.ll_getRefValue(addr, casFeatCode_fsNoRef);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setFsNoRef(int addr, int v) {
        if (featOkTst && casFeat_fsNoRef == null)
      jcas.throwFeatMissing("fsNoRef", "de.julielab.jcore.types.test.MultiValueTypesHolder");
    ll_cas.ll_setRefValue(addr, casFeatCode_fsNoRef, v);}
    
   /** @generated
   * @param addr low level Feature Structure reference
   * @param i index of item in the array
   * @return value at index i in the array 
   */
  public int getFsNoRef(int addr, int i) {
        if (featOkTst && casFeat_fsNoRef == null)
      jcas.throwFeatMissing("fsNoRef", "de.julielab.jcore.types.test.MultiValueTypesHolder");
    if (lowLevelTypeChecks)
      return ll_cas.ll_getRefArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_fsNoRef), i, true);
    jcas.checkArrayBounds(ll_cas.ll_getRefValue(addr, casFeatCode_fsNoRef), i);
	return ll_cas.ll_getRefArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_fsNoRef), i);
  }
   
  /** @generated
   * @param addr low level Feature Structure reference
   * @param i index of item in the array
   * @param v value to set
   */ 
  public void setFsNoRef(int addr, int i, int v) {
        if (featOkTst && casFeat_fsNoRef == null)
      jcas.throwFeatMissing("fsNoRef", "de.julielab.jcore.types.test.MultiValueTypesHolder");
    if (lowLevelTypeChecks)
      ll_cas.ll_setRefArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_fsNoRef), i, v, true);
    jcas.checkArrayBounds(ll_cas.ll_getRefValue(addr, casFeatCode_fsNoRef), i);
    ll_cas.ll_setRefArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_fsNoRef), i, v);
  }
 
 
  /** @generated */
  final Feature casFeat_fs;
  /** @generated */
  final int     casFeatCode_fs;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public int getFs(int addr) {
        if (featOkTst && casFeat_fs == null)
      jcas.throwFeatMissing("fs", "de.julielab.jcore.types.test.MultiValueTypesHolder");
    return ll_cas.ll_getRefValue(addr, casFeatCode_fs);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setFs(int addr, int v) {
        if (featOkTst && casFeat_fs == null)
      jcas.throwFeatMissing("fs", "de.julielab.jcore.types.test.MultiValueTypesHolder");
    ll_cas.ll_setRefValue(addr, casFeatCode_fs, v);}
    
   /** @generated
   * @param addr low level Feature Structure reference
   * @param i index of item in the array
   * @return value at index i in the array 
   */
  public int getFs(int addr, int i) {
        if (featOkTst && casFeat_fs == null)
      jcas.throwFeatMissing("fs", "de.julielab.jcore.types.test.MultiValueTypesHolder");
    if (lowLevelTypeChecks)
      return ll_cas.ll_getRefArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_fs), i, true);
    jcas.checkArrayBounds(ll_cas.ll_getRefValue(addr, casFeatCode_fs), i);
	return ll_cas.ll_getRefArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_fs), i);
  }
   
  /** @generated
   * @param addr low level Feature Structure reference
   * @param i index of item in the array
   * @param v value to set
   */ 
  public void setFs(int addr, int i, int v) {
        if (featOkTst && casFeat_fs == null)
      jcas.throwFeatMissing("fs", "de.julielab.jcore.types.test.MultiValueTypesHolder");
    if (lowLevelTypeChecks)
      ll_cas.ll_setRefArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_fs), i, v, true);
    jcas.checkArrayBounds(ll_cas.ll_getRefValue(addr, casFeatCode_fs), i);
    ll_cas.ll_setRefArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_fs), i, v);
  }
 
 
  /** @generated */
  final Feature casFeat_fslistNoRef;
  /** @generated */
  final int     casFeatCode_fslistNoRef;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public int getFslistNoRef(int addr) {
        if (featOkTst && casFeat_fslistNoRef == null)
      jcas.throwFeatMissing("fslistNoRef", "de.julielab.jcore.types.test.MultiValueTypesHolder");
    return ll_cas.ll_getRefValue(addr, casFeatCode_fslistNoRef);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setFslistNoRef(int addr, int v) {
        if (featOkTst && casFeat_fslistNoRef == null)
      jcas.throwFeatMissing("fslistNoRef", "de.julielab.jcore.types.test.MultiValueTypesHolder");
    ll_cas.ll_setRefValue(addr, casFeatCode_fslistNoRef, v);}
    
  
 
  /** @generated */
  final Feature casFeat_fslist;
  /** @generated */
  final int     casFeatCode_fslist;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public int getFslist(int addr) {
        if (featOkTst && casFeat_fslist == null)
      jcas.throwFeatMissing("fslist", "de.julielab.jcore.types.test.MultiValueTypesHolder");
    return ll_cas.ll_getRefValue(addr, casFeatCode_fslist);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setFslist(int addr, int v) {
        if (featOkTst && casFeat_fslist == null)
      jcas.throwFeatMissing("fslist", "de.julielab.jcore.types.test.MultiValueTypesHolder");
    ll_cas.ll_setRefValue(addr, casFeatCode_fslist, v);}
    
  



  /** initialize variables to correspond with Cas Type and Features
	 * @generated
	 * @param jcas JCas
	 * @param casType Type 
	 */
  public MultiValueTypesHolder_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

 
    casFeat_daNoRef = jcas.getRequiredFeatureDE(casType, "daNoRef", "uima.cas.DoubleArray", featOkTst);
    casFeatCode_daNoRef  = (null == casFeat_daNoRef) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_daNoRef).getCode();

 
    casFeat_da = jcas.getRequiredFeatureDE(casType, "da", "uima.cas.DoubleArray", featOkTst);
    casFeatCode_da  = (null == casFeat_da) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_da).getCode();

 
    casFeat_sa = jcas.getRequiredFeatureDE(casType, "sa", "uima.cas.ShortArray", featOkTst);
    casFeatCode_sa  = (null == casFeat_sa) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_sa).getCode();

 
    casFeat_saNoRef = jcas.getRequiredFeatureDE(casType, "saNoRef", "uima.cas.ShortArray", featOkTst);
    casFeatCode_saNoRef  = (null == casFeat_saNoRef) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_saNoRef).getCode();

 
    casFeat_ilNoRef = jcas.getRequiredFeatureDE(casType, "ilNoRef", "uima.cas.IntegerList", featOkTst);
    casFeatCode_ilNoRef  = (null == casFeat_ilNoRef) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_ilNoRef).getCode();

 
    casFeat_il = jcas.getRequiredFeatureDE(casType, "il", "uima.cas.IntegerList", featOkTst);
    casFeatCode_il  = (null == casFeat_il) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_il).getCode();

 
    casFeat_fsNoRef = jcas.getRequiredFeatureDE(casType, "fsNoRef", "uima.cas.FSArray", featOkTst);
    casFeatCode_fsNoRef  = (null == casFeat_fsNoRef) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_fsNoRef).getCode();

 
    casFeat_fs = jcas.getRequiredFeatureDE(casType, "fs", "uima.cas.FSArray", featOkTst);
    casFeatCode_fs  = (null == casFeat_fs) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_fs).getCode();

 
    casFeat_fslistNoRef = jcas.getRequiredFeatureDE(casType, "fslistNoRef", "uima.cas.FSList", featOkTst);
    casFeatCode_fslistNoRef  = (null == casFeat_fslistNoRef) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_fslistNoRef).getCode();

 
    casFeat_fslist = jcas.getRequiredFeatureDE(casType, "fslist", "uima.cas.FSList", featOkTst);
    casFeatCode_fslist  = (null == casFeat_fslist) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_fslist).getCode();

  }
}



    