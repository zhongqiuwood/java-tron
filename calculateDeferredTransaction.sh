#!/usr/bin/env bash
sum=0
cat  logs/tron.log | grep "cost/txs"  | while read line2
do
cost=`echo $line2 | awk -F ':' '{print $7}'`
if [ $cost -gt $1 ]; then
echo $line2
sum=$(($sum+$cost))
echo $sum
fi
done
