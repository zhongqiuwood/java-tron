stressStartNum=25163
stressEndNum=25707

stressServer=47.52.254.128:8090
stressStartTime=`curl -s -X POST http://$stressServer/wallet/getblockbynum -d \{"num":$stressStartNum}\ | jq .block_header.raw_data.timestamp`
stressEndTime=`curl -s -X POST http://$stressServer/wallet/getblockbynum -d \{"num":$stressEndNum}\ | jq .block_header.raw_data.timestamp`
TransactionCount=1
echo $stressStartTime
echo $stressEndTime

for((i=$stressStartNum;i<=$stressEndNum;i++));
do
transactionNumInThisBlock=`curl -s -X POST http://$stressServer/wallet/getblockbynum -d \{"num":$i}\ | jq . | grep "txID" | wc -l`
TransactionCount=$[$TransactionCount+$transactionNumInThisBlock]
done
targetTime=$((stressEndNum - stressStartNum))
targetTime=`expr $targetTime \* 3000`
echo "targetTime is "$targetTime
costTime=$((stressEndTime - stressStartTime))
costHours=$(printf "%.2f" `echo "scale=2;$costTime/3600000"|bc`)
echo "costTime is "$costTime
tps=$(($TransactionCount*1000/$costTime))
backwardTime=$(($costTime-$targetTime))
MissBlockRate=`awk 'BEGIN{printf "%.1f%\n",('$backwardTime'/'$costTime')*100}'`
echo "2.Stress Pressure report: Total transactions: $TransactionCount, cost time: $costHours"h", push block average tps: $tps/s, MissBlockRate: $MissBlockRate"
