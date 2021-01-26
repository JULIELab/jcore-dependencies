package dragon.ir.topicmodel;

import dragon.ir.index.IndexReader;

import java.util.Random;

/**
 * <p>LDA Gibbs sampling two dimensional topical model</p>
 * <p> </p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: IST, Drexel University</p>
 * @author Davis Zhou
 * @version 1.0
 */

public class TwoDimensionGibbsLDA extends AbstractTwoDimensionModel{
    private double alpha; //the prior for view distribution in each document;
    private double beta, wBeta; //the prior for word distribution in each view
    private double gamma0, gamma1; //gamma0:view-free, gamma1: view-specific
    private double delta, wDelta; //the prior for word disbribution in each view-free theme
    private double epsilon, wEpsilon; // the prior for word distribution in each view-specific theme
    private double rho; // the prior for theme distribution in each document;
    private int tokenNumO, tokenNumP;

    public TwoDimensionGibbsLDA(IndexReader viewIndexReader, IndexReader topicIndexReader,
                            double alpha, double beta, double gamma0,double gamma1, double delta, double epsilon, double rho) {
        super(viewIndexReader,topicIndexReader);
        this.seed=-1;
        this.iterations=1000;
        this.alpha =alpha;
        this.beta=beta;
        this.gamma0 =gamma0;
        this.delta=delta;
        this.epsilon=epsilon;
        this.rho=rho;
        tokenNumO=(int)viewIndexReader.getCollection().getTermCount();
        tokenNumP=(int)viewIndexReader.getCollection().getTermCount();
        wBeta=beta*(viewTermNum+themeTermNum);
        wDelta=delta*themeTermNum;
        wEpsilon=epsilon*themeTermNum;
    }

    public boolean estimateModel(int viewNum, int topicNum){
        int[] arrTermO, arrDocO, arrTermP, arrDocP;
        int[] arrZO, arrZP, arrY, arrX;
        int[][] arrTVCountO, arrTVCountP, arrDVCount, arrTTCount;
        int[][][] arrTTViewCount, arrTTComCount, arrDTCount;
        int i, j, k;
        double sum;

        this.viewNum =viewNum;
        this.themeNum =topicNum;
        arrTermO=new int[tokenNumO];
        arrDocO=new int[tokenNumO];
        arrTermP=new int[tokenNumP];
        arrDocP=new int[tokenNumP];
        arrZO=new int[tokenNumO];
        arrZP=new int[tokenNumP];
        arrY=new int[tokenNumP];
        arrX=new int[tokenNumP];

        arrTVCountO=new int[viewTermNum][viewNum];
        arrTVCountP=new int[themeTermNum][viewNum];
        arrDVCount=new int[docNum][viewNum];
        arrTTCount=new int[themeTermNum][themeNum];
        arrTTViewCount= new int[themeTermNum][viewNum][themeNum];
        arrTTComCount=new int[themeTermNum][viewNum][themeNum];
        arrDTCount=new int[docNum][viewNum][themeNum];


        arrViewProb=new double[viewNum][viewTermNum];
        arrDocView = new double[docNum][viewNum];
        arrThemeProb = new double[viewNum][themeNum][themeTermNum];
        arrDocTheme = new double[docNum][viewNum][themeNum];
        arrCommonThemeProb = new double[themeNum][themeTermNum];

        //read sequence from indexing files
        readSequence(viewIndexReader,arrTermO,arrDocO);
        readSequence(topicIndexReader,arrTermP,arrDocP);

        //run one makov chain
        run(seed,arrTermO, arrDocO, arrZO, arrTVCountO, arrTermP,arrDocP,arrZP, arrTVCountP, arrDVCount,
            arrY, arrX, arrTTViewCount, arrTTComCount, arrTTCount, arrDTCount);

        //estimate word distrubtion in each view
        for(i=0;i<viewNum;i++){
            sum=beta*viewTermNum;
            for(j=0;j<viewTermNum;j++)
                sum+=arrTVCountO[j][i];
            for(j=0;j<viewTermNum;j++)
                arrViewProb[i][j]=(arrTVCountO[j][i]+beta)/sum;
        }

        //estimate view distribution in each document
        for(i=0;i<docNum;i++){
            sum=viewNum*alpha;
            for(j=0;j<viewNum;j++)
                sum+=arrDVCount[i][j];
            for(j=0;j<viewNum;j++)
                arrDocView[i][j]=(arrDVCount[i][j]+alpha)/sum;
        }

        //estimate word distribution in each common topic
        for(i=0;i<themeNum;i++){
            sum=delta*themeTermNum;
            for(j=0;j<themeTermNum;j++)
                sum+=arrTTCount[j][i];
            for(j=0;j<themeTermNum;j++)
                arrCommonThemeProb[i][j]=(arrTTCount[j][i]+delta)/sum;
        }

        //estimate word distribution in each view-specific topic
        for(k=0;k<viewNum;k++){
            for (i = 0; i < themeNum; i++) {
                sum = epsilon * themeTermNum;
                for (j = 0; j < themeTermNum; j++)
                    sum += arrTTViewCount[j][k][i];
                for (j = 0; j < themeTermNum; j++)
                    arrThemeProb[k][i][j] = (arrTTViewCount[j][k][i] + epsilon) / sum;
            }
        }

        //estimate theme distribution in each document
        for(k=0;k<docNum;k++){
            for (i = 0; i < viewNum; i++) {
                sum = rho * themeNum;
                for (j = 0; j < themeNum; j++)
                    sum += arrDTCount[k][i][j];
                for (j = 0; j < themeNum; j++)
                    arrDocTheme[k][i][j] = (arrDTCount[k][i][j] + rho) / sum;
            }
        }

        return true;
    }

