#!/bin/bash
# This script can train and test BioSem on the BioNLP Shared Task data of 2009, 2011 and 2013.
# The BioSem code must have been built before via 'mvn clean package'.
# Parameters: BioNLP Edition (2009, 2011, 2013) ; Training data (train or mixed (train+devel)) ; Test data (devel or test)


set -euo pipefail

if [[ $# -eq 0 ]]; then
  echo "Parameters: <BioNLP ST Edition: '2009', '2011', '2013'> <training data: 'train' or 'mixed' (mixed=train+devel)> <test data: 'devel' or 'test'>"
  exit 1
fi

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

if [[ "$EDITION" == "2009" ]]; then

  if [[ ! -d "$STBASE" ]]; then
    git -C $DOWNLOAD clone https://github.com/openbiocorpora/bionlp-st-2009.git
  fi

elif [[ "$EDITION" == "2011" ]]; then

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

echo "Looking for existing training database to clear."
if [[ ! -z `ls $TRAIN_DB.*` ]]; then

  rm -r $TRAIN_DB.*

fi

echo "Looking for existing test database to clear."
if [[ ! -z `ls $TEST_DB.*` ]]; then

  rm -r $TEST_DB.*

fi

echo "Train data $TRAIN_DATA"
echo "Test data: $TEST_DATA"

echo "Creating database for train texts"
java corpora.DataLoader "$TRAIN_DATA" "$TRAIN_DB" true
echo "Learning triggers"
java relations.TriggerLearner "$TRAIN_DB" "$TRAIN_DB"
echo "Learning event patterns"
java relations.RuleLearner "$TRAIN_DB"
echo "Creating database for test data: java corpora.DataLoader "$TEST_DATA" "$TEST_DB" false"
java corpora.DataLoader "$TEST_DATA" "$TEST_DB" false
echo "Extracting events from test data to $TEST_OUTPUT"
if [[ ! -d "$TEST_OUTPUT" ]]; then

  mkdir $TEST_OUTPUT

elif [[ ! -z $TEST_OUTPUT ]]; then

  rm $TEST_OUTPUT/*

fi
java relations.EventExtraction "$TRAIN_DB" "$TEST_DB" "$TEST_OUTPUT"

if [[ "$EVAL_TARGET" == "devel" ]]; then

    if [[ "$EDITION" == "2009" || "$EDITION" == "2011" ]]; then

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

  if [[ "$EDITION" == "2011" ]]; then

    echo "Overall evaluation result on $EVAL_TARGET for $EDITION"
    perl "$EVALTOOLS"/a2-evaluate.pl -g $TEST_DATA -t1 $TEST_OUTPUT/PMID-*.a2
    echo "Abstracts-only result on $EVAL_TARGET for $EDITION"
    perl "$EVALTOOLS"/a2-evaluate.pl -g $TEST_DATA -t1 $TEST_OUTPUT/PMC-*.a2
    echo "Fulltexts-only result on $EVAL_TARGET for $EDITION"

  fi

elif [[ "$EVAL_TARGET" == "test" && "$EDITION" == "2013" ]]; then
  # the 2013 edition has encoding issues in some file names about NFκB; the test evaluation will not work
  # unless this is fixed
  for i in $TEST_OUTPUT/*; do mv $i `echo $i | sed 's/+%A6/κ/'`; done

fi

if [[ "$EVAL_TARGET" == "test" ]]; then

  echo "Making tar ball of test data output named 'test-output-$EDITION.tar.gz' that can be sent to the online evaluation at http://bionlp-st.dbcls.jp/GE/$EDITION/eval-test/"

  if [[ "$EDITION" == "2009" ]]; then
    # The data we download for the 2009 edition does not specify the "PMID-" prefix that is expected by the evaluation service
    rm -rf evaltmp;
    mkdir evaltmp;
    cp $TEST_OUTPUT/*.a2 evaltmp;
    cd evaltmp;
    for i in *; do
      mv $i "PMID-$i";
    done
    cd ..
    TEST_OUTPUT=evaltmp
  fi
  # from https://stackoverflow.com/a/39530409/1314955
  echo "find $TEST_OUTPUT -name '*.a2' -printf "%P\n" | tar -czf test-output-$EDITION.tar.gz --no-recursion -C $TEST_OUTPUT -T -"
  find $TEST_OUTPUT -name '*.a2' -printf "%P\n" | tar -czf test-output-$EDITION.tar.gz --no-recursion -C $TEST_OUTPUT -T -
  if [[ -d evaltmp ]]; then rm -rf evaltmp; fi

fi
