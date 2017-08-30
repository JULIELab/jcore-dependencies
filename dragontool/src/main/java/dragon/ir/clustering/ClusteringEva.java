package dragon.ir.clustering;

/**
 * <p>Class for evaluating clustering results </p>
 * <p> </p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class ClusteringEva {
    private double entropyScore;
    private double fScore;
    private double purityScore;
    private double nmiScore;
    private double geoNmiScore;
    private double miScore;

    private DocClusterSet human, machine;
    private double[][] matchMatrix;
    private double[][] recallMatrix;
    private double[][] precMatrix;
    private double docSum;
    private int clusterNum, classNum;

    public ClusteringEva() {
    }

    public boolean evaluate(DocClusterSet machine, DocClusterSet human){
        this.human=preprocessDocClusterSet(human);
        this.machine=preprocessDocClusterSet(machine);
        stat();
        matchMatrix=this.compClassMatch();
        compPrecRecall();
        compEntropy();
        compFScore();
        compPurityScore();
        compNMI();
        return true;
    }

    public double[][] getMatchMatrix(){
        return matchMatrix;
    }

    public double getMI(){
        return miScore;
    }

    public double getEntropy(){
        return entropyScore;
    }

    public double getFScore(){
        return fScore;
    }

    public double getPurity(){
        return purityScore;
    }

    public double getGeometryNMI(){
        return geoNmiScore;
    }

    public double getNMI(){
        return this.nmiScore;
    }

    private void stat(){
        int docSum, i;

        docSum = 0;
        for (i = 0; i < machine.getClusterNum(); i++) {
            docSum += machine.getDocCluster(i).getDocNum();
        }
        this.docSum = docSum;
        this.clusterNum=machine.getClusterNum();
        this.classNum =machine.getClusterNum();
    }

    private void compFScore(){
        int i,j;
        double max,sum;

        max=0;
        sum=0;
        for (i = 0; i< recallMatrix.length; i++) {
            max=0;
            for (j = 0; j < recallMatrix[0].length; j++) {
                if(max<compFScore(i,j))
                    max = compFScore(i, j);
            }
            sum+=(human.getDocCluster(i).getDocNum()/(double)docSum)*max;
        }
        this.fScore=sum;
    }

    private double compFScore(int i, int j){
        // cluster i, class j
        if (recallMatrix[i][j] == 0 && recallMatrix[i][j] == 0)
            return 0;
        return (2 * recallMatrix[i][j] * precMatrix[i][j]) / ( (precMatrix[i][j] + recallMatrix[i][j]));
    }

    private void compPurityScore(){
        int i;
        double sum;
        sum=0;
        for(i=0;i<machine.getClusterNum();i++){
            sum+=compPurityScore(machine.getDocCluster(i).getClusterID())*(machine.getDocCluster(i).getDocNum()/(double)docSum);
        }
        this.purityScore=sum;
    }

    private double compPurityScore(int clusterID){
        int i;
        double max;

        if(machine.getDocCluster(clusterID).getDocNum()==0)
            return 0;

        max = Double.MIN_VALUE;
        for (i = 0; i <classNum; i++) {
            if (max < matchMatrix[clusterID][i]) {
                max = matchMatrix[clusterID][i];
            }
        }
        return max/machine.getDocCluster(clusterID).getDocNum();
    }

    private void compEntropy(){
        int i;
        double sum;
        sum=0;

        for(i=0;i<clusterNum;i++){
            sum+=machine.getDocCluster(i).getDocNum()*compEntropy(i)/docSum;
        }
        this.entropyScore=sum;
    }

    private double compEntropy(int clusterID){
        double sum;
        int j;

        sum=0;
        for(j=0;j<clusterNum;j++){
            if(precMatrix[clusterID][j]==0)
                sum+=-Math.log(1.0/docSum)*(1.0/docSum);
            else
                sum+=-Math.log(precMatrix[clusterID][j])*precMatrix[clusterID][j];
        }
        return sum;
    }

    private double[][] compClassMatch(){
        DocCluster dc1,dc2;
        double[][] match;
        int i,j,k;

        match = new double[clusterNum][classNum];
        for(i=0;i<clusterNum;i++){
            dc1=machine.getDocCluster(i);
            for(j=0;j<classNum;j++){
                dc2=human.getDocCluster(j);
                for(k=0;k<dc2.getDocNum();k++)
                    if (dc1.containDoc(dc2.getDoc(k)))
                        match[i][j]++;
            }
        }
        return match;
    }

    private void compPrecRecall(){
        DocCluster dc1,dc2;

        precMatrix=new double[matchMatrix.length][matchMatrix[0].length];
        recallMatrix=new double[matchMatrix.length][matchMatrix[0].length];
        for(int i=0;i<clusterNum;i++){
            dc1=machine.getDocCluster(i);
            for(int j=0;j<classNum;j++){
                dc2=human.getDocCluster(j);
                precMatrix[i][j]=matchMatrix[i][j]/dc1.getDocNum();
                recallMatrix[i][j]=matchMatrix[i][j]/dc2.getDocNum();
            }
        }
    }

    private void compNMI(){
        int i, j;
        double sumJoint, sumX, sumY, xProb, yProb, jointProb;

        sumJoint = 0;
        sumX = 0;
        sumY = 0;

        for (i = 0; i < clusterNum; i++) {
            for (j = 0; j <classNum; j++) {
                if (matchMatrix[i][j] == 0)
                    jointProb = 1.0 / docSum;
                else
                    jointProb = matchMatrix[i][j] / docSum;
                xProb = machine.getDocCluster(i).getDocNum() / docSum;
                yProb = human.getDocCluster(j).getDocNum() / docSum;
                sumJoint += jointProb * Math.log(jointProb / (xProb * yProb));
            }
        }

        for (i = 0; i < clusterNum; i++) {
            xProb = machine.getDocCluster(i).getDocNum() / docSum;
            sumX += xProb * Math.log(1 / xProb);
        }

        for (i = 0; i < clusterNum; i++) {
            yProb = human.getDocCluster(i).getDocNum() / docSum;
            sumY += yProb * Math.log(1 / yProb);
        }

        miScore = sumJoint;
        nmiScore = 2.0 * sumJoint / (Math.log(clusterNum) + Math.log(classNum));
        geoNmiScore = sumJoint / Math.sqrt(sumX * sumY);
    }

    private DocClusterSet preprocessDocClusterSet(DocClusterSet oldSet){
        DocClusterSet newSet;
        int i,count;

        count=0;
        for(i=0;i<oldSet.getClusterNum();i++)
            if(oldSet.getDocCluster(i).getDocNum()>0)
                count++;

        newSet=new DocClusterSet(count);
        count=0;
        for(i=0;i<oldSet.getClusterNum();i++){
            if (oldSet.getDocCluster(i).getDocNum() > 0) {
                newSet.setDocCluster(oldSet.getDocCluster(i), count);
                count++;
            }
        }
        return newSet;
    }
}