#!/bin/bash

if [[ -d "upload" ]]; then
	echo "Removing directory 'upload' for fresh file creation"
	rm -r upload
fi

mkdir upload

VERSION=2.0

cp pom.xml upload/linnaeus-$VERSION.pom
cp ../bin/linnaeus-2.0.jar upload/linnaeus-$VERSION.jar
jar cf upload/linnaeus-$VERSION-sources.jar -C ../src/ .
jar cf upload/linnaeus-$VERSION-javadoc.jar ../javadoc/ .

gpg --use-agent --output upload/linnaeus-$VERSION.pom.asc --detach-sign upload/linnaeus-$VERSION.pom
gpg --use-agent --output upload/linnaeus-$VERSION.jar.asc --detach-sign upload/linnaeus-$VERSION.jar
gpg --use-agent --output upload/linnaeus-$VERSION-sources.jar.asc --detach-sign upload/linnaeus-$VERSION-sources.jar
gpg --use-agent --output upload/linnaeus-$VERSION-javadoc.jar.asc --detach-sign upload/linnaeus-$VERSION-javadoc.jar

