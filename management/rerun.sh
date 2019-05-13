#!/usr/bin/env bash
# */1 * * * *  root cd /home/guosen/ws/gold-pointing/demo && ./rerun.sh > rerun.output
# */1 * * * *  root cd /home/guosen/ws/gold-pointing/demo && date > rerun-date.log
echo 'check'
p=8888
r=`lsof -i:$p | grep java`
if [ "$r" != "" ]; then
  echo 'yes'
  date
else
  time=`date +%F-%H-%M-%S`
  log=rerun-$time.log
  echo 'reruned' > $log
  # change to jetty
  /data/server/poker/management/run-server-jetty.sh > run.output &
  echo 'rerun..'
  date
fi