    private void run(int seed, int[] arrTermO, int[] arrDocO, int[] arrZO, int[][] arrTVCountO,
                     int[] arrTermP, int[] arrDocP, int[] arrZP, int[][] arrTVCountP, int[][] arrDVCount,
                     int[] arrY, int[] arrX, int[][][] arrTTViewCount, int[][][] arrTTComCount, int[][] arrTTCount, int[][][] arrDTCount)
    {
        Random random;
        int[] arrViewCount,arrTopicCount, arrOrderO, arrOrderP;
        int[][] arrViewTopicCount;
        int termIndex, docIndex, view, topic,status;
        int i, j, k,iter;

        //initiliazation
        arrViewCount=new int[viewNum];
        arrTopicCount=new int[themeNum];
        arrViewTopicCount=new int[viewNum][themeNum];
        arrOrderO=new int[tokenNumO];
        arrOrderP=new int[tokenNumP];
        random=new Random();
        if(seed>=0)
            random.setSeed(seed);

        printStatus(new java.util.Date().toString()+" Starting random initialization...");
        for(i=0;i<tokenNumO;i++){
            view=random.nextInt(viewNum);
            arrZO[i]=view;
            termIndex=arrTermO[i];
            docIndex=arrDocO[i];
            arrTVCountO[termIndex][view]++;
            arrDVCount[docIndex][view]++;
            arrViewCount[view]++;
        }
        for(i=0;i<tokenNumP;i++){
            view=random.nextInt(viewNum);
            arrZP[i]=view;
            termIndex=arrTermP[i];
            docIndex=arrDocP[i];
            arrTVCountP[termIndex][view]++;
            arrDVCount[docIndex][view]++;
            arrViewCount[view]++;
        }

        for(i=0;i<tokenNumP;i++){
            topic=random.nextInt(themeNum);
            view=arrZP[i];
            arrY[i]=topic;
            termIndex=arrTermP[i];
            docIndex=arrDocP[i];
            arrTTViewCount[termIndex][view][topic]++;
            arrDTCount[docIndex][view][topic]++;
            arrViewTopicCount[view][topic]++;
            arrX[i]=1; //initially all property words are view-spcific
        }

        //Determine random update sequence
        printStatus(new java.util.Date().toString()+" Determining random update sequence...");
        for (i=0; i<tokenNumO; i++) arrOrderO[i]=i;
        for (i = 0; i < (tokenNumO - 1); i++) {
            // pick a random integer between i and n
            k = i +random.nextInt(tokenNumO-i);
            // switch contents on position i and position k
            j = arrOrderO[k];
            arrOrderO[k] = arrOrderO[i];
            arrOrderO[i] = j;
        }

        for (i=0; i<tokenNumP; i++) arrOrderP[i]=i;
        for (i = 0; i < (tokenNumP - 1); i++) {
            // pick a random integer between i and n
            k = i +random.nextInt(tokenNumP-i);
            // switch contents on position i and position k
            j = arrOrderP[k];
            arrOrderP[k] = arrOrderP[i];
            arrOrderP[i] = j;
        }

        for (iter = 0; iter <iterations; iter++) {
            printStatus(new java.util.Date().toString()+" Iteration #"+(iter+1));

            //sampling words in the major dimension
            for (k = 0; k <tokenNumO; k++) {
                i = arrOrderO[k]; // current word token to assess
                termIndex = arrTermO[i];
                docIndex =arrDocO[i];
                view = arrZO[i];

                // substract the current instance from counts
                arrViewCount[view]--;
                arrTVCountO[termIndex][view]--;
                arrDVCount[docIndex][view]--;

                // sample a view from the distribution for the current instance
                view=sampleView(random,arrTVCountO[termIndex],arrDVCount[docIndex],arrViewCount);

                //update counts
                arrZO[i] = view;
                arrTVCountO[termIndex][view]++;
                arrDVCount[docIndex][view]++;
                arrViewCount[view]++;
            }

            //sampling words in the secondary dimension
            for (k = 0; k <tokenNumP; k++) {
                i = arrOrderP[k]; // current word token to assess
                termIndex = arrTermP[i];
                docIndex =arrDocP[i];
                topic = arrY[i];
                view = arrZP[i];
                status=arrX[i];

                //sample a view
                // substract the current instance from counts
                arrViewCount[view]--;
                arrTVCountP[termIndex][view]--;
                arrDVCount[docIndex][view]--;
                if(status==0){
                    arrTTComCount[termIndex][view][topic]--;
                }
                else{
                    arrTTViewCount[termIndex][view][topic]--;
                    arrViewTopicCount[view][topic]--;
                }
                arrDTCount[docIndex][view][topic]--;

                // sample a view from the distribution for the current instance
                view=sampleView(random,arrTVCountP[termIndex],arrDVCount[docIndex],arrViewCount);

                //update counts
                arrZP[i] = view;
                arrTVCountP[termIndex][view]++;
                arrDVCount[docIndex][view]++;
                arrViewCount[view]++;
                if(status==0)
                    arrTTComCount[termIndex][view][topic]++;
                else{
                    arrTTViewCount[termIndex][view][topic]++;
                    arrViewTopicCount[view][topic]++;
                }
                arrDTCount[docIndex][view][topic]++;


                //sample a theme(topic)
                if(status==0){
                    //the theme is view-free
                    // substract the current instance from counts
                    arrTopicCount[topic]--;
                    arrTTCount[termIndex][topic]--;
                    arrTTComCount[termIndex][view][topic]--;
                    arrDTCount[docIndex][view][topic]--;

                    // sample a topic from the distribution for the current instance
                    topic = sampleCommonTopic(random, arrTTCount[termIndex], arrDTCount[docIndex][view], arrTopicCount);

                    //update counts
                    arrY[i] = topic;
                    arrTTComCount[termIndex][view][topic]++;
                    arrTTCount[termIndex][topic]++;
                    arrDTCount[docIndex][view][topic]++;
                    arrTopicCount[topic]++;
                }
                else{
                    //the theme is view-specific
                    // substract the current instance from counts
                    arrViewTopicCount[view][topic]--;
                    arrTTViewCount[termIndex][view][topic]--;
                    arrDTCount[docIndex][view][topic]--;

                    // sample a topic from the distribution for the current instance
                    topic = sampleViewTopic(random, arrTTViewCount[termIndex][view], arrDTCount[docIndex][view], arrViewTopicCount[view]);

                    //update counts
                    arrY[i] = topic;
                    arrTTViewCount[termIndex][view][topic]++;
                    arrDTCount[docIndex][view][topic]++;
                    arrViewTopicCount[view][topic]++;
                }

                //sample a status
                // substract the current instance from counts
                if(status==0){
                    arrTopicCount[topic]--;
                    arrTTCount[termIndex][topic]--;
                    arrTTComCount[termIndex][view][topic]--;
                    arrDTCount[docIndex][view][topic]--;
                }
                else{
                    arrViewTopicCount[view][topic]--;
                    arrTTViewCount[termIndex][view][topic]--;
                    arrDTCount[docIndex][view][topic]--;
                }

                // sample a status from the distribution for the current instance
                status = sampleStatus(random, arrTTViewCount[termIndex][view][topic], arrViewTopicCount[view][topic],
                                      arrTTComCount[termIndex][view][topic], arrTTCount[termIndex][topic], arrTopicCount[topic]);

                //update counts
                arrX[i] = status;
                if(status==0){
                    arrTTComCount[termIndex][view][topic]++;
                    arrTTCount[termIndex][topic]++;
                    arrDTCount[docIndex][view][topic]++;
                    arrTopicCount[topic]++;
                }
                else{
                    arrTTViewCount[termIndex][view][topic]++;
                    arrDTCount[docIndex][view][topic]++;
                    arrViewTopicCount[view][topic]++;
                }
            }
        }
    }

