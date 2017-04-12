package edu.stanford;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import java.util.UUID;
import java.util.List;
import java.util.Arrays;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;

import org.apache.storm.Config;
import org.apache.storm.StormSubmitter;
import org.apache.storm.generated.AlreadyAliveException;
import org.apache.storm.generated.InvalidTopologyException;
import org.apache.storm.spout.SpoutOutputCollector;
import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.TopologyBuilder;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.topology.base.BaseRichSpout;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;

import java.util.concurrent.TimeUnit;

import org.apache.storm.kafka.KafkaSpout;
import org.apache.storm.kafka.SpoutConfig;
//import org.apache.storm.spout.RawMultiScheme;
import org.apache.storm.spout.SchemeAsMultiScheme;
import org.apache.storm.kafka.StringScheme;
import org.apache.storm.kafka.ZkHosts;


/**
 * This is a topology that does simple word counts.
 */
public final class KafkaCountTopology {
  private KafkaCountTopology() {
  }

  /**
   * A bolt that counts the words that it receives
   */
  public static class ConsumerBolt extends BaseRichBolt {
    private static final long serialVersionUID = -5470591933906954522L;

    private OutputCollector collector;
    private Map<String, Integer> countMap;

    @SuppressWarnings("rawtypes")
    public void prepare(Map map, TopologyContext topologyContext, OutputCollector outputCollector) {
      collector = outputCollector;
      countMap = new HashMap<String, Integer>();
    }

    @Override
    public void execute(Tuple tuple) {
      String key = tuple.getString(0);
      if (countMap.get(key) == null) {
        countMap.put(key, 1);
      } else {
        Integer val = countMap.get(key);
        countMap.put(key, ++val);
      }
      System.out.println("countMap: " + countMap);
      collector.ack(tuple);
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
    }
  }

  /**
   * Main method
   */
  public static void main(String[] args) throws AlreadyAliveException, InvalidTopologyException {
    String topologyName = "kafkaapp"; 
    int numStmgrs = 2;
    int numInstancesConsumer = 2;

    String zkServerHosts = "localhost";
    int zkPort = 2181;
    String kafkaTopic = "test";
    int kafkaPartitions = 1;


    //--------- for kafka word spout ------
    ZkHosts hosts = new ZkHosts(zkServerHosts);

    SpoutConfig spoutConfig = new SpoutConfig(hosts, kafkaTopic, "/" + kafkaTopic, UUID.randomUUID().toString());
    spoutConfig.zkServers = Arrays.asList(new String[]{ zkServerHosts });
    spoutConfig.zkPort = zkPort;

    //to get the kafka message as a tuple with key 'bytes' and value a byte
    //array without any parsing by kafka spout
    //spoutConfig.scheme = new RawMultiScheme();  

    //to get the kafka message as a tuple with key 'str' and value a string
    spoutConfig.scheme = new SchemeAsMultiScheme(new StringScheme());

    //start reading the kafka topic from the most recent message
    spoutConfig.startOffsetTime = kafka.api.OffsetRequest.LatestTime();
    //----------------------------------
    
    TopologyBuilder builder = new TopologyBuilder();
    KafkaSpout kafkaSpout = new KafkaSpout(spoutConfig);
    builder.setSpout("kafkaword", kafkaSpout, kafkaPartitions);
    builder.setBolt("consumer", new ConsumerBolt(), numInstancesConsumer)
      .fieldsGrouping("kafkaword", new Fields("kafkaword"));

    // Set up the topology config 
    Config conf = new Config();
    conf.setNumStmgrs(numStmgrs);

    //--------- for kafka word spout ------
    //NOTE: MUST set these parameters or kafka spout will run into null pointer
    //exceptions, even though these parameters are not part of spoutConfig;
    //kafka spout assumes these parameters to be set in the topology
    //configuration, and throws unhelpful exceptions otherwise
    conf.put("storm.zookeeper.session.timeout", 20000);
    conf.put("storm.zookeeper.connection.timeout", 15000);
    conf.put("storm.zookeeper.retry.times", 5); 
    conf.put("storm.zookeeper.retry.interval", 1000);
    //----------------------------------


    long MEGABYTE = 1024 * 1024;
    long maxContainerDisk = 100L * MEGABYTE;
   
    conf.setContainerMaxDiskHint(maxContainerDisk); 
    conf.setContainerDiskRequested(500L * MEGABYTE);

    conf.setContainerPaddingPercentage(5); 

    StormSubmitter.submitTopology(topologyName, conf, builder.createTopology());
  }
}
