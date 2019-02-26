#!/bin/bash
count=1
while [ $count -le 60 ]; do
  pid=`ps -ef |grep FullNode.jar |grep java-tron |grep -v grep |awk '{print $2}'`
  echo $pid
  if [ -n "$pid" ]; then
    sudo kill -15 $pid
    echo "kill -15 java-tron, counter $count"
    sleep 1
  else
    echo "java-tron killed"
    break
  fi
  count=$[$count+1]
  if [ $count -ge 60 ]; then
    kill -9 $pid
  fi
done
