#!/bin/bash
# This script can train and test BioSem on the BioNLP Shared Task data of 2009, 2011 and 2013.
# The program must have been built before via 'mvn clean package'.
# Parameters: BioNLP Edition (2009, 2011, 2013) ; Training data (train or mixed (train+devel)) ; Test data (devel or test)


set -euo pipefail

DATA=evaldata
DOWNLOAD=$DATA/download

# 2009, 2011 or 2013
EDITION=$1
# Train or mixed (train + devel)
TRAIN_SOURCE=$2
# devel or test
EVAL_TARGET=$3
TASK="-ge"
# The git repository for 2009 does not carry a suffix like 'ge'
if [[ "$EDITION" = "2009" ]]; then
  TASK=""
fi

TEST_OUTPUT=$DATA/test-output

STBASE=$DOWNLOAD/bionlp-st-$EDITION$TASK/original-data
STTRAIN=$STBASE/train
STDEVEL=$STBASE/devel
# mixed = train + devel
STMIXED=$STBASE/mixed
STDBS=$DATA/dbs/st$EDITION

export CLASSPATH=`echo maven-lib/*.jar | tr ' ' ':'`:target/classes
mkdir -p $DOWNLOAD

if [[ "$EDITION" == "2011" ]]; then

  if [[ ! -d "$STBASE" ]]; then
    git -C $DOWNLOAD clone https://github.com/openbiocorpora/bionlp-st-2011-ge.git
  fi

elif [[ "$EDITION" == "2013" ]]; then

  if [[ ! -d "$STBASE" ]]; then
    git -C $DOWNLOAD clone https://github.com/openbiocorpora/bionlp-st-2013-ge.git
  fi

fi

if [[ "$EVAL_TARGET" == "test" ]]; then

    TEST_DATA="$STBASE"/test
    TEST_DB="$STDBS"/test

  elif [[ "$EVAL_TARGET" == "devel" ]]; then

    TEST_DATA="$STBASE"/devel
    TEST_DB="$STDBS"/devel

  fi

 # Prepare mixed training data
if [[ $TRAIN_SOURCE == "mixed" ]]; then

  TRAIN_DATA="$STBASE"/mixed
  TRAIN_DB="$STDBS"/mixed

  if [[ ! -d "$STMIXED" ]]; then

    echo "Copy train and dev data into $STMIXED"
    mkdir "$STMIXED"
    cp "$STTRAIN"/* "$STMIXED"
    cp "$STDEVEL"/* "$STMIXED"

  fi

elif [[ $TRAIN_SOURCE == "train" ]]; then

  TRAIN_DATA="$STBASE"/train
  TRAIN_DB="$STDBS"/train

fi

if [[ ! -z `ls $TRAIN_DB.*` ]]; then

  rm -r $TRAIN_DB.*

fi

if [[ ! -z `ls $TEST_DB.*` ]]; then

  rm -r $TEST_DB.*

fi

echo "Creating database for train texts"
java corpora.DataLoader "$TRAIN_DATA" "$TRAIN_DB" true
echo "Learning triggers"
java relations.TriggerLearner "$TRAIN_DB" "$TRAIN_DB"
echo "Learning event patterns"
java relations.RuleLearner "$TRAIN_DB"
echo "Creating database for test data"
java corpora.DataLoader "$TEST_DATA" "$TEST_DB" false
echo "Extracting events from test data to $TEST_OUTPUT"
if [[ ! -d "$TEST_OUTPUT" ]]; then

  mkdir $TEST_OUTPUT

elif [[ ! -z $TEST_OUTPUT ]]; then

  rm $TEST_OUTPUT/*

fi
java relations.EventExtraction "$TRAIN_DB" "$TEST_DB" "$TEST_OUTPUT"

if [[ "$EVAL_TARGET" == "devel" ]]; then

    if [[ "$EDITION" = "2009" || "$EDITION" = "2011" ]]; then

      EVALTOOLS_URL=http://bionlp-st.dbcls.jp/GE/2011/downloads/BioNLP-ST_2011_genia_tools_rev1.tar.gz
      EVALTOOLS_FILE=BioNLP-ST_2011_genia_tools_rev1.tar.gz
      EVALTOOLS=$DATA/BioNLP-ST_2011_genia_tools_rev1

    elif [[ "$EDITION" == "2013" ]]; then

      EVALTOOLS_URL=http://bionlp-st.dbcls.jp/GE/2013/downloads/BioNLP-ST-2013-GE-tools.tar.gz
      EVALTOOLS_FILE=BioNLP-ST-2013-GE-tools.tar.gz
      EVALTOOLS=$DATA/tools

    fi

    if [[ ! -d "$EVALTOOLS" ]]; then

      wget -P $DOWNLOAD $EVALTOOLS_URL
      tar -xzf $DOWNLOAD/$EVALTOOLS_FILE -C $DATA

    fi

  echo "perl $EVALTOOLS/a2-evaluate.pl -g $TEST_DATA -t1 $TEST_OUTPUT/*.a2"
  perl "$EVALTOOLS"/a2-evaluate.pl -g $TEST_DATA -t1 $TEST_OUTPUT/*.a2

fi