#!/bin/bash

export CLASSPATH=`echo maven-lib/*.jar | tr ' ' ':'`:target/classes
java application.BioSemApplication $*
