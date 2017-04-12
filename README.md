# Heron kafka word count example application

This is a complete Heron example application that mimics the Heron example
WordCountTopology while consuming words from Kafka using a kafka spout instead
of using a word-generating spout.

## Requirements

* Kafka version 2.11-0.10.0.1 (follow setup instructions on Apache Kafka release page)
* Heron version 0.14.5 (follow setup instructions on Twitter Heron release page)
* Python 2.7.6 (may work with python3 too)
* kafka-python: `sudo pip install kafka-python`

## Setup Kafka

Follow instructions on Apache Kafka release page to setup a local kafka
instance. At the end of successful Kafka installation and kafka startup,
zookeeper support kafka should be serving at localhost:2181 and a kafka broker
should be serving at localhost:9092.

## Setup Heron

Follow instructions on Twitter Heron release page to setup a local heron
instance. At the end of successful Heron installation, you should be able to
submit/run/kill a Heron topology with commands like:

```
$ heron submit local --config-path ~/.heron/conf/ <application-jar> <application-class> <topology-name>
$ heron kill local <topology-name>
```

You should also be able to start `heron-tracker` and `heron-ui` and see the
topology running at http://localhost:8889.


## Run a throttled kafka producer

Make sure kafka-python is installed and kafka is up and running. Then, run a
throttled unbounded producer to start feeding a data stream to kafka:

```
#python producer using kafka-python
$ ./throttled_producer.py <topic> <eps>
$ ./throttled_producer.py test 1000
```

topic: kafka topic to produce messages to  
eps: events/messages per second to produce


You can verify the event stream with a counting unbounded consumer:

```
#python consumer using kafka-python
$ ./counting_consumer.py <topic> <epr>
$ ./counting_consumer.py test 1000
```

topic: kafka topic to consume messages from  
epr: events/messages per report (expected eps)

You can use another consumer like the kafka console consumer included with
kafka to verify the event stream.


## Build the heron kafka word count example application

```
$ mvn package
```


## Run the application

Make sure Heron 0.14.5 is already set up on localhost and is accepting topologies. Then, submit the application using 
the included convenience `heronctl.sh` script:

```
$ ./heronctl.sh local submit
```

## Verify run results

If all goes well, you should see non-zero emit counts on the Heron Tracker UI
at http://localhost:8889/topologies/local/default/kafkaapp. Also, you should
see logs for kafka spout called `kafkaword` and consumer bolts called
`consumer` in ~/.herondata/topologies/local/<username>/kafkaapp/log-files. A
consumer log should show countMap output with lines like the following:

```
[2017-04-11 16:01:17 -0700] stdout STDOUT:  countMap: {323k2xsdwf63dqtp5ysg=1, oxr3w453v932hmjpveb0=1, 9seun0m2bqtr2gz11w1e=1, kos856oaqlfb9r49vc21=1, gb7yoa01ds95qy7j53od=1, djt9fyodgt25tt02fmz8=1, htfgvigrceicvjbcz2vc=1, vaqwczxlg3wcvlonub1m=1, lksx54f17i3l7og8civd=1, ok0f4...
```

## Stop the application 

You can kill the topology as follows:


```
$ ./heronctl.sh local kill
```
