#!/usr/bin/python

from __future__ import print_function
from kafka import KafkaProducer
import logging
import json
import sys
import time
from datetime import datetime
import subprocess
import random

ARRAY_LENGTH = 128 * 1024
WORD_LENGTH = 20

class RandomString:
  def __init__(self, length):
    tmp = ""
    tmp += "".join([chr(i) for i in range(ord('0'),ord('9')+1)])
    tmp += "".join([chr(i) for i in range(ord('a'),ord('z')+1)])
    self.symbols = tmp
    self.word_length = length

  def nextString(self):
    symbols = self.symbols
    tmp = [symbols[random.randint(0, len(symbols) - 1)] \
      for _ in range(self.word_length)]
    word = "".join(tmp)
    return word

def init_word_array():
  randomString = RandomString(WORD_LENGTH)
  words = [randomString.nextString() for i in range(ARRAY_LENGTH)]
  return words

def produce_messages(words, producer, topic, n=1, outp=None):
  #current_time = time.mktime(datetime.utcnow().timetuple())
  #print("producing message at", current_time)

  #msg = b'some_message_bytes'
  nextInts = [random.randint(0, ARRAY_LENGTH - 1) for _ in range(n)]
  msgs = [words[nextInt] for nextInt in nextInts]
  if outp:
    for i in range(n):
      msg = msgs[i]
      #print(msg)
      outp.write(msg + "\n")
    outp.flush()
  else:
    for i in range(n):
      msg = msgs[i]
      #print(msg)
      producer.send(topic, msg)


def wait_with_timeout(until):
    """Sleeps until an input time."""
    current_time = time.mktime(datetime.utcnow().timetuple())
    while current_time < until:
        time.sleep(0.1)
        current_time = time.mktime(datetime.utcnow().timetuple())

def main():
    topic = sys.argv[1]
    eps = int(sys.argv[2])
    if len(sys.argv) > 3:
      out = sys.argv[3]
      outp = open(out, "wb")
    else:
      out = None
      outp = None

    logging.info("topic = %s", topic)
    logging.info("eps = %d", eps)
    logging.info("out = %s", out)

    current_time = time.mktime(datetime.utcnow().timetuple())
    next_tick = current_time + 1
    msg_count = 0
    nmsgs = 0

    #producer = None
    producer = KafkaProducer(\
      bootstrap_servers='localhost:9092', \
      linger_ms=200, \
      acks=1)

    words = init_word_array()

    N = eps
    while True:
	produce_messages(words, producer, topic, N, outp)
	msg_count += N

	if eps and msg_count >= eps:
	    nmsgs += msg_count
	    current_time = time.mktime(datetime.utcnow().timetuple())
	    logging.info("dumped %10d events, time: %d" % (nmsgs, current_time))
	    print("dumped %10d events, time: %d" % (nmsgs, current_time))
	    msg_count = 0
	    wait_with_timeout(next_tick)
	    next_tick += 1


def setup_logging():
    """Setup logging to a file named throttled_producer.out"""
    log_format = '%(asctime)-15s %(levelname)s: %(message)s'
    logging.basicConfig(format=log_format, level=logging.DEBUG,
                        filename='throttled_producer.out')

if __name__ == "__main__":
    setup_logging()
    main()