    private int sampleView(Random random, int[] arrTVCount, int[] arrDVCount, int[] arrViewCount){
        double[] arrProb;
        double totalProb, r, max;
        int j,view;

        totalProb = 0;
        arrProb=new double[viewNum];
        for (j = 0; j < viewNum; j++) {
            arrProb[j] = (arrTVCount[j] + beta)/(arrViewCount[j] + wBeta) *(arrDVCount[j]+ alpha);
            totalProb += arrProb[j];
        }
        r = totalProb * random.nextDouble();
        max = arrProb[0];
        view = 0;
        while (r > max) {
            view++;
            max += arrProb[view];
        }
        return view;
    }

    private int sampleCommonTopic(Random random, int[] arrTTCount, int[] arrDTCount, int[] arrTopicCount){
        double[] arrProb;
        double totalProb, r, max;
        int j,topic;

        totalProb = 0;
        arrProb=new double[themeNum];
        for (j = 0; j < themeNum; j++) {
            arrProb[j] = (arrTTCount[j] + delta)/(arrTopicCount[j] + wDelta) *(arrDTCount[j]+ rho);
            totalProb += arrProb[j];
        }
        r = totalProb * random.nextDouble();
        max = arrProb[0];
        topic = 0;
        while (r > max) {
            topic++;
            max += arrProb[topic];
        }
        return topic;
    }

