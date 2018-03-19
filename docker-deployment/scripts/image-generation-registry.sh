#!/bin/bash

#
# Copyright Indra Sistemas, S.A.
# 2013-2018 SPAIN
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#      http://www.apache.org/licenses/LICENSE-2.0
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
# ------------------------------------------------------------------------

buildImage()
{
	echo "Docker image generation with spotify plugin for Sofia2 module: "$1 
	mvn clean package docker:build -Dmaven.test.skip=true
}

buildConfigDB()
{
	echo "ConfigDB image generation with Docker CLI: "
	docker build -t sofia2/configdb:$1 .
}

buildSchedulerDB()
{
	echo "SchedulerDB image generation with Docker CLI: "
	docker build -t sofia2/schedulerdb:$1 .
}

buildRealTimeDB()
{
	echo "RealTimeDB image generation with Docker CLI: "
	docker build -t sofia2/realtimedb:$1 .
}

buildNginx()
{
	echo "NGINX image generation with Docker CLI: "
	docker build -t sofia2/nginx:$1 .		
}

buildQuasar()
{
	echo "Quasar image generation with Docker CLI: "
	echo "Step 1: download quasar binary file"
	wget https://github.com/quasar-analytics/quasar/releases/download/v14.2.6-quasar-web/quasar-web-assembly-14.2.6.jar
	
	echo "Step 2: build quasar image"
	docker build -t sofia2/quasar:$1 .	
	
	rm quasar-web-assembly*.jar
}

