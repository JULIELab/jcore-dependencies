package dragon.ir.index;

import dragon.matrix.*;
import dragon.nlp.*;
import dragon.util.FileUtil;
import java.io.*;
import java.util.ArrayList;

/**
 * <p>Import (export) indexing from (to) text format</p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class IndexConverter {
    public IndexConverter() {
    }

    public void importIndex(String indexFolder, String doctermFile){
        BasicIndexWriteController controller;
        BufferedReader br;
        ArrayList conceptList;
        Token token;
        String line, docKey, arrField[];
        int i,sectionID, featureNum;

        try{
            sectionID=0;
            conceptList=new ArrayList(500);
            controller = new BasicIndexWriteController(indexFolder, false, false);
            controller.addSection(new IRSection(sectionID, "all"));
            controller.initialize();
            br = FileUtil.getTextReader(doctermFile);
            while ( (line = br.readLine()) != null) {
                arrField=line.split("\t");
                docKey=arrField[0];
                featureNum=Integer.parseInt(arrField[1]);
                if(!controller.setDoc(docKey))
                    continue;

                conceptList.clear();
                for(i=0;i<featureNum;i++){
                    token=new Token(arrField[2+i*2]);
                    token.setFrequency(Integer.parseInt(arrField[3+i*2]));
                    conceptList.add(token);
                }
                controller.write(sectionID, conceptList);
            }
            controller.close();
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    public void importDocLinkage(String indexFolder, String doclinkFile,boolean outputTransposedMatrix){
        DoubleSuperSparseMatrix matrix, matrixT;
        SimpleElementList docList;
        BufferedReader br;
        String line, arrField[];
        double weight;
        int i, src, dest, num;

        try{
            docList=new SimpleElementList(indexFolder+"/dockey.list",false);
            matrix = new DoubleSuperSparseMatrix(indexFolder + "/doclinkage.index",indexFolder + "/doclinkage.matrix", false, false);
            if (outputTransposedMatrix)
                matrixT = new DoubleSuperSparseMatrix(indexFolder + "/doclinkaget.index",
                    indexFolder + "/doclinkaget.matrix", false, false);
            else
                matrixT=null;
            br=FileUtil.getTextReader(doclinkFile);
            while((line=br.readLine())!=null){
                arrField=line.split("\t");
                src=docList.search(arrField[0]);
                num=Integer.parseInt(arrField[1]);
                if(src<0 || num==0)
                    continue;
                for(i=0;i<num;i++){
                    dest = docList.search(arrField[2+2*i]);
                    if (dest < 0)
                        continue;
                    weight = Double.parseDouble(arrField[3+2*i]);
                    matrix.add(src, dest, weight);
                    if (matrixT != null)
                        matrixT.add(dest, src, weight);
                }
            }
            docList.close();
            matrix.finalizeData(true);
            matrix.close();
            if(matrixT!=null){
                matrixT.finalizeData(true);
                matrixT.close();
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    public void exportIndex(String indexFolder, String contentFile){
        exportIndex(indexFolder,"all",contentFile);
    }

    public void exportIndex(String indexFolder, String section, String contentFile){
        SimpleElementList docList, termList;
        IntGiantSparseMatrix matrix;

        docList=new SimpleElementList(indexFolder+"/dockey.list",false);
        termList=new SimpleElementList(indexFolder+"/termkey.list",false);
        matrix = new IntGiantSparseMatrix(indexFolder + "/all/docterm.index",indexFolder + "/all/docterm.matrix");
        exportMatrix(docList,docList,matrix,contentFile);
        docList.close();
        termList.close();
        matrix.close();
    }

    public void exportDocLinkage(String indexFolder,String docRelationFile){
        SimpleElementList docList;
        DoubleGiantSparseMatrix matrix;

        docList=new SimpleElementList(indexFolder+"/dockey.list",false);
        matrix = new DoubleGiantSparseMatrix(indexFolder + "/doclinkage.index",indexFolder + "/doclinkage.matrix");
        exportMatrix(docList,docList,matrix,docRelationFile);
        docList.close();
        matrix.close();
    }

    public void exportMatrix(SimpleElementList rowList, SimpleElementList colList, DoubleSparseMatrix matrix, String outputFile){
        exportMatrix(rowList,colList,matrix,false,outputFile);
    }

    public void exportMatrix(SimpleElementList rowList, SimpleElementList colList, IntSparseMatrix matrix, String outputFile){
        exportMatrix(rowList,colList,matrix,true,outputFile);
    }

    public void exportMatrix(SimpleElementList rowList, SimpleElementList colList, SparseMatrix matrix, boolean exportAsInteger,
                              String outputFile){
        PrintWriter out;
        double arrDblWeight[];
        int i, j, termNum, arrIndex[],arrIntWeight[];

        out=FileUtil.getPrintWriter(outputFile);
        arrIntWeight=null;
        arrDblWeight=null;

        for(i=0;i<matrix.rows();i++){
            out.print(rowList.search(i));
            out.print('\t');
            arrIndex=matrix.getNonZeroColumnsInRow(i);
            if(exportAsInteger)
                arrIntWeight=matrix.getNonZeroIntScoresInRow(i);
            else
                arrDblWeight=matrix.getNonZeroDoubleScoresInRow(i);
            if(arrIndex==null)
                termNum=0;
            else
                termNum=arrIndex.length;
            out.print(termNum);
            for(j=0;j<termNum;j++){
                out.print('\t');
                out.print(colList.search(arrIndex[j]));
                out.print('\t');
                if(exportAsInteger)
                    out.print(arrIntWeight[j]);
                else
                    out.print(arrDblWeight[j]);
            }
            out.print('\n');
            out.flush();
        }
        out.close();
    }
}