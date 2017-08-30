//package dragon.ir.topicmodel;
//
//import dragon.nlp.*;
//import dragon.nlp.compare.*;
//import java.io.*;
//import java.util.*;
//import jxl.*;
//import jxl.write.*;
//import jxl.write.Number;
//
///**
// * <p>Writing topic model results to excel sheets </p>
// * <p> </p>
// * <p>Copyright: Copyright (c) 2005</p>
// * <p>Company: IST, Drexel University</p>
// * @author Davis Zhou
// * @version 1.0
// */
//
//public class ModelExcelWriter {
//    public ModelExcelWriter() {
//    }
//
//    public void write(TopicModel model, int top, String outputFile){
//        write(model,null,top, outputFile);
//    }
//
//    public void write(TopicModel model, String[] termNameList, int top, String outputFile) {
//        ArrayList termList;
//        WritableSheet sheet;
//        WritableWorkbook workbook;
//        WritableFont arial14font;
//        WritableCellFormat arial14format;
//        int topicNum;
//        int i;
//
//        try {
//            topicNum=model.getTopicNum();
//            arial14font = new WritableFont(WritableFont.ARIAL, 14);
//            arial14format = new WritableCellFormat(arial14font);
//            workbook = Workbook.createWorkbook(new File(outputFile));
//            sheet = workbook.createSheet("Topic Model", 0);
//            if(termNameList==null){
//                termNameList = new String[model.getTermNum()];
//                for (i = 0; i < model.getTermNum(); i++)
//                    termNameList[i] = model.getTermName(i);
//            }
//
//            for (i = 0; i < topicNum; i++) {
//                sheet.addCell(new Label(i*2, 0, "Topic" + (i + 1), arial14format));
//                termList = sortThemeTerm(model.getTopic(i), termNameList, top);
//                this.saveTermList(sheet, i*2, 1, termList);
//            }
//            workbook.write();
//            workbook.close();
//        }
//        catch (Exception ex) {
//            ex.printStackTrace();
//        }
//    }
//
//    public void write(TwoDimensionModel model, int top, String outputFile){
//        write(model,null,null,top,outputFile);
//    }
//
//    public void write(TwoDimensionModel model, String[] viewTermList, String[] topicTermList, int top, String outputFile) {
//        ArrayList termList;
//        WritableSheet sheet;
//        WritableWorkbook workbook;
//        WritableFont arial14font;
//        WritableCellFormat arial14format;
//
//        int viewNum, topicNum;
//        int i, j;
//
//        try {
//            viewNum=model.getViewNum();
//            topicNum=model.getTopicNum();
//            arial14font = new WritableFont(WritableFont.ARIAL, 14);
//            arial14format = new WritableCellFormat(arial14font);
//            workbook = Workbook.createWorkbook(new File(outputFile));
//            sheet = workbook.createSheet("View-Topic Model", 0);
//            sheet.addCell(new Label(0, 0, "View-Topic", arial14format));
//            sheet.addCell(new Label(1, 0, "View Content", arial14format));
//            if(viewTermList==null){
//                viewTermList = new String[model.getViewTermNum()];
//                for (i = 0; i < viewTermList.length; i++)
//                    viewTermList[i] = model.getViewTermName(i);
//            }
//            if(topicTermList==null){
//                topicTermList = new String[model.getTopicTermNum()];
//                for (i = 0; i < topicTermList.length; i++)
//                    topicTermList[i] = model.getTopicTermName(i);
//            }
//
//            for (i = 0; i < viewNum; i++) {
//                sheet.addCell(new Label(0, (i + 1) * top + 1, "View " + (i + 1), arial14format));
//                termList = sortThemeTerm(model.getView(i), viewTermList, top);
//                this.saveTermList(sheet, 1, (i + 1) * top + 1, termList);
//                for (j = 0; j <topicNum; j++) {
//                    termList = sortThemeTerm(model.getViewTopic(i,j) , topicTermList, top);
//                    this.saveTermList(sheet, 2 + j * 2+1, (i + 1) * top + 1, termList);
//                }
//            }
//
//            for (i = 0; i < topicNum; i++) {
//                sheet.addCell(new Label(3 + i * 2, 0, "Topic " + (i + 1), arial14format));
//                termList = sortThemeTerm(model.getCommonTopic(i) , topicTermList,top);
//                this.saveTermList(sheet, 3 + i * 2, 1, termList);
//            }
//            workbook.write();
//            workbook.close();
//        }
//        catch (Exception ex) {
//            ex.printStackTrace();
//        }
//    }
//
//    private void saveTermList(WritableSheet sheet,int colNum,int rowNum,ArrayList list){
//        Token token;
//        int i;
//
//        try{
//            for (i = 0; i < list.size(); i+=1) {
//                token =(Token)list.get(i);
//                sheet.addCell(new Label(colNum,rowNum+i,token.getName()));
//                sheet.addCell(new Number(colNum+1, rowNum+i, token.getWeight()));
//            }
//        }
//        catch(Exception ex){
//            ex.printStackTrace();
//        }
//    }
//
//    private ArrayList sortThemeTerm(double[] termProbs, String[] termKeyList, int top){
//        Token token;
//        ArrayList termList, newList;
//        String termKey;
//        int i;
//
//        newList =new ArrayList();
//        termList = new ArrayList();
//        for (i = 0; i < termProbs.length; i++) {
//            termKey = termKeyList[i];
//            token = new Token(termKey);
//            token.setIndex(i);
//            token.setWeight(termProbs[i]);
//            termList.add(token);
//        }
//
//        Collections.sort(termList,new WeightComparator());
//        if(top>termList.size())
//            top=termList.size();
//
//        for(i=0;i<top;i++)
//            newList.add(termList.get(i));
//        return newList;
//    }
//}