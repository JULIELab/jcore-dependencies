#this will currently only accept a single set. set == -1 denoted x-axis names.

rm(list=ls());

inFile <- commandArgs(trailingOnly=T)[1];
outFile <- commandArgs(trailingOnly=T)[2];
#inFile <- "z:\\temp\\temp.csv";

png(filename=outFile, width=500, height=500,units="px",bg="white");

frame();

data <- read.table(inFile,sep=",",header=T);

attach(data)
labels <- unique(data[set != -1,2]);
colors <- 1:length(labels);

for (i in 1:length(labels)) {
	barplot(data[set == labels[i],1],names.arg=data[set == -1, 1]);
}

#legend("topright",legend=labels,col=colors);

detach(data);
dev.off();