package dragon.matrix;

import dragon.util.*;
import java.io.*;

/**
 * <p>Double flat dense matrix which provides options of storing data to disk in binary manner or text manner </p>
 * <p></p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class DoubleFlatDenseMatrix extends AbstractDenseMatrix implements DoubleDenseMatrix, java.io.Serializable{
	private static final long serialVersionUID = 1L;
	private double[][] arrMatrix;

    public DoubleFlatDenseMatrix(double[][] newMatrix) {
        super(newMatrix.length,newMatrix[0].length,8);
        arrMatrix=newMatrix;
    }

    public DoubleFlatDenseMatrix(int row, int column) {
        super(row,column,8);
        arrMatrix = new double[row][column];
    }

    public DoubleFlatDenseMatrix(String filename) {
        super(-1,-1,8);
        this.readTextMatrixFile(filename);
    }

    public DoubleFlatDenseMatrix(String filename, boolean binaryFile) {
        super(-1,-1,8);
        if(binaryFile)
            this.readBinMatrixFile(filename);
        else
            this.readTextMatrixFile(filename);
    }

    public void assign(double val){
        int i, j;

        for(i=0;i<rows;i++)
            for(j=0;j<columns;j++)
                arrMatrix[i][j]=val;
    }

    public boolean add(int row, int column, double score){
        if (row > rows || column > columns)
           return false;
       else {
           arrMatrix[row][column]+= score;
           return true;
       }
    }

    public boolean setDouble(int row, int column, double score){
        if (row > rows || column > columns)
           return false;
       else {
           arrMatrix[row][column] = score;
           return true;
       }
    }

    public double getRowSum(int row){
        int i;
        double sum;

        sum=0;
        for(i=0;i<columns;i++){
            sum+=arrMatrix[row][i];
        }
        return sum;
    }

    public double getColumnSum(int column){
        int i;
        double sum;

        sum=0;
        for(i=0;i<rows;i++){
            sum+=arrMatrix[i][column];
        }
        return sum;
    }

    public int getInt(int row, int column){
        return (int)arrMatrix[row][column];
    }

    public double getDouble(int row, int column){
        return arrMatrix[row][column];
    }

    public void saveTo(String filename, boolean binary){
        int i,j;
        FastBinaryWriter fastBinWriter;

        if(!binary) {
            this.saveTo(filename);
            return;
        }
        fastBinWriter = new FastBinaryWriter(filename);
        try{
            fastBinWriter.writeInt(rows);
            fastBinWriter.writeInt(columns);
            fastBinWriter.writeInt(rows*columns);

            for(i=0;i<rows;i++){
                for(j=0;j<columns;j++){
                    fastBinWriter.writeDouble(arrMatrix[i][j]);
                }
                if(i%100==0)
                    fastBinWriter.flush();
            }
            fastBinWriter.flush();
            fastBinWriter.close();
        }catch(Exception ex){
            ex.printStackTrace();
        }
    }

    public void saveTo(String filename)
    {
        print(FileUtil.getPrintWriter(filename));
    }

    public void print(PrintWriter out) {
        int i, j;
        try {

            //output rows, columns and edges.
            out.write(String.valueOf(rows) + "," + String.valueOf(columns) + "," + String.valueOf(rows * columns) +
                      "\n");
            //output all rows
            for (i = 0; i < rows; i++) {
                for (j = 0; j < columns; j++)
                    out.write(String.valueOf(arrMatrix[i][j]) + ",");
                out.write("\n");
                if (i % 100 == 0)
                    out.flush();
            }
            out.flush();
            out.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void readTextMatrixFile(String fileName) {
        int i, j;
        String line;
        String[] arrLine;
        BufferedReader br;
        br = FileUtil.getTextReader(fileName);
        try {
            line = br.readLine();
            arrLine = line.split(",");

            rows = Integer.parseInt(arrLine[0]);
            columns = Integer.parseInt(arrLine[1]);
            arrMatrix = new double[rows][columns];

            i = 0;
            while ( (line = br.readLine()) != null) {
                arrLine = line.split(",");
                for (j = 0; j < arrMatrix[0].length; j++)
                    arrMatrix[i][j] = Double.parseDouble(arrLine[j]);
                i++;
            }

        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void readBinMatrixFile(String fileName) {
        int i, j;
        FastBinaryReader fbr;

        fbr = new FastBinaryReader(fileName);
        try {
            rows = fbr.readInt();
            columns = fbr.readInt();
            arrMatrix = new double[rows][columns];

            fbr.readInt();
            for (i = 0; i < rows; i++)
                for (j = 0; j < columns; j++)
                    arrMatrix[i][j] = fbr.readDouble();
            fbr.close();
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void close() {
        rows = 0;
        columns = 0;
        arrMatrix = null;
    }
}