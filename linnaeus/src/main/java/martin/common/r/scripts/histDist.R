rm(list=ls());

numcells <- 50;

inFile <- commandArgs(trailingOnly=T)[1];
outFile <- commandArgs(trailingOnly=T)[2];

png(filename=outFile, width=500, height=500,units="px",bg="white");

frame();

data <- read.table(inFile,sep=",",header=T);

attach(data)
labels <- unique(data[,2]);
colors <- 1:length(labels);

max <- -Inf;
min <- Inf;
for (i in 1:length(labels)) {
	label <- labels[i];
	localdata <- data[set == labels[i],1];
	
	max <- max(max(localdata),max);
	min <- min(min(localdata),min);
}

breaks <- seq(min,max,(max-min)/numcells);

maxcounts <- -Inf;
mincounts <- Inf;

for (i in 1:length(labels)) {
	cutdata <- table(cut(data[set == labels[i],1],breaks));

	maxcounts <- max(max(cutdata),maxcounts);
	mincounts <- min(min(cutdata),mincounts);

	plot(cutdata,type="n");
}

for (i in 1:length(labels)) {
	cutdata <- cut(data[set == labels[i],1],breaks);
	lines(table(cutdata), col=1, lty=i);
}

legend(35,50,labels,lty=colors);

detach(data);
dev.off();