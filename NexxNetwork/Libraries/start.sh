#!/bin/sh

instance="SLAVE_CONTROLLER"

while true
do
	java -Xmx1G -Xms1G \
	-DServerType="${instance}" \
	-DDataFolder="/home/minecraft/data" \
	-Xloggc:gc.log \
	-verbose:gc \
	-XX:+PrintGCDetails \
	-XX:+PrintGCDateStamps \
	-XX:+PrintGCTimeStamps \
	-XX:+UseGCLogFileRotation \
	-XX:NumberOfGCLogFiles=5 \
	-XX:GCLogFileSize=1M \
	-XX:+UseG1GC \
	-XX:+DisableExplicitGC \
	-Dfile.encoding=UTF-8 \
	-Djava.awt.headless=true \
	-Djava.rmi.server.hostname=66.70.180.227 \
	-Dcom.sun.management.jmxremote \
	-Dcom.sun.management.jmxremote.ssl=false \
	-Dcom.sun.management.jmxremote.authenticate=false \
	-Dorg.bukkit.plugin.java.JavaPluginLoader.checkForPrisonYml=true \
	-jar slavecontroller.jar

	echo "If you want to completely stop the server process now, press Ctrl+C before the time is up!"
	echo "Rebooting in:"
	for i in 5 4 3 2 1
	do
		echo "$i..."
		sleep 1
	done
	echo "Rebooting now!"
done
