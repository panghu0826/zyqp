#!/usr/bin/env bash
/home/work/soft/jdk1.8.0_151/bin/java -Djava.awt.headless=true -Duser.timezone=GMT+08 -Xms64M -Xmx64M -XX:PermSize=64M -XX:MaxPermSize=64M -XX:+UseConcMarkSweepGC -XX:+UseParNewGC -XX:+CMSParallelRemarkEnabled -XX:ErrorFile=hs_error%p.log -XX:OnError="pmap %p;jmap -dump:format=b,file=logs/jmap%p.log %p;jstack %p;jstat -gcutil %p;" -Djava.net.preferIPv4Stack=true -cp "/home/work/soft/ideawork/qchy3Ddq/management/lib/*:." com.guosen.webx.web.ServerJetty config/server-prd.properties