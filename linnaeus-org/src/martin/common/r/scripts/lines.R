rm(list=ls());

inFile <- commandArgs(trailingOnly=T)[1];
outFile <- commandArgs(trailingOnly=T)[2];
#inFile <- "z:\\temp\\1542367370.temp";

png(filename=outFile, width=500, height=500,units="px",bg="white");

frame();

data <- read.table(inFile,sep=",",header=T);

attach(data)
labels <- unique(data[,3]);
colors <- 1:length(labels);

for (i in 1:length(labels)) {
	localdata <- data[1:2];
	plot(localdata,type="n");
}

for (i in 1:length(labels)) {
	localdata <- data[set == labels[i],1:2];
	lines(localdata, col = 1, lty = i);
}

legend("topright",legend=labels,lty=colors);

detach(data);
dev.off();