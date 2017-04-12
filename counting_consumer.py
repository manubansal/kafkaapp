#!/usr/bin/python

from __future__ import print_function
from kafka import KafkaConsumer
import logging
import time
from datetime import datetime
import sys

def main():
    topic = sys.argv[1]
    epr = int(sys.argv[2])	#events per report
    if len(sys.argv) > 3:
      inp = sys.argv[3]
      inpp = open(inp, "r")
    else:
      inp = None
      inpp = None

    if inpp:
      consumer = inpp
    else:
      consumer = KafkaConsumer(topic, consumer_timeout_ms = 2000)

    current_time = time.mktime(datetime.utcnow().timetuple())
    next_tick = current_time + 1
    nmsgs = 0
    while True:
      for msg in consumer:
	#print(msg)
	nmsgs += 1
	if nmsgs % epr == 0:
	  current_time = time.mktime(datetime.utcnow().timetuple())
	  logging.info("nmsgs: %d, time: %d" % (nmsgs, current_time))
	  print("nmsgs: %d, time: %d" % (nmsgs, current_time))

      current_time = time.mktime(datetime.utcnow().timetuple())
      logging.info("no messages received, timeout at %d" % current_time)
      print("no messages received, timeout at %d" % current_time)


def setup_logging():
    """Setup logging to a file named counting_consumer.out"""
    log_format = '%(asctime)-15s %(levelname)s: %(message)s'
    logging.basicConfig(format=log_format, level=logging.DEBUG,
                        filename='counting_consumer.out')

if __name__ == "__main__":
    setup_logging()
    main()
