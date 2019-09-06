

/* First created by JCasGen Tue Sep 03 12:35:27 CEST 2019 */
package de.julielab.jcore.types.test;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;

import org.apache.uima.jcas.cas.FSArray;
import org.apache.uima.jcas.cas.FSList;
import org.apache.uima.jcas.cas.StringList;
import org.apache.uima.jcas.cas.IntegerList;
import org.apache.uima.jcas.cas.ShortArray;
import org.apache.uima.jcas.cas.DoubleArray;
import de.julielab.jcore.types.Annotation;


/** 
 * Updated by JCasGen Tue Sep 03 12:35:27 CEST 2019
 * XML source: arrayAndListHolderTestType.xml
 * @generated */
public class MultiValueTypesHolder extends Annotation {
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = JCasRegistry.register(MultiValueTypesHolder.class);
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static int type = typeIndexID;
  /** @generated
   * @return index of the type  
   */
  @Override
  public              int getTypeIndexID() {return typeIndexID;}
 
  /** Never called.  Disable default constructor
   * @generated */
  protected MultiValueTypesHolder() {/* intentionally empty block */}
    
  /** Internal - constructor used by generator 
   * @generated
   * @param addr low level Feature Structure reference
   * @param type the type of this Feature Structure 
   */
  public MultiValueTypesHolder(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated
   * @param jcas JCas to which this Feature Structure belongs 
   */
  public MultiValueTypesHolder(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated
   * @param jcas JCas to which this Feature Structure belongs
   * @param begin offset to the begin spot in the SofA
   * @param end offset to the end spot in the SofA 
  */  
  public MultiValueTypesHolder(JCas jcas, int begin, int end) {
    super(jcas);
    setBegin(begin);
    setEnd(end);
    readObject();
  }   

  /** 
   * <!-- begin-user-doc -->
   * Write your own initialization here
   * <!-- end-user-doc -->
   *
   * @generated modifiable 
   */
  private void readObject() {/*default - does nothing empty block */}
     
 
    
  //*--------------*
  //* Feature: daNoRef

  /** getter for daNoRef - gets 
   * @generated
   * @return value of the feature 
   */
  public DoubleArray getDaNoRef() {
    if (MultiValueTypesHolder_Type.featOkTst && ((MultiValueTypesHolder_Type)jcasType).casFeat_daNoRef == null)
      jcasType.jcas.throwFeatMissing("daNoRef", "de.julielab.jcore.types.test.MultiValueTypesHolder");
    return (DoubleArray)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((MultiValueTypesHolder_Type)jcasType).casFeatCode_daNoRef)));}
    
  /** setter for daNoRef - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setDaNoRef(DoubleArray v) {
    if (MultiValueTypesHolder_Type.featOkTst && ((MultiValueTypesHolder_Type)jcasType).casFeat_daNoRef == null)
      jcasType.jcas.throwFeatMissing("daNoRef", "de.julielab.jcore.types.test.MultiValueTypesHolder");
    jcasType.ll_cas.ll_setRefValue(addr, ((MultiValueTypesHolder_Type)jcasType).casFeatCode_daNoRef, jcasType.ll_cas.ll_getFSRef(v));}    
    
  /** indexed getter for daNoRef - gets an indexed value - 
   * @generated
   * @param i index in the array to get
   * @return value of the element at index i 
   */
  public double getDaNoRef(int i) {
    if (MultiValueTypesHolder_Type.featOkTst && ((MultiValueTypesHolder_Type)jcasType).casFeat_daNoRef == null)
      jcasType.jcas.throwFeatMissing("daNoRef", "de.julielab.jcore.types.test.MultiValueTypesHolder");
    jcasType.jcas.checkArrayBounds(jcasType.ll_cas.ll_getRefValue(addr, ((MultiValueTypesHolder_Type)jcasType).casFeatCode_daNoRef), i);
    return jcasType.ll_cas.ll_getDoubleArrayValue(jcasType.ll_cas.ll_getRefValue(addr, ((MultiValueTypesHolder_Type)jcasType).casFeatCode_daNoRef), i);}

  /** indexed setter for daNoRef - sets an indexed value - 
   * @generated
   * @param i index in the array to set
   * @param v value to set into the array 
   */
  public void setDaNoRef(int i, double v) { 
    if (MultiValueTypesHolder_Type.featOkTst && ((MultiValueTypesHolder_Type)jcasType).casFeat_daNoRef == null)
      jcasType.jcas.throwFeatMissing("daNoRef", "de.julielab.jcore.types.test.MultiValueTypesHolder");
    jcasType.jcas.checkArrayBounds(jcasType.ll_cas.ll_getRefValue(addr, ((MultiValueTypesHolder_Type)jcasType).casFeatCode_daNoRef), i);
    jcasType.ll_cas.ll_setDoubleArrayValue(jcasType.ll_cas.ll_getRefValue(addr, ((MultiValueTypesHolder_Type)jcasType).casFeatCode_daNoRef), i, v);}
   
    
  //*--------------*
  //* Feature: da

  /** getter for da - gets 
   * @generated
   * @return value of the feature 
   */
  public DoubleArray getDa() {
    if (MultiValueTypesHolder_Type.featOkTst && ((MultiValueTypesHolder_Type)jcasType).casFeat_da == null)
      jcasType.jcas.throwFeatMissing("da", "de.julielab.jcore.types.test.MultiValueTypesHolder");
    return (DoubleArray)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((MultiValueTypesHolder_Type)jcasType).casFeatCode_da)));}
    
  /** setter for da - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setDa(DoubleArray v) {
    if (MultiValueTypesHolder_Type.featOkTst && ((MultiValueTypesHolder_Type)jcasType).casFeat_da == null)
      jcasType.jcas.throwFeatMissing("da", "de.julielab.jcore.types.test.MultiValueTypesHolder");
    jcasType.ll_cas.ll_setRefValue(addr, ((MultiValueTypesHolder_Type)jcasType).casFeatCode_da, jcasType.ll_cas.ll_getFSRef(v));}    
    
  /** indexed getter for da - gets an indexed value - 
   * @generated
   * @param i index in the array to get
   * @return value of the element at index i 
   */
  public double getDa(int i) {
    if (MultiValueTypesHolder_Type.featOkTst && ((MultiValueTypesHolder_Type)jcasType).casFeat_da == null)
      jcasType.jcas.throwFeatMissing("da", "de.julielab.jcore.types.test.MultiValueTypesHolder");
    jcasType.jcas.checkArrayBounds(jcasType.ll_cas.ll_getRefValue(addr, ((MultiValueTypesHolder_Type)jcasType).casFeatCode_da), i);
    return jcasType.ll_cas.ll_getDoubleArrayValue(jcasType.ll_cas.ll_getRefValue(addr, ((MultiValueTypesHolder_Type)jcasType).casFeatCode_da), i);}

  /** indexed setter for da - sets an indexed value - 
   * @generated
   * @param i index in the array to set
   * @param v value to set into the array 
   */
  public void setDa(int i, double v) { 
    if (MultiValueTypesHolder_Type.featOkTst && ((MultiValueTypesHolder_Type)jcasType).casFeat_da == null)
      jcasType.jcas.throwFeatMissing("da", "de.julielab.jcore.types.test.MultiValueTypesHolder");
    jcasType.jcas.checkArrayBounds(jcasType.ll_cas.ll_getRefValue(addr, ((MultiValueTypesHolder_Type)jcasType).casFeatCode_da), i);
    jcasType.ll_cas.ll_setDoubleArrayValue(jcasType.ll_cas.ll_getRefValue(addr, ((MultiValueTypesHolder_Type)jcasType).casFeatCode_da), i, v);}
   
    
  //*--------------*
  //* Feature: sa

  /** getter for sa - gets 
   * @generated
   * @return value of the feature 
   */
  public ShortArray getSa() {
    if (MultiValueTypesHolder_Type.featOkTst && ((MultiValueTypesHolder_Type)jcasType).casFeat_sa == null)
      jcasType.jcas.throwFeatMissing("sa", "de.julielab.jcore.types.test.MultiValueTypesHolder");
    return (ShortArray)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((MultiValueTypesHolder_Type)jcasType).casFeatCode_sa)));}
    
  /** setter for sa - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setSa(ShortArray v) {
    if (MultiValueTypesHolder_Type.featOkTst && ((MultiValueTypesHolder_Type)jcasType).casFeat_sa == null)
      jcasType.jcas.throwFeatMissing("sa", "de.julielab.jcore.types.test.MultiValueTypesHolder");
    jcasType.ll_cas.ll_setRefValue(addr, ((MultiValueTypesHolder_Type)jcasType).casFeatCode_sa, jcasType.ll_cas.ll_getFSRef(v));}    
    
  /** indexed getter for sa - gets an indexed value - 
   * @generated
   * @param i index in the array to get
   * @return value of the element at index i 
   */
  public short getSa(int i) {
    if (MultiValueTypesHolder_Type.featOkTst && ((MultiValueTypesHolder_Type)jcasType).casFeat_sa == null)
      jcasType.jcas.throwFeatMissing("sa", "de.julielab.jcore.types.test.MultiValueTypesHolder");
    jcasType.jcas.checkArrayBounds(jcasType.ll_cas.ll_getRefValue(addr, ((MultiValueTypesHolder_Type)jcasType).casFeatCode_sa), i);
    return jcasType.ll_cas.ll_getShortArrayValue(jcasType.ll_cas.ll_getRefValue(addr, ((MultiValueTypesHolder_Type)jcasType).casFeatCode_sa), i);}

  /** indexed setter for sa - sets an indexed value - 
   * @generated
   * @param i index in the array to set
   * @param v value to set into the array 
   */
  public void setSa(int i, short v) { 
    if (MultiValueTypesHolder_Type.featOkTst && ((MultiValueTypesHolder_Type)jcasType).casFeat_sa == null)
      jcasType.jcas.throwFeatMissing("sa", "de.julielab.jcore.types.test.MultiValueTypesHolder");
    jcasType.jcas.checkArrayBounds(jcasType.ll_cas.ll_getRefValue(addr, ((MultiValueTypesHolder_Type)jcasType).casFeatCode_sa), i);
    jcasType.ll_cas.ll_setShortArrayValue(jcasType.ll_cas.ll_getRefValue(addr, ((MultiValueTypesHolder_Type)jcasType).casFeatCode_sa), i, v);}
   
    
  //*--------------*
  //* Feature: saNoRef

  /** getter for saNoRef - gets 
   * @generated
   * @return value of the feature 
   */
  public ShortArray getSaNoRef() {
    if (MultiValueTypesHolder_Type.featOkTst && ((MultiValueTypesHolder_Type)jcasType).casFeat_saNoRef == null)
      jcasType.jcas.throwFeatMissing("saNoRef", "de.julielab.jcore.types.test.MultiValueTypesHolder");
    return (ShortArray)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((MultiValueTypesHolder_Type)jcasType).casFeatCode_saNoRef)));}
    
  /** setter for saNoRef - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setSaNoRef(ShortArray v) {
    if (MultiValueTypesHolder_Type.featOkTst && ((MultiValueTypesHolder_Type)jcasType).casFeat_saNoRef == null)
      jcasType.jcas.throwFeatMissing("saNoRef", "de.julielab.jcore.types.test.MultiValueTypesHolder");
    jcasType.ll_cas.ll_setRefValue(addr, ((MultiValueTypesHolder_Type)jcasType).casFeatCode_saNoRef, jcasType.ll_cas.ll_getFSRef(v));}    
    
  /** indexed getter for saNoRef - gets an indexed value - 
   * @generated
   * @param i index in the array to get
   * @return value of the element at index i 
   */
  public short getSaNoRef(int i) {
    if (MultiValueTypesHolder_Type.featOkTst && ((MultiValueTypesHolder_Type)jcasType).casFeat_saNoRef == null)
      jcasType.jcas.throwFeatMissing("saNoRef", "de.julielab.jcore.types.test.MultiValueTypesHolder");
    jcasType.jcas.checkArrayBounds(jcasType.ll_cas.ll_getRefValue(addr, ((MultiValueTypesHolder_Type)jcasType).casFeatCode_saNoRef), i);
    return jcasType.ll_cas.ll_getShortArrayValue(jcasType.ll_cas.ll_getRefValue(addr, ((MultiValueTypesHolder_Type)jcasType).casFeatCode_saNoRef), i);}

  /** indexed setter for saNoRef - sets an indexed value - 
   * @generated
   * @param i index in the array to set
   * @param v value to set into the array 
   */
  public void setSaNoRef(int i, short v) { 
    if (MultiValueTypesHolder_Type.featOkTst && ((MultiValueTypesHolder_Type)jcasType).casFeat_saNoRef == null)
      jcasType.jcas.throwFeatMissing("saNoRef", "de.julielab.jcore.types.test.MultiValueTypesHolder");
    jcasType.jcas.checkArrayBounds(jcasType.ll_cas.ll_getRefValue(addr, ((MultiValueTypesHolder_Type)jcasType).casFeatCode_saNoRef), i);
    jcasType.ll_cas.ll_setShortArrayValue(jcasType.ll_cas.ll_getRefValue(addr, ((MultiValueTypesHolder_Type)jcasType).casFeatCode_saNoRef), i, v);}
   
    
  //*--------------*
  //* Feature: ilNoRef

  /** getter for ilNoRef - gets 
   * @generated
   * @return value of the feature 
   */
  public IntegerList getIlNoRef() {
    if (MultiValueTypesHolder_Type.featOkTst && ((MultiValueTypesHolder_Type)jcasType).casFeat_ilNoRef == null)
      jcasType.jcas.throwFeatMissing("ilNoRef", "de.julielab.jcore.types.test.MultiValueTypesHolder");
    return (IntegerList)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((MultiValueTypesHolder_Type)jcasType).casFeatCode_ilNoRef)));}
    
  /** setter for ilNoRef - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setIlNoRef(IntegerList v) {
    if (MultiValueTypesHolder_Type.featOkTst && ((MultiValueTypesHolder_Type)jcasType).casFeat_ilNoRef == null)
      jcasType.jcas.throwFeatMissing("ilNoRef", "de.julielab.jcore.types.test.MultiValueTypesHolder");
    jcasType.ll_cas.ll_setRefValue(addr, ((MultiValueTypesHolder_Type)jcasType).casFeatCode_ilNoRef, jcasType.ll_cas.ll_getFSRef(v));}    
   
    
  //*--------------*
  //* Feature: il

  /** getter for il - gets 
   * @generated
   * @return value of the feature 
   */
  public IntegerList getIl() {
    if (MultiValueTypesHolder_Type.featOkTst && ((MultiValueTypesHolder_Type)jcasType).casFeat_il == null)
      jcasType.jcas.throwFeatMissing("il", "de.julielab.jcore.types.test.MultiValueTypesHolder");
    return (IntegerList)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((MultiValueTypesHolder_Type)jcasType).casFeatCode_il)));}
    
  /** setter for il - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setIl(IntegerList v) {
    if (MultiValueTypesHolder_Type.featOkTst && ((MultiValueTypesHolder_Type)jcasType).casFeat_il == null)
      jcasType.jcas.throwFeatMissing("il", "de.julielab.jcore.types.test.MultiValueTypesHolder");
    jcasType.ll_cas.ll_setRefValue(addr, ((MultiValueTypesHolder_Type)jcasType).casFeatCode_il, jcasType.ll_cas.ll_getFSRef(v));}    
   
    
  //*--------------*
  //* Feature: fsNoRef

  /** getter for fsNoRef - gets 
   * @generated
   * @return value of the feature 
   */
  public FSArray getFsNoRef() {
    if (MultiValueTypesHolder_Type.featOkTst && ((MultiValueTypesHolder_Type)jcasType).casFeat_fsNoRef == null)
      jcasType.jcas.throwFeatMissing("fsNoRef", "de.julielab.jcore.types.test.MultiValueTypesHolder");
    return (FSArray)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((MultiValueTypesHolder_Type)jcasType).casFeatCode_fsNoRef)));}
    
  /** setter for fsNoRef - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setFsNoRef(FSArray v) {
    if (MultiValueTypesHolder_Type.featOkTst && ((MultiValueTypesHolder_Type)jcasType).casFeat_fsNoRef == null)
      jcasType.jcas.throwFeatMissing("fsNoRef", "de.julielab.jcore.types.test.MultiValueTypesHolder");
    jcasType.ll_cas.ll_setRefValue(addr, ((MultiValueTypesHolder_Type)jcasType).casFeatCode_fsNoRef, jcasType.ll_cas.ll_getFSRef(v));}    
    
  /** indexed getter for fsNoRef - gets an indexed value - 
   * @generated
   * @param i index in the array to get
   * @return value of the element at index i 
   */
  public org.apache.uima.jcas.tcas.Annotation getFsNoRef(int i) {
    if (MultiValueTypesHolder_Type.featOkTst && ((MultiValueTypesHolder_Type)jcasType).casFeat_fsNoRef == null)
      jcasType.jcas.throwFeatMissing("fsNoRef", "de.julielab.jcore.types.test.MultiValueTypesHolder");
    jcasType.jcas.checkArrayBounds(jcasType.ll_cas.ll_getRefValue(addr, ((MultiValueTypesHolder_Type)jcasType).casFeatCode_fsNoRef), i);
    return (org.apache.uima.jcas.tcas.Annotation)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefArrayValue(jcasType.ll_cas.ll_getRefValue(addr, ((MultiValueTypesHolder_Type)jcasType).casFeatCode_fsNoRef), i)));}

  /** indexed setter for fsNoRef - sets an indexed value - 
   * @generated
   * @param i index in the array to set
   * @param v value to set into the array 
   */
  public void setFsNoRef(int i, org.apache.uima.jcas.tcas.Annotation v) { 
    if (MultiValueTypesHolder_Type.featOkTst && ((MultiValueTypesHolder_Type)jcasType).casFeat_fsNoRef == null)
      jcasType.jcas.throwFeatMissing("fsNoRef", "de.julielab.jcore.types.test.MultiValueTypesHolder");
    jcasType.jcas.checkArrayBounds(jcasType.ll_cas.ll_getRefValue(addr, ((MultiValueTypesHolder_Type)jcasType).casFeatCode_fsNoRef), i);
    jcasType.ll_cas.ll_setRefArrayValue(jcasType.ll_cas.ll_getRefValue(addr, ((MultiValueTypesHolder_Type)jcasType).casFeatCode_fsNoRef), i, jcasType.ll_cas.ll_getFSRef(v));}
   
    
  //*--------------*
  //* Feature: fs

  /** getter for fs - gets 
   * @generated
   * @return value of the feature 
   */
  public FSArray getFs() {
    if (MultiValueTypesHolder_Type.featOkTst && ((MultiValueTypesHolder_Type)jcasType).casFeat_fs == null)
      jcasType.jcas.throwFeatMissing("fs", "de.julielab.jcore.types.test.MultiValueTypesHolder");
    return (FSArray)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((MultiValueTypesHolder_Type)jcasType).casFeatCode_fs)));}
    
  /** setter for fs - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setFs(FSArray v) {
    if (MultiValueTypesHolder_Type.featOkTst && ((MultiValueTypesHolder_Type)jcasType).casFeat_fs == null)
      jcasType.jcas.throwFeatMissing("fs", "de.julielab.jcore.types.test.MultiValueTypesHolder");
    jcasType.ll_cas.ll_setRefValue(addr, ((MultiValueTypesHolder_Type)jcasType).casFeatCode_fs, jcasType.ll_cas.ll_getFSRef(v));}    
    
  /** indexed getter for fs - gets an indexed value - 
   * @generated
   * @param i index in the array to get
   * @return value of the element at index i 
   */
  public org.apache.uima.jcas.tcas.Annotation getFs(int i) {
    if (MultiValueTypesHolder_Type.featOkTst && ((MultiValueTypesHolder_Type)jcasType).casFeat_fs == null)
      jcasType.jcas.throwFeatMissing("fs", "de.julielab.jcore.types.test.MultiValueTypesHolder");
    jcasType.jcas.checkArrayBounds(jcasType.ll_cas.ll_getRefValue(addr, ((MultiValueTypesHolder_Type)jcasType).casFeatCode_fs), i);
    return (org.apache.uima.jcas.tcas.Annotation)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefArrayValue(jcasType.ll_cas.ll_getRefValue(addr, ((MultiValueTypesHolder_Type)jcasType).casFeatCode_fs), i)));}

  /** indexed setter for fs - sets an indexed value - 
   * @generated
   * @param i index in the array to set
   * @param v value to set into the array 
   */
  public void setFs(int i, org.apache.uima.jcas.tcas.Annotation v) { 
    if (MultiValueTypesHolder_Type.featOkTst && ((MultiValueTypesHolder_Type)jcasType).casFeat_fs == null)
      jcasType.jcas.throwFeatMissing("fs", "de.julielab.jcore.types.test.MultiValueTypesHolder");
    jcasType.jcas.checkArrayBounds(jcasType.ll_cas.ll_getRefValue(addr, ((MultiValueTypesHolder_Type)jcasType).casFeatCode_fs), i);
    jcasType.ll_cas.ll_setRefArrayValue(jcasType.ll_cas.ll_getRefValue(addr, ((MultiValueTypesHolder_Type)jcasType).casFeatCode_fs), i, jcasType.ll_cas.ll_getFSRef(v));}
   
    
  //*--------------*
  //* Feature: fslistNoRef

  /** getter for fslistNoRef - gets 
   * @generated
   * @return value of the feature 
   */
  public FSList getFslistNoRef() {
    if (MultiValueTypesHolder_Type.featOkTst && ((MultiValueTypesHolder_Type)jcasType).casFeat_fslistNoRef == null)
      jcasType.jcas.throwFeatMissing("fslistNoRef", "de.julielab.jcore.types.test.MultiValueTypesHolder");
    return (FSList)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((MultiValueTypesHolder_Type)jcasType).casFeatCode_fslistNoRef)));}
    
  /** setter for fslistNoRef - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setFslistNoRef(FSList v) {
    if (MultiValueTypesHolder_Type.featOkTst && ((MultiValueTypesHolder_Type)jcasType).casFeat_fslistNoRef == null)
      jcasType.jcas.throwFeatMissing("fslistNoRef", "de.julielab.jcore.types.test.MultiValueTypesHolder");
    jcasType.ll_cas.ll_setRefValue(addr, ((MultiValueTypesHolder_Type)jcasType).casFeatCode_fslistNoRef, jcasType.ll_cas.ll_getFSRef(v));}    
   
    
  //*--------------*
  //* Feature: fslist

  /** getter for fslist - gets 
   * @generated
   * @return value of the feature 
   */
  public FSList getFslist() {
    if (MultiValueTypesHolder_Type.featOkTst && ((MultiValueTypesHolder_Type)jcasType).casFeat_fslist == null)
      jcasType.jcas.throwFeatMissing("fslist", "de.julielab.jcore.types.test.MultiValueTypesHolder");
    return (FSList)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((MultiValueTypesHolder_Type)jcasType).casFeatCode_fslist)));}
    
  /** setter for fslist - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setFslist(FSList v) {
    if (MultiValueTypesHolder_Type.featOkTst && ((MultiValueTypesHolder_Type)jcasType).casFeat_fslist == null)
      jcasType.jcas.throwFeatMissing("fslist", "de.julielab.jcore.types.test.MultiValueTypesHolder");
    jcasType.ll_cas.ll_setRefValue(addr, ((MultiValueTypesHolder_Type)jcasType).casFeatCode_fslist, jcasType.ll_cas.ll_getFSRef(v));}    
   
    
  //*--------------*
  //* Feature: sl

  /** getter for sl - gets 
   * @generated
   * @return value of the feature 
   */
  public StringList getSl() {
    if (MultiValueTypesHolder_Type.featOkTst && ((MultiValueTypesHolder_Type)jcasType).casFeat_sl == null)
      jcasType.jcas.throwFeatMissing("sl", "de.julielab.jcore.types.test.MultiValueTypesHolder");
    return (StringList)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((MultiValueTypesHolder_Type)jcasType).casFeatCode_sl)));}
    
  /** setter for sl - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setSl(StringList v) {
    if (MultiValueTypesHolder_Type.featOkTst && ((MultiValueTypesHolder_Type)jcasType).casFeat_sl == null)
      jcasType.jcas.throwFeatMissing("sl", "de.julielab.jcore.types.test.MultiValueTypesHolder");
    jcasType.ll_cas.ll_setRefValue(addr, ((MultiValueTypesHolder_Type)jcasType).casFeatCode_sl, jcasType.ll_cas.ll_getFSRef(v));}    
   
    
  //*--------------*
  //* Feature: slNoRef

  /** getter for slNoRef - gets 
   * @generated
   * @return value of the feature 
   */
  public StringList getSlNoRef() {
    if (MultiValueTypesHolder_Type.featOkTst && ((MultiValueTypesHolder_Type)jcasType).casFeat_slNoRef == null)
      jcasType.jcas.throwFeatMissing("slNoRef", "de.julielab.jcore.types.test.MultiValueTypesHolder");
    return (StringList)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((MultiValueTypesHolder_Type)jcasType).casFeatCode_slNoRef)));}
    
  /** setter for slNoRef - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setSlNoRef(StringList v) {
    if (MultiValueTypesHolder_Type.featOkTst && ((MultiValueTypesHolder_Type)jcasType).casFeat_slNoRef == null)
      jcasType.jcas.throwFeatMissing("slNoRef", "de.julielab.jcore.types.test.MultiValueTypesHolder");
    jcasType.ll_cas.ll_setRefValue(addr, ((MultiValueTypesHolder_Type)jcasType).casFeatCode_slNoRef, jcasType.ll_cas.ll_getFSRef(v));}    
  }

    