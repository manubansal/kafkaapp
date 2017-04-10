#!/bin/bash


#CLUSTER=aurora/ubuntu/devel
CLUSTER=${1:-local}
COMMAND=${2:-submit}

CONFDIR=~/.heron/conf/
JAR=~/workspace/kafkaapp/target/kafkaapp-1.0-SNAPSHOT-jar-with-dependencies.jar

TOPOLOGY=kafkaapp
CLASS=edu.stanford.KafkaCountTopology

if [[ $COMMAND = "submit" ]]; then
  heron $COMMAND $CLUSTER --config-path $CONFDIR $JAR $CLASS $TOPOLOGY
elif [[ $COMMAND = "forcekill" ]]; then
  find ~/.herondata -name *$TOPOLOGY* -exec rm -rf {} \;
else
  heron $COMMAND $CLUSTER $TOPOLOGY
fi