    private int sampleViewTopic(Random random, int[] arrTTViewCount, int[] arrDTCount, int[] arrViewTopicCount){
        double[] arrProb;
        double totalProb, r, max;
        int j,topic;

        totalProb = 0;
        arrProb=new double[themeNum];
        for (j = 0; j < themeNum; j++) {
            arrProb[j] = (arrTTViewCount[j] + epsilon)/(arrViewTopicCount[j] + wEpsilon) *(arrDTCount[j]+ rho);
            totalProb += arrProb[j];
        }
        r = totalProb * random.nextDouble();
        max = arrProb[0];
        topic = 0;
        while (r > max) {
            topic++;
            max += arrProb[topic];
        }
        return topic;
    }

    private int sampleStatus(Random random, int ttViewCount, int sumTTViewCount, int ttCommonCount, int ttCount, int sumTTCount){
        double x0, x1, prob;

        x0=(gamma0+ttCommonCount)* (ttCount + delta)/(sumTTCount + wDelta);
        x1=(gamma1+ttViewCount)*(ttViewCount + epsilon)/(sumTTViewCount + wEpsilon);
        x0=x0/(x0+x1);
        prob=random.nextDouble();
        if(prob<=x0)
            return 0;
        else
            return 1;
    }

    private void readSequence(IndexReader indexReader, int[] arrTerm, int[] arrDoc){
        int[] arrIndex, arrFreq;
        int docNum, i, j, k, count;

        docNum=indexReader.getCollection().getDocNum();
        count=0;
        for(i=0;i<docNum;i++){
            arrIndex=indexReader.getTermIndexList(i);
            arrFreq=indexReader.getTermFrequencyList(i);
            if(arrIndex==null || arrIndex.length==0) continue;
            for(j=0;j<arrIndex.length;j++){
                for(k=0;k<arrFreq[j];k++){
                    arrTerm[count + k] = arrIndex[j];
                    arrDoc[count + k] = i;
                }
                count+=arrFreq[j];
            }
        }
    }
}