prepareNodeRED()
{
	cp $homepath/../tools/Flow-Engine-Manager/*.zip $homepath/../modules/flow-engine/docker/nodered.zip
	cd $homepath/../modules/flow-engine/docker
	unzip nodered.zip		
	cp -f $homepath/dockerfiles/nodered/proxy-nodered.js $homepath/../modules/flow-engine/docker/Flow-Engine-Manager/
	cp -f $homepath/dockerfiles/nodered/sofia2-config-nodes-config.js $homepath/../modules/flow-engine/docker/Flow-Engine-Manager/node_modules/node-red-sofia/nodes/config/sofia2-config.js
	cp -f $homepath/dockerfiles/nodered/sofia2-config-public-config.js $homepath/../modules/flow-engine/docker/Flow-Engine-Manager/node_modules/node-red-sofia/public/config/sofia2-config.js	
}

removeNodeRED()
{
	cd $homepath/../modules/flow-engine/docker
	rm -rf Flow-Engine-Manager
	rm nodered.zip		
}

pushAllImages2Registry()
{
	docker tag sofia2/configdb:$1 moaf-nexus.westeurope.cloudapp.azure.com:443/sofia2/configdb:$1
	docker push moaf-nexus.westeurope.cloudapp.azure.com:443/sofia2/configdb:$1	

	docker tag sofia2/schedulerdb:$1 moaf-nexus.westeurope.cloudapp.azure.com:443/sofia2/schedulerdb:$1
	docker push moaf-nexus.westeurope.cloudapp.azure.com:443/sofia2/schedulerdb:$1	
	
	docker tag sofia2/realtimedb:$1 moaf-nexus.westeurope.cloudapp.azure.com:443/sofia2/realtimedb:$1
	docker push moaf-nexus.westeurope.cloudapp.azure.com:443/sofia2/realtimedb:$1	
	
	docker tag sofia2/controlpanel:$1 moaf-nexus.westeurope.cloudapp.azure.com:443/sofia2/controlpanel:$1
	docker push moaf-nexus.westeurope.cloudapp.azure.com:443/sofia2/controlpanel:$1	
	
	docker tag sofia2/iotbroker:$1 moaf-nexus.westeurope.cloudapp.azure.com:443/sofia2/iotbroker:$1
	docker push moaf-nexus.westeurope.cloudapp.azure.com:443/sofia2/iotbroker:$1	
	
	docker tag sofia2/apimanager:$1 moaf-nexus.westeurope.cloudapp.azure.com:443/sofia2/apimanager:$1
	docker push moaf-nexus.westeurope.cloudapp.azure.com:443/sofia2/apimanager:$1	
	
	docker tag sofia2/flowengine:$1 moaf-nexus.westeurope.cloudapp.azure.com:443/sofia2/flowengine:$1
	docker push moaf-nexus.westeurope.cloudapp.azure.com:443/sofia2/flowengine:$1

	docker tag sofia2/devicesimulator:$1 moaf-nexus.westeurope.cloudapp.azure.com:443/sofia2/devicesimulator:$1
	docker push moaf-nexus.westeurope.cloudapp.azure.com:443/sofia2/devicesimulator:$1
	
	docker tag sofia2/dashboard:$1 moaf-nexus.westeurope.cloudapp.azure.com:443/sofia2/dashboard:$1
	docker push moaf-nexus.westeurope.cloudapp.azure.com:443/sofia2/dashboard:$1	
	
	docker tag sofia2/monitoringui:$1 moaf-nexus.westeurope.cloudapp.azure.com:443/sofia2/monitoringui:$1
	docker push moaf-nexus.westeurope.cloudapp.azure.com:443/sofia2/monitoringui:$1		
	
	docker tag sofia2/nginx:$1 moaf-nexus.westeurope.cloudapp.azure.com:443/sofia2/nginx:$1
	docker push moaf-nexus.westeurope.cloudapp.azure.com:443/sofia2/nginx:$1		
	
	docker tag sofia2/quasar:$1 moaf-nexus.westeurope.cloudapp.azure.com:443/sofia2/quasar:$1
	docker push moaf-nexus.westeurope.cloudapp.azure.com:443/sofia2/quasar:$1							
}

pushImage2Registry()
{
	echo "¿Deploy "$1 " image to registry y/n: "
	read confirmation
	if [ "$confirmation" == "y" ]; then
		docker tag sofia2/$1:$2 moaf-nexus.westeurope.cloudapp.azure.com:443/sofia2/$1:$2
		docker push moaf-nexus.westeurope.cloudapp.azure.com:443/sofia2/$1:$2	
	fi	
}

echo "##########################################################################################"
echo "#                                                                                        #"
echo "#   _____             _                                                                  #"              
echo "#  |  __ \           | |                                                                 #"            
echo "#  | |  | | ___   ___| | _____ _ __                                                      #"
echo "#  | |  | |/ _ \ / __| |/ / _ \ '__|                                                     #"
echo "#  | |__| | (_) | (__|   <  __/ |                                                        #"
echo "#  |_____/ \___/ \___|_|\_\___|_|                                                        #"                
echo "#                                                                                        #"
echo "# Sofia2 Docker Image generation                                                         #"
echo "# arg1 (opt) --> -1 if only want to create images for persistence layer                  #"
echo "#                                                                                        #"
echo "##########################################################################################"

if [ -z "$1" ]; then
	echo "Continue? y/n: "
	
	read confirmation
	
	if [ "$confirmation" != "y" ]; then
		exit 1
	fi
fi

homepath=$PWD

# Only create persistence layer
if [ -z "$1" ]; then
	# Generates images only if they are not present in local docker registry
	if [[ "$(docker images -q sofia2/controlpanel 2> /dev/null)" == "" ]]; then
		cd $homepath/../modules/control-panel/
		buildImage "Control Panel"
	fi	
	
	if [[ "$(docker images -q sofia2/iotbroker 2> /dev/null)" == "" ]]; then
		cd $homepath/../modules/iotbroker/sofia2-iotbroker-boot/	
		buildImage "IoT Broker"
	fi
	
	if [[ "$(docker images -q sofia2/apimanager 2> /dev/null)" == "" ]]; then	
		cd $homepath/../modules/api-manager/	
		buildImage "API Manager"
	fi
	
	if [[ "$(docker images -q sofia2/dashboard 2> /dev/null)" == "" ]]; then
		cd $homepath/../modules/dashboard-engine/
		buildImage "Dashboard Engine"
	fi
	
	if [[ "$(docker images -q sofia2/devicesimulator 2> /dev/null)" == "" ]]; then
		cd $homepath/../modules/device-simulator/
		buildImage "Device Simulator"
	fi	
	
	if [[ "$(docker images -q sofia2/monitoringui 2> /dev/null)" == "" ]]; then
		cd $homepath/../modules/monitoring-ui/
		buildImage "Monitoring UI"
	fi		
	
	if [[ "$(docker images -q sofia2/flowengine 2> /dev/null)" == "" ]]; then		
 		prepareNodeRED		
	
		cd $homepath/../modules/flow-engine/
		buildImage "Flow Engine"
		
		removeNodeRED
	fi	
fi

# Generates images only if they are not present in local docker registry
if [[ "$(docker images -q sofia2/configdb 2> /dev/null)" == "" ]]; then
	cd $homepath/dockerfiles/configdb
	buildConfigDB latest
fi

if [[ "$(docker images -q sofia2/schedulerdb 2> /dev/null)" == "" ]]; then
	cd $homepath/dockerfiles/schedulerdb
	buildSchedulerDB latest
fi

if [[ "$(docker images -q sofia2/realtimedb 2> /dev/null)" == "" ]]; then
	cd $homepath/dockerfiles/realtimedb
	buildRealTimeDB latest
fi

if [[ "$(docker images -q sofia2/nginx 2> /dev/null)" == "" ]]; then
	cd $homepath/dockerfiles/nginx
	buildNginx latest
fi

if [[ "$(docker images -q sofia2/quasar 2> /dev/null)" == "" ]]; then
	cd $homepath/dockerfiles/quasar
	buildQuasar latest
fi

echo "Docker images successfully generated!"

echo "Push Sofia2 images to private registry"

pushImage2Registry configdb latest 
pushImage2Registry schedulerdb latest 
pushImage2Registry realtimedb latest 
pushImage2Registry controlpanel latest 
pushImage2Registry iotbroker latest 
pushImage2Registry apimanager latest 
pushImage2Registry flowengine latest 
pushImage2Registry devicesimulator latest 
pushImage2Registry dashboard latest 
pushImage2Registry monitoringui latest 
pushImage2Registry nginx latest
pushImage2Registry quasar latest 

# pushAllImages2Registry latest

exit 0