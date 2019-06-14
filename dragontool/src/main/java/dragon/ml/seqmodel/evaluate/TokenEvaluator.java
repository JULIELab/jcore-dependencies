package dragon.ml.seqmodel.evaluate;

import dragon.ml.seqmodel.data.DataSequence;
import dragon.ml.seqmodel.data.Dataset;

/**
 * <p>Token based evaluator</p>
 * <p> </p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class TokenEvaluator extends AbstractEvaluator {
    private int labelNum;
    private int truePos[];
    private int totalMarkedPos[];
    private int totalPos[];
    private int confuseMatrix[][];

    public TokenEvaluator(int labelNum) {
        this.labelNum = labelNum;
        truePos = new int[labelNum];
        totalMarkedPos = new int[labelNum];
        totalPos = new int[labelNum];
        confuseMatrix = new int[labelNum][labelNum];
    }

    public void evaluate(Dataset human, Dataset machine) {
        DataSequence humanSeq, machineSeq;
        double prec, recall;
        int len, i, machineLabel, humanLabel;

        if (machine.size() != human.size()) {
            System.out.println("Length Mismatch - Auto: " + machine.size() + " Man: " + human.size());
            return;
        }

        human.startScan();
        machine.startScan();
        while(human.hasNext() && machine.hasNext()){
            humanSeq = human.next();
            machineSeq = machine.next();
            if (humanSeq.length() != machineSeq.length()) {
                System.out.println("Length Mismatch - Manual: " + humanSeq.length() + " Auto: " + machineSeq.length());
                continue;
            }
            len = humanSeq.length();
            for (i = 0; i < len; i++) {
                machineLabel=machineSeq.getOriginalLabel(i);
                totalLabels++;
                if(machineLabel<0) continue;

                humanLabel=humanSeq.getOriginalLabel(i);
                totalMarkedPos[machineLabel]++;
                annotatedLabels++;
                totalPos[humanLabel]++;
                confuseMatrix[humanLabel][machineLabel]++;
                if (humanLabel == machineLabel) {
                    correctAnnotatedLabels++;
                    truePos[humanLabel]++;
                }
            }
        }

        System.out.println("\n\nCalculations:");
        System.out.println();
        System.out.println("Label\tTrue+\tMarked+\tActual+\tPrec.\tRecall\tF1");

        for (i = 0; i <labelNum; i++) {
            prec = (totalMarkedPos[i] == 0) ? 0 : ( (double) (truePos[i] * 100000 / totalMarkedPos[i])) / 1000;
            recall = (totalPos[i] == 0) ? 0 : ( (double) (truePos[i] * 100000 / totalPos[i])) / 1000;
            System.out.println( (i) + ":\t" + truePos[i] + "\t" + totalMarkedPos[i] + "\t" + totalPos[i] + "\t" + prec +
                               "\t" + recall + "\t" + 2 * prec * recall / (prec + recall));
        }

        //print out overall performance
        System.out.println("---------------------------------------------------------");
        System.out.println("Ov:\t" + correctAnnotatedLabels + "\t" + annotatedLabels + "\t" + totalLabels +
                           "\t" + precision() + "\t" + recall() + "\t" + 2 * precision() * recall() / (precision() + recall()));
    }

}