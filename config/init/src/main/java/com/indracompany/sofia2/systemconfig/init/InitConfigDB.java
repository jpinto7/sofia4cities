/**
 * Copyright Indra Sistemas, S.A.
 * 2013-2018 SPAIN
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *      http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.indracompany.sofia2.systemconfig.init;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.annotation.PostConstruct;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.stereotype.Component;
import org.springframework.test.context.junit4.SpringRunner;

import com.indracompany.sofia2.config.model.ActionsDigitalTwinType;
import com.indracompany.sofia2.config.model.ClientConnection;
import com.indracompany.sofia2.config.model.ClientPlatform;
import com.indracompany.sofia2.config.model.ClientPlatformOntology;
import com.indracompany.sofia2.config.model.Configuration;
import com.indracompany.sofia2.config.model.ConsoleMenu;
import com.indracompany.sofia2.config.model.Dashboard;
import com.indracompany.sofia2.config.model.DashboardUserAccessType;
import com.indracompany.sofia2.config.model.DataModel;
import com.indracompany.sofia2.config.model.DeviceSimulation;
import com.indracompany.sofia2.config.model.DigitalTwinDevice;
import com.indracompany.sofia2.config.model.DigitalTwinType;
import com.indracompany.sofia2.config.model.EventsDigitalTwinType;
import com.indracompany.sofia2.config.model.EventsDigitalTwinType.Type;
import com.indracompany.sofia2.config.model.FlowDomain;
import com.indracompany.sofia2.config.model.Gadget;
import com.indracompany.sofia2.config.model.GadgetDatasource;
import com.indracompany.sofia2.config.model.GadgetMeasure;
import com.indracompany.sofia2.config.model.MarketAsset;
import com.indracompany.sofia2.config.model.Notebook;
import com.indracompany.sofia2.config.model.Ontology;
import com.indracompany.sofia2.config.model.OntologyCategory;
import com.indracompany.sofia2.config.model.OntologyUserAccessType;
import com.indracompany.sofia2.config.model.PropertyDigitalTwinType;
import com.indracompany.sofia2.config.model.PropertyDigitalTwinType.Direction;
import com.indracompany.sofia2.config.model.Role;
import com.indracompany.sofia2.config.model.Token;
import com.indracompany.sofia2.config.model.User;
import com.indracompany.sofia2.config.model.UserToken;
import com.indracompany.sofia2.config.repository.ClientConnectionRepository;
import com.indracompany.sofia2.config.repository.ClientPlatformOntologyRepository;
import com.indracompany.sofia2.config.repository.ClientPlatformRepository;
import com.indracompany.sofia2.config.repository.ConfigurationRepository;
import com.indracompany.sofia2.config.repository.ConsoleMenuRepository;
import com.indracompany.sofia2.config.repository.DashboardRepository;
import com.indracompany.sofia2.config.repository.DashboardUserAccessTypeRepository;
import com.indracompany.sofia2.config.repository.DataModelRepository;
import com.indracompany.sofia2.config.repository.DeviceSimulationRepository;
import com.indracompany.sofia2.config.repository.DigitalTwinDeviceRepository;
import com.indracompany.sofia2.config.repository.DigitalTwinTypeRepository;
import com.indracompany.sofia2.config.repository.FlowDomainRepository;
import com.indracompany.sofia2.config.repository.GadgetDatasourceRepository;
import com.indracompany.sofia2.config.repository.GadgetMeasureRepository;
import com.indracompany.sofia2.config.repository.GadgetRepository;
import com.indracompany.sofia2.config.repository.MarketAssetRepository;
import com.indracompany.sofia2.config.repository.NotebookRepository;
import com.indracompany.sofia2.config.repository.OntologyCategoryRepository;
import com.indracompany.sofia2.config.repository.OntologyRepository;
import com.indracompany.sofia2.config.repository.OntologyUserAccessRepository;
import com.indracompany.sofia2.config.repository.OntologyUserAccessTypeRepository;
import com.indracompany.sofia2.config.repository.RoleRepository;
import com.indracompany.sofia2.config.repository.TokenRepository;
import com.indracompany.sofia2.config.repository.UserRepository;
import com.indracompany.sofia2.config.repository.UserTokenRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@ConditionalOnProperty(name = "sofia2.init.configdb")
@RunWith(SpringRunner.class)
@SpringBootTest
public class InitConfigDB {

	private static boolean started = false;
	private static User userCollaborator = null;
	private static User userAdministrator = null;
	private static User user = null;
	private static User userAnalytics = null;
	private static User userSysAdmin = null;
	private static User userPartner = null;
	private static User userOperation = null;
	private static Token tokenAdministrator = null;
	private static Ontology ontologyAdministrator = null;
	private static GadgetDatasource gadgetDatasourceDeveloper = null;
	private static Gadget gadgetAdministrator = null;

	@Autowired
	ClientConnectionRepository clientConnectionRepository;
	@Autowired
	ClientPlatformRepository clientPlatformRepository;
	@Autowired
	ClientPlatformOntologyRepository clientPlatformOntologyRepository;
	@Autowired
	ConsoleMenuRepository consoleMenuRepository;
	@Autowired
	DataModelRepository dataModelRepository;
	@Autowired
	DashboardRepository dashboardRepository;
	@Autowired
	GadgetMeasureRepository gadgetMeasureRepository;
	@Autowired
	GadgetDatasourceRepository gadgetDatasourceRepository;
	@Autowired
	GadgetRepository gadgetRepository;
	@Autowired
	OntologyRepository ontologyRepository;
	@Autowired
	OntologyCategoryRepository ontologyCategoryRepository;

	@Autowired
	OntologyUserAccessRepository ontologyUserAccessRepository;
	@Autowired
	OntologyUserAccessTypeRepository ontologyUserAccessTypeRepository;
	@Autowired
	DashboardUserAccessTypeRepository dashboardUserAccessTypeRepository;
	@Autowired
	RoleRepository roleRepository;
	@Autowired
	TokenRepository tokenRepository;
	@Autowired
	UserRepository userCDBRepository;
	@Autowired
	ConfigurationRepository configurationRepository;

	@Autowired
	FlowDomainRepository domainRepository;

	@Autowired
	DigitalTwinTypeRepository digitalTwinTypeRepository;

	@Autowired
	DigitalTwinDeviceRepository digitalTwinDeviceRepository;

	@Autowired
	UserTokenRepository userTokenRepository;

	@Autowired
	MarketAssetRepository marketAssetRepository;

	@Autowired
	NotebookRepository notebookRepository;

	@Autowired
	DeviceSimulationRepository simulationRepository;

	@PostConstruct
	@Test
	public void init() {
		if (!started) {
			started = true;

			log.info("Start initConfigDB...");
			// first we need to create users
			init_RoleUser();
			log.info("OK init_RoleUser");
			init_User();
			log.info("OK init_User");
			//
			init_DataModel();
			log.info("OK init_DataModel");
			init_OntologyCategory();
			log.info("OK init_OntologyCategory");
			init_Ontology();
			log.info("OK init_Ontology");
			init_OntologyUserAccess();
			log.info("OK init_OntologyUserAccess");
			init_OntologyUserAccessType();
			log.info("OK init_OntologyUserAccessType");

			init_OntologyCategory();
			log.info("OK init_OntologyCategory");

			//
			init_ClientPlatform();
			log.info("OK init_ClientPlatform");
			init_ClientPlatformOntology();
			log.info("OK init_ClientPlatformOntology");
			init_ClientConnection();
			log.info("OK init_ClientConnection");
			//
			init_Token();
			log.info("OK init_Token");

			init_UserToken();
			log.info("OK USER_Token");

			init_GadgetDatasource();
			log.info("OK init_GadgetDatasource");
			init_Gadget();
			log.info("OK init_Gadget");
			init_GadgetMeasure();
			log.info("OK init_GadgetMeasure");

			init_Dashboard();
			log.info("OK init_Dashboard");
			init_DashboardUserAccessType();
			log.info("OK init_DashboardUserAccessType");

			init_Menu_ControlPanel();
			log.info("OK init_ConsoleMenu");
			init_Configuration();
			log.info("OK init_Configuration");

			init_FlowDomain();
			log.info("OK init_FlowDomain");

			init_DigitalTwinType();
			log.info("OK init_DigitalTwinType");

			init_DigitalTwinDevice();
			log.info("OK init_DigitalTwinDevice");

			init_market();
			log.info("OK init_Market");

			init_notebook();
			log.info("OK init_Notebook");

			init_simulations();
			log.info("OK init_simulations");
		}

	}

	private void init_simulations() {
		DeviceSimulation simulation = this.simulationRepository.findByIdentification("Issue generator");
		if (simulation == null) {
			simulation = new DeviceSimulation();
			simulation.setActive(false);
			simulation.setCron("0/5 * * ? * * *");
			simulation.setIdentification("Issue generator");
			simulation.setInterval(5);
			simulation.setJson(loadFromResources("simulations/DeviceSimulation_example1.json"));
			simulation.setClientPlatform(this.clientPlatformRepository.findByIdentification("Ticketing App"));
			simulation.setOntology(this.ontologyRepository.findByIdentification("Ticket"));
			simulation.setToken(this.tokenRepository.findByClientPlatform(simulation.getClientPlatform()).get(0));
			simulation.setUser(getUserDeveloper());
			this.simulationRepository.save(simulation);
		}

	}

	private void init_DigitalTwinDevice() {
		log.info("init_DigitalTwinDevice");
		if (this.digitalTwinDeviceRepository.count() == 0) {
			DigitalTwinDevice device = new DigitalTwinDevice();

			// Turbine example

			device.setContextPath("/turbine");
			device.setDigitalKey("f0e50f5f8c754204a4ac601f29775c15");
			device.setIdentification("TurbineHelsinki");
			device.setIntrface("eth0");
			device.setIpv6(false);
			device.setLatitude("60.17688297979675");
			device.setLongitude("24.92333816559176");
			device.setPort(10000);
			device.setUrlSchema("http");
			device.setUrl("https://s4citiespro.westeurope.cloudapp.azure.com/digitaltwinbroker");
			device.setTypeId(this.digitalTwinTypeRepository.findByName("Turbine"));
			device.setUser(getUserDeveloper());
			this.digitalTwinDeviceRepository.save(device);

			// Sensehat example

			device = new DigitalTwinDevice();
			device.setContextPath("/sensehat");
			device.setDigitalKey("f0e50f5f8c754204a4ac601f29775c15");
			device.setIdentification("SensehatHelsinki");
			device.setIntrface("eth0");
			device.setIpv6(false);
			device.setLatitude("60.17688297979675");
			device.setLongitude("24.92333816559176");
			device.setPort(10000);
			device.setUrlSchema("http");
			device.setUrl("https://s4citiespro.westeurope.cloudapp.azure.com/digitaltwinbroker");
			device.setTypeId(this.digitalTwinTypeRepository.findByName("Sensehat"));
			device.setUser(getUserDeveloper());
			this.digitalTwinDeviceRepository.save(device);

		}
	}

	private void init_DigitalTwinType() {
		log.info("init_DigitalTwinType");

		if (this.digitalTwinTypeRepository.count() == 0) {

			// Turbine example

			DigitalTwinType type = new DigitalTwinType();
			type.setName("Turbine");
			type.setType("thing");
			type.setDescription("Wind Turbine for electricity generation");
			type.setJson(
					"{\"title\":\"Turbine\",\"links\":{\"properties\":\"thing/Turbine/properties\",\"actions\":\"thing/Turbine/actions\",\"events\":\"thing/Turbine/events\"},\"description\":\"Wind Turbine for electricity generation\",\"properties\":{\"rotorSpeed\":{\"type\":\"int\",\"units\":\"rpm\",\"direction\":\"out\",\"description\":\"Rotor speed\"},\"maxRotorSpeed\":{\"type\":\"int\",\"units\":\"rpm\",\"direction\":\"in_out\",\"description\":\"Max allowed speed for the rotor\"},\"power\":{\"type\":\"double\",\"units\":\"wat/h\",\"direction\":\"out\",\"description\":\"Current Power generated by the turbine\"},\"alternatorTemp\":{\"type\":\"double\",\"units\":\"celsius\",\"direction\":\"out\",\"description\":\"Temperature of the alternator\"},\"nacelleTemp\":{\"type\":\"double\",\"units\":\"celsius\",\"direction\":\"out\",\"description\":\"Temperature into the nacelle\"},\"windDirection\":{\"type\":\"int\",\"units\":\"degrees\",\"direction\":\"out\",\"description\":\"Wind direction\"}},\"actions\":{\"connectElectricNetwork\":{\"description\":\"Connect the turbine to the electric network to provide power\"},\"disconnectElectricNetwork\":{\"description\":\"Disconnect the turbine to the electric network to prevent problems\"},\"limitRotorSpeed\":{\"description\":\"Limits the rotor speed\"}},\"events\":{\"register\":{\"description\":\"Register the device into the plaform\"},\"updateshadow\":{\"description\":\"Updates the shadow in the plaform\"},\"ping\":{\"description\":\"Ping the platform to keepalive the device\"},\"log\":{\"description\":\"Log information in plaform\"}}}");
			type.setUser(getUserDeveloper());
			type.setLogic(
					"var digitalTwinApi = Java.type('com.indracompany.sofia2.digitaltwin.logic.api.DigitalTwinApi').getInstance();"
							+ System.getProperty("line.separator") + "function init(){"
							+ System.getProperty("line.separator")
							+ "    digitalTwinApi.log('Init TurbineHelsinki shadow');"
							+ System.getProperty("line.separator")
							+ "    digitalTwinApi.setStatusValue('alternatorTemp', 25.0);"
							+ System.getProperty("line.separator")
							+ "    digitalTwinApi.setStatusValue('power', 50000.2);"
							+ System.getProperty("line.separator")
							+ "    digitalTwinApi.setStatusValue('nacelleTemp', 25.9);"
							+ System.getProperty("line.separator")
							+ "    digitalTwinApi.setStatusValue('rotorSpeed', 30);"
							+ System.getProperty("line.separator")
							+ "    digitalTwinApi.setStatusValue('windDirection', 68);"
							+ System.getProperty("line.separator") + "" + System.getProperty("line.separator")
							+ "    digitalTwinApi.sendUpdateShadow();" + System.getProperty("line.separator")
							+ "    digitalTwinApi.log('Send Update Shadow for init function');"
							+ System.getProperty("line.separator") + "}" + System.getProperty("line.separator")
							+ "function main(){" + System.getProperty("line.separator")
							+ "    digitalTwinApi.log('New loop');" + System.getProperty("line.separator")
							+ "    var alternatorTemp = digitalTwinApi.getStatusValue('alternatorTemp');"
							+ System.getProperty("line.separator") + "" + System.getProperty("line.separator")
							+ "    alternatorTemp ++;" + System.getProperty("line.separator")
							+ "    digitalTwinApi.setStatusValue('alternatorTemp', alternatorTemp);"
							+ System.getProperty("line.separator")
							+ "    digitalTwinApi.setStatusValue('power', 50000.2);"
							+ System.getProperty("line.separator")
							+ "    digitalTwinApi.setStatusValue('nacelleTemp', 25.9);"
							+ System.getProperty("line.separator")
							+ "    digitalTwinApi.setStatusValue('rotorSpeed', 30);"
							+ System.getProperty("line.separator")
							+ "    digitalTwinApi.setStatusValue('windDirection', 68);"
							+ System.getProperty("line.separator") + "" + System.getProperty("line.separator")
							+ "    digitalTwinApi.sendUpdateShadow();" + System.getProperty("line.separator")
							+ "    digitalTwinApi.log('Send Update Shadow');" + System.getProperty("line.separator")
							+ " " + System.getProperty("line.separator") + "   if(alternatorTemp>=30){"
							+ System.getProperty("line.separator")
							+ "      digitalTwinApi.sendCustomEvent('tempAlert');"
							+ System.getProperty("line.separator") + "   }" + System.getProperty("line.separator") + "}"
							+ System.getProperty("line.separator") + "" + System.getProperty("line.separator")
							+ "var onActionConnectElectricNetwork=function(data){ }"
							+ System.getProperty("line.separator")
							+ "var onActionDisconnectElectricNetwork=function(data){ }"
							+ System.getProperty("line.separator") + "var onActionLimitRotorSpeed=function(data){ }");

			Set<PropertyDigitalTwinType> properties = createTurbinePropertiesDT(type);
			Set<ActionsDigitalTwinType> actions = createTurbineActionsDT(type);
			Set<EventsDigitalTwinType> events = createTurbineEventsDT(type);

			type.setPropertyDigitalTwinTypes(properties);
			type.setActionDigitalTwinTypes(actions);
			type.setEventDigitalTwinTypes(events);

			this.digitalTwinTypeRepository.save(type);

			// Sensehat example
			type = new DigitalTwinType();
			type.setName("Sensehat");
			type.setType("thing");
			type.setDescription("Raspberry with Sensehat");
			type.setJson(
					"{\"title\": \"Sensehat\",\"links\": {\"properties\": \"thing/Sensehat/properties\",\"actions\": \"thing/Sensehat/actions\",\"events\": \"thing/Sensehat/events\"},\"description\": \"Raspberry - Sensehat\",\"properties\": {\"temperature\": {\"type\": \"double\",\"units\": \"degrees\",\"direction\": \"out\",\"description\": \"Temperature\"},\"humidity\": {\"type\": \"double\",\"units\": \"milibars\",\"direction\": \"out\",\"description\": \"Humidity\"},\"pressure\": {\"type\": \"double\",\"units\": \"%\",\"direction\": \"out\",\"description\": \"Pressure\"}},\"actions\": {\"joystickUp\": {\"description\": \"Joysctick action up\"},\"joystickRight\": {\"description\": \"Joystick action to the right\"},\"joystickDown\": {\"description\": \"Joystick action down\"},\"joystickLeft\": {\"description\": \"Joystick action to the left\"},\"joystickMiddle\": {\"description\": \"Joystick action to the middle\"}},\"events\": {\"ping\": {\"description\": \"Ping\"},\"register\": {\"description\": \"Register\"},\"log\": {\"description\": \"Log information in platform\"},\"joystickEventRigth\": {\"description\": \"Send joystick event to the right\"},\"joystickEventLeft\": {\"description\": \"Send joystick event to the left\"},\"joystickEventUp\": {\"description\": \"Send joystick event up\"},\"joystickEventDown\": {\"description\": \"Send joystick event down\"},\"joystickEventMiddle\": {\"description\": \"Send joystick event to the middle\"},\"updateShadow\": {\"description\": \"Send joystick event to the right\"}}}");
			type.setUser(getUserDeveloper());
			type.setLogic(
					"var digitalTwinApi = Java.type('com.indracompany.sofia2.digitaltwin.logic.api.DigitalTwinApi').getInstance();"
							+ System.getProperty("line.separator")
							+ "var senseHatApi = Java.type('com.indracompany.sofia2.raspberry.sensehat.digitaltwin.api.SenseHatApi').getInstance();"
							+ System.getProperty("line.separator") + "function init(){"
							+ System.getProperty("line.separator")
							+ "   senseHatApi.setJoystickUpListener('joystickEventUp');"
							+ System.getProperty("line.separator")
							+ "   senseHatApi.setJoystickDownListener('joystickEventDown')"
							+ System.getProperty("line.separator")
							+ "   senseHatApi.setJoystickLeftListener('joystickEventLeft');"
							+ System.getProperty("line.separator")
							+ "   senseHatApi.setJoystickRightListener('joystickEventRight');"
							+ System.getProperty("line.separator")
							+ "   senseHatApi.setJoystickMiddleListener('joystickEventMiddle');"
							+ System.getProperty("line.separator")
							+ "   digitalTwinApi.log('Init SenseHatSpain shadow');"
							+ System.getProperty("line.separator") + "   var sensorPress = senseHatApi.getPressure();"
							+ System.getProperty("line.separator") + "   var sensorTemp = senseHatApi.getTemperature();"
							+ System.getProperty("line.separator") + "   var sensorHum = senseHatApi.getHumidity();"
							+ System.getProperty("line.separator")
							+ "   digitalTwinApi.setStatusValue('pressure', sensorPress);"
							+ System.getProperty("line.separator")
							+ "   digitalTwinApi.setStatusValue('temperature', sensorTemp);"
							+ System.getProperty("line.separator")
							+ "    digitalTwinApi.setStatusValue('humidity', sensorHum);"
							+ System.getProperty("line.separator")
							+ "    var temp = digitalTwinApi.getStatusValue('temperature');"
							+ System.getProperty("line.separator")
							+ "    var hum = digitalTwinApi.getStatusValue('humidity');"
							+ System.getProperty("line.separator")
							+ "    var pressure = digitalTwinApi.getStatusValue('pressure');"
							+ System.getProperty("line.separator")
							+ "    digitalTwinApi.log('Temperature: ' + temp + ' - Humidity: ' + hum + ' - Pressure: '+ pressure);"
							+ System.getProperty("line.separator") + "    digitalTwinApi.sendUpdateShadow();"
							+ System.getProperty("line.separator") + "    digitalTwinApi.log('Send Update Shadow');"
							+ System.getProperty("line.separator") + "}" + System.getProperty("line.separator")
							+ "function main(){" + System.getProperty("line.separator")
							+ "   digitalTwinApi.log('New main execution');" + System.getProperty("line.separator")
							+ "   var sensorPress = senseHatApi.getPressure();" + System.getProperty("line.separator")
							+ "   var sensorTemp = senseHatApi.getTemperature();" + System.getProperty("line.separator")
							+ "   var sensorHum = senseHatApi.getHumidity();" + System.getProperty("line.separator")
							+ "   digitalTwinApi.setStatusValue('pressure', sensorPress);"
							+ System.getProperty("line.separator")
							+ "   digitalTwinApi.setStatusValue('temperature', sensorTemp);"
							+ System.getProperty("line.separator")
							+ "    digitalTwinApi.setStatusValue('humidity', sensorHum);"
							+ System.getProperty("line.separator") + "    digitalTwinApi.sendUpdateShadow();"
							+ System.getProperty("line.separator") + "    digitalTwinApi.log('Send Update Shadow');"
							+ System.getProperty("line.separator") + "}" + System.getProperty("line.separator")
							+ "var joystickEventLeft=function(event){" + System.getProperty("line.separator")
							+ "   digitalTwinApi.log('Received joystick event to the left');"
							+ System.getProperty("line.separator")
							+ "   digitalTwinApi.sendCustomEvent('joystickEventLeft');"
							+ System.getProperty("line.separator") + "   senseHatApi.showTextLedMatrix(event);"
							+ System.getProperty("line.separator") + "}" + System.getProperty("line.separator")
							+ "var joystickEventRight=function(event){" + System.getProperty("line.separator")
							+ "   digitalTwinApi.log('Received joystick event to the right');"
							+ System.getProperty("line.separator")
							+ "   digitalTwinApi.sendCustomEvent('joystickEventRight');"
							+ System.getProperty("line.separator") + "   senseHatApi.showTextLedMatrix(event);"
							+ System.getProperty("line.separator") + "}" + System.getProperty("line.separator")
							+ "var joystickEventUp=function(event){" + System.getProperty("line.separator")
							+ "   digitalTwinApi.log('Received joystick event up');"
							+ System.getProperty("line.separator")
							+ "   digitalTwinApi.sendCustomEvent('joystickEventUp');"
							+ System.getProperty("line.separator") + "   senseHatApi.showTextLedMatrix(event);"
							+ System.getProperty("line.separator") + "}" + System.getProperty("line.separator")
							+ "var joystickEventDown=function(event){" + System.getProperty("line.separator")
							+ "   digitalTwinApi.log('Received joystick event down');"
							+ System.getProperty("line.separator")
							+ "   digitalTwinApi.sendCustomEvent('joystickEventDown');"
							+ System.getProperty("line.separator") + "   senseHatApi.showTextLedMatrix(event);"
							+ System.getProperty("line.separator") + "}" + System.getProperty("line.separator")
							+ "var joystickEventMiddle=function(event){" + System.getProperty("line.separator")
							+ "   digitalTwinApi.log('Received joystick event to the middle');"
							+ System.getProperty("line.separator")
							+ "   digitalTwinApi.sendCustomEvent('joystickEventMiddle');"
							+ System.getProperty("line.separator") + "   senseHatApi.showTextLedMatrix(event);"
							+ System.getProperty("line.separator") + "}" + System.getProperty("line.separator")
							+ "var onActionJoystickRight=function(data){" + System.getProperty("line.separator")
							+ "   digitalTwinApi.log('Received joystick action to the right');"
							+ System.getProperty("line.separator") + "   senseHatApi.showTextLedMatrix('Right');"
							+ System.getProperty("line.separator") + "}" + System.getProperty("line.separator")
							+ "function onActionJoystickLeft(data){\r\n"
							+ "   digitalTwinApi.log('Received joystick action to the left');"
							+ System.getProperty("line.separator") + "   senseHatApi.showTextLedMatrix('Left');"
							+ System.getProperty("line.separator") + "}" + System.getProperty("line.separator")
							+ "var onActionJoystickUp=function(data){ " + System.getProperty("line.separator")
							+ "   digitalTwinApi.log('Received joystick action up');"
							+ System.getProperty("line.separator") + "   senseHatApi.showTextLedMatrix('Up');"
							+ System.getProperty("line.separator") + "}" + System.getProperty("line.separator")
							+ "var onActionJoystickDown=function(data){" + System.getProperty("line.separator")
							+ "   digitalTwinApi.log('Received joystick action down');"
							+ System.getProperty("line.separator") + "   senseHatApi.showTextLedMatrix('Down');"
							+ System.getProperty("line.separator") + "}" + System.getProperty("line.separator")
							+ "var onActionJoystickMiddle=function(data){ " + System.getProperty("line.separator")
							+ "   digitalTwinApi.log('Received joystick action to the middle');"
							+ System.getProperty("line.separator") + "   senseHatApi.showTextLedMatrix('Middle');"
							+ System.getProperty("line.separator") + "}");

			Set<PropertyDigitalTwinType> propertiesSensehat = createSensehatPropertiesDT(type);
			Set<ActionsDigitalTwinType> actionsSensehat = createSensehatActionsDT(type);
			Set<EventsDigitalTwinType> eventsSensehat = createSensehatEventsDT(type);

			type.setPropertyDigitalTwinTypes(propertiesSensehat);
			type.setActionDigitalTwinTypes(actionsSensehat);
			type.setEventDigitalTwinTypes(eventsSensehat);

			this.digitalTwinTypeRepository.save(type);
		}
	}

	private Set<EventsDigitalTwinType> createTurbineEventsDT(DigitalTwinType type) {
		Set<EventsDigitalTwinType> events = new HashSet<EventsDigitalTwinType>();
		EventsDigitalTwinType event = new EventsDigitalTwinType();
		event.setName("ping");
		event.setStatus(true);
		event.setType(Type.PING);
		event.setDescription("Ping the platform to keepalive the device");
		event.setTypeId(type);
		events.add(event);

		event = new EventsDigitalTwinType();
		event.setName("updateshadow");
		event.setStatus(true);
		event.setType(Type.UPDATE_SHADOW);
		event.setDescription("Updates the shadow in the plaform");
		event.setTypeId(type);
		events.add(event);

		event = new EventsDigitalTwinType();
		event.setName("log");
		event.setStatus(true);
		event.setType(Type.LOG);
		event.setDescription("Log information in plaform");
		event.setTypeId(type);
		events.add(event);

		event = new EventsDigitalTwinType();
		event.setName("register");
		event.setStatus(true);
		event.setType(Type.REGISTER);
		event.setDescription("Register the device into the plaform");
		event.setTypeId(type);
		events.add(event);

		event = new EventsDigitalTwinType();
		event.setName("tempAlert");
		event.setStatus(true);
		event.setType(Type.OTHER);
		event.setDescription("Send an Alarm when temperature is high.");
		event.setTypeId(type);
		events.add(event);

		return events;
	}

	private Set<ActionsDigitalTwinType> createTurbineActionsDT(DigitalTwinType type) {
		Set<ActionsDigitalTwinType> actions = new HashSet<ActionsDigitalTwinType>();
		ActionsDigitalTwinType action = new ActionsDigitalTwinType();
		action.setName("disconnectElectricNetwork");
		action.setDescription("Disconnect the turbine to the electric network to prevent problems");
		action.setTypeId(type);
		actions.add(action);

		action = new ActionsDigitalTwinType();
		action.setName("connectElectricNetwork");
		action.setDescription("Connect the turbine to the electric network to provide power");
		action.setTypeId(type);
		actions.add(action);

		action = new ActionsDigitalTwinType();
		action.setName("limitRotorSpeed");
		action.setDescription("Limits the rotor speed");
		action.setTypeId(type);
		actions.add(action);

		return actions;
	}

	private Set<PropertyDigitalTwinType> createTurbinePropertiesDT(DigitalTwinType type) {
		Set<PropertyDigitalTwinType> props = new HashSet<PropertyDigitalTwinType>();
		PropertyDigitalTwinType prop = new PropertyDigitalTwinType();
		prop.setName("alternatorTemp");
		prop.setType("double");
		prop.setUnit("celsius");
		prop.setDirection(Direction.OUT);
		prop.setDescription("Temperature of the alternator");
		prop.setTypeId(type);
		props.add(prop);

		prop = new PropertyDigitalTwinType();
		prop.setName("power");
		prop.setType("double");
		prop.setUnit("wat/h");
		prop.setDirection(Direction.OUT);
		prop.setDescription("Current Power generated by the turbine");
		prop.setTypeId(type);
		props.add(prop);

		prop = new PropertyDigitalTwinType();
		prop.setName("nacelleTemp");
		prop.setType("double");
		prop.setUnit("celsius");
		prop.setDirection(Direction.OUT);
		prop.setDescription("Temperature into the nacelle");
		prop.setTypeId(type);
		props.add(prop);

		prop = new PropertyDigitalTwinType();
		prop.setName("rotorSpeed");
		prop.setType("int");
		prop.setUnit("rpm");
		prop.setDirection(Direction.OUT);
		prop.setDescription("Rotor speed");
		prop.setTypeId(type);
		props.add(prop);

		prop = new PropertyDigitalTwinType();
		prop.setName("maxRotorSpeed");
		prop.setType("int");
		prop.setUnit("rpm");
		prop.setDirection(Direction.IN_OUT);
		prop.setDescription("Max allowed speed for the rotor");
		prop.setTypeId(type);
		props.add(prop);

		prop = new PropertyDigitalTwinType();
		prop.setName("windDirection");
		prop.setType("int");
		prop.setUnit("degrees");
		prop.setDirection(Direction.OUT);
		prop.setDescription("Wind direction");
		prop.setTypeId(type);
		props.add(prop);

		return props;
	}

	private Set<EventsDigitalTwinType> createSensehatEventsDT(DigitalTwinType type) {
		Set<EventsDigitalTwinType> events = new HashSet<EventsDigitalTwinType>();
		EventsDigitalTwinType event = new EventsDigitalTwinType();
		event.setName("ping");
		event.setStatus(true);
		event.setType(Type.PING);
		event.setDescription("Ping the platform to keepalive the device");
		event.setTypeId(type);
		events.add(event);

		event = new EventsDigitalTwinType();
		event.setName("updateshadow");
		event.setStatus(true);
		event.setType(Type.UPDATE_SHADOW);
		event.setDescription("Updates the shadow in the plaform");
		event.setTypeId(type);
		events.add(event);

		event = new EventsDigitalTwinType();
		event.setName("log");
		event.setStatus(true);
		event.setType(Type.LOG);
		event.setDescription("Log information in plaform");
		event.setTypeId(type);
		events.add(event);

		event = new EventsDigitalTwinType();
		event.setName("register");
		event.setStatus(true);
		event.setType(Type.REGISTER);
		event.setDescription("Register the device into the plaform");
		event.setTypeId(type);
		events.add(event);

		event = new EventsDigitalTwinType();
		event.setName("joystickEventMiddle");
		event.setStatus(true);
		event.setType(Type.OTHER);
		event.setDescription("Send joystick event to the middle");
		event.setTypeId(type);
		events.add(event);

		event = new EventsDigitalTwinType();
		event.setName("joystickEventRight");
		event.setStatus(true);
		event.setType(Type.OTHER);
		event.setDescription("Send joystick event to the right");
		event.setTypeId(type);
		events.add(event);

		event = new EventsDigitalTwinType();
		event.setName("joystickEventLeft");
		event.setStatus(true);
		event.setType(Type.OTHER);
		event.setDescription("Send joystick event to the left");
		event.setTypeId(type);
		events.add(event);

		event = new EventsDigitalTwinType();
		event.setName("joystickEventUp");
		event.setStatus(true);
		event.setType(Type.OTHER);
		event.setDescription("Send joystick event up");
		event.setTypeId(type);
		events.add(event);

		event = new EventsDigitalTwinType();
		event.setName("joystickEventDown");
		event.setStatus(true);
		event.setType(Type.OTHER);
		event.setDescription("Send joystick event down");
		event.setTypeId(type);
		events.add(event);

		return events;
	}

	private Set<ActionsDigitalTwinType> createSensehatActionsDT(DigitalTwinType type) {
		Set<ActionsDigitalTwinType> actions = new HashSet<ActionsDigitalTwinType>();
		ActionsDigitalTwinType action = new ActionsDigitalTwinType();
		action.setName("joystickUp");
		action.setDescription("Joystick action up");
		action.setTypeId(type);
		actions.add(action);

		action = new ActionsDigitalTwinType();
		action.setName("joystickDown");
		action.setDescription("Joystick action down");
		action.setTypeId(type);
		actions.add(action);

		action = new ActionsDigitalTwinType();
		action.setName("joystickLeft");
		action.setDescription("Joystick action to the left");
		action.setTypeId(type);
		actions.add(action);

		action = new ActionsDigitalTwinType();
		action.setName("joystickRight");
		action.setDescription("Joystick action to the right");
		action.setTypeId(type);
		actions.add(action);

		action = new ActionsDigitalTwinType();
		action.setName("joystickMiddle");
		action.setDescription("Joystick action to the middle");
		action.setTypeId(type);
		actions.add(action);

		return actions;
	}

	private Set<PropertyDigitalTwinType> createSensehatPropertiesDT(DigitalTwinType type) {
		Set<PropertyDigitalTwinType> props = new HashSet<PropertyDigitalTwinType>();
		PropertyDigitalTwinType prop = new PropertyDigitalTwinType();
		prop.setName("temperature");
		prop.setType("double");
		prop.setUnit("degrees");
		prop.setDirection(Direction.OUT);
		prop.setDescription("Temperature");
		prop.setTypeId(type);
		props.add(prop);

		prop = new PropertyDigitalTwinType();
		prop.setName("humidity");
		prop.setType("double");
		prop.setUnit("%");
		prop.setDirection(Direction.OUT);
		prop.setDescription("Humidity");
		prop.setTypeId(type);
		props.add(prop);

		prop = new PropertyDigitalTwinType();
		prop.setName("pressure");
		prop.setType("double");
		prop.setUnit("milibars");
		prop.setDirection(Direction.OUT);
		prop.setDescription("Pressure");
		prop.setTypeId(type);
		props.add(prop);

		return props;
	}

	private void init_FlowDomain() {
		log.info("init_FlowDomain");
		// Domain for administrator
		if (this.domainRepository.count() == 0) {
			FlowDomain domain = new FlowDomain();
			domain.setActive(true);
			domain.setIdentification("adminDomain");
			domain.setUser(userCDBRepository.findByUserId("administrator"));
			domain.setHome("/tmp/administrator");
			domain.setState("START");
			domain.setPort(8000);
			domain.setServicePort(7000);
			domainRepository.save(domain);
			// Domain for developer
			domain = new FlowDomain();
			domain.setActive(true);
			domain.setIdentification("devDomain");
			domain.setUser(userCDBRepository.findByUserId("developer"));
			domain.setHome("/tmp/developer");
			domain.setState("START");
			domain.setPort(8001);
			domain.setServicePort(7001);
			domainRepository.save(domain);
		}
	}

	private void init_Configuration() {
		log.info("init_Configuration");
		if (this.configurationRepository.count() == 0) {

			Configuration config = new Configuration();
			config = new Configuration();
			config.setType(Configuration.Type.TwitterConfiguration);
			config.setUser(getUserAdministrator());
			config.setEnvironment("dev");
			config.setYmlConfig(loadFromResources("TwitterConfiguration.yml"));
			this.configurationRepository.save(config);
			//
			config = new Configuration();
			config.setType(Configuration.Type.TwitterConfiguration);
			config.setUser(getUserAdministrator());
			config.setEnvironment("default");
			config.setSuffix("lmgracia");
			config.setDescription("Twitter");
			config.setYmlConfig(loadFromResources("TwitterConfiguration.yml"));
			this.configurationRepository.save(config);
			//
			config = new Configuration();
			config.setType(Configuration.Type.SchedulingConfiguration);
			config.setUser(getUserAdministrator());
			config.setEnvironment("default");
			config.setDescription("RtdbMaintainer config");
			config.setYmlConfig(loadFromResources("SchedulingConfiguration_default.yml"));
			this.configurationRepository.save(config);
			//

			config = new Configuration();
			config.setType(Configuration.Type.EndpointModulesConfiguration);
			config.setUser(getUserAdministrator());
			config.setEnvironment("default");
			config.setDescription("Endpoints default profile");
			config.setYmlConfig(loadFromResources("EndpointModulesConfigurationDefault.yml"));
			this.configurationRepository.save(config);
			//
			//

			config = new Configuration();
			config.setType(Configuration.Type.EndpointModulesConfiguration);
			config.setUser(getUserAdministrator());
			config.setEnvironment("docker");
			config.setDescription("Endpoints docker profile");
			config.setYmlConfig(loadFromResources("EndpointModulesConfigurationDocker.yml"));
			this.configurationRepository.save(config);
			//

			config = new Configuration();
			config.setType(Configuration.Type.MailConfiguration);
			config.setUser(getUserAdministrator());
			config.setEnvironment("default");
			config.setYmlConfig(loadFromResources("MailConfiguration.yml"));
			this.configurationRepository.save(config);
			//

			config = new Configuration();
			config.setType(Configuration.Type.RTDBConfiguration);
			config.setUser(getUserAdministrator());
			config.setEnvironment("default");
			config.setYmlConfig(loadFromResources("RTDBConfiguration.yml"));
			this.configurationRepository.save(config);
			//

			config = new Configuration();
			config.setType(Configuration.Type.MonitoringConfiguration);
			config.setUser(getUserAdministrator());
			config.setEnvironment("default");
			config.setYmlConfig(loadFromResources("MonitoringConfiguration.yml"));
			this.configurationRepository.save(config);

		}

	}

	public void init_ClientConnection() {
		log.info("init ClientConnection");
		List<ClientConnection> clients = this.clientConnectionRepository.findAll();
		ClientPlatform cp = this.clientPlatformRepository.findAll().get(0);
		if (clients.isEmpty()) {
			log.info("No clients ...");
			ClientConnection con = new ClientConnection();
			//
			con.setClientPlatform(cp);
			con.setIdentification("1");
			con.setIpStrict(true);
			con.setStaticIp(false);
			con.setLastIp("192.168.1.89");
			Calendar date = Calendar.getInstance();
			con.setLastConnection(date);
			con.setClientPlatform(cp);
			clientConnectionRepository.save(con);
		}
	}
	// List<ClientConnection> clients=
	// this.clientConnectionRepository.findAll();
	// if (clients.isEmpty()) {
	// log.info("No clients ...");
	// ClientConnection con= new ClientConnection();
	// ClientPlatform client= new ClientPlatform();
	// client.setId("06be1962-aa27-429c-960c-d8a324eef6d4");
	// clientPlatformRepository.save(client);
	// con.setClientPlatformId(client);
	// con.setIdentification("1");
	// con.setIpStrict(true);
	// con.setStaticIp(false);
	// con.setLastIp("192.168.1.89");
	// Calendar date = Calendar.getInstance();
	// con.setLastConnection(date);
	// clientConnectionRepository.save(con);
	// }

	public void init_ClientPlatformOntology() {

		log.info("init ClientPlatformOntology");
		List<ClientPlatformOntology> cpos = this.clientPlatformOntologyRepository.findAll();
		if (cpos.isEmpty()) {
			if (this.clientPlatformRepository.findAll().isEmpty())
				throw new RuntimeException("There must be at least a ClientPlatform with id=1 created");
			if (this.ontologyRepository.findAll().isEmpty())
				throw new RuntimeException("There must be at least a Ontology with id=1 created");
			log.info("No Client Platform Ontologies");
			ClientPlatformOntology cpo = new ClientPlatformOntology();
			cpo.setClientPlatform(this.clientPlatformRepository.findByIdentification("Ticketing App"));
			cpo.setOntology(this.ontologyRepository.findByIdentification("Ticket"));
			cpo.setAccesEnum(ClientPlatformOntology.AccessType.ALL);
			this.clientPlatformOntologyRepository.save(cpo);
		}
	}

	public void init_ClientPlatform() {
		log.info("init ClientPlatform");
		List<ClientPlatform> clients = this.clientPlatformRepository.findAll();
		if (clients.isEmpty()) {
			log.info("No clients ...");
			ClientPlatform client = new ClientPlatform();
			client.setId("1");
			client.setUser(getUserDeveloper());
			client.setIdentification("Client-MasterData");
			client.setEncryptionKey("b37bf11c-631e-4bc4-ae44-910e58525952");
			client.setDescription("ClientPatform created as MasterData");
			clientPlatformRepository.save(client);
			client = new ClientPlatform();
			client.setId("2");
			client.setUser(getUserDeveloper());
			client.setIdentification("GTKP-Example");
			client.setEncryptionKey("f9dfe72e-7082-4fe8-ba37-3f569b30a691");
			client.setDescription("ClientPatform created as Example");
			clientPlatformRepository.save(client);
			client = new ClientPlatform();
			client.setId("3");
			client.setUser(getUserDeveloper());
			client.setIdentification("Ticketing App");
			client.setEncryptionKey(UUID.randomUUID().toString());
			client.setDescription("Platform client for issues and ticketing");
			clientPlatformRepository.save(client);
			client = new ClientPlatform();
			client.setId("4");
			client.setUser(getUserDeveloper());
			client.setIdentification("Device Master");
			client.setEncryptionKey(UUID.randomUUID().toString());
			client.setDescription("Device template for testing");
			clientPlatformRepository.save(client);
		}

	}

	public void init_Menu_ControlPanel() {
		log.info("init ConsoleMenu");
		List<ConsoleMenu> menus = this.consoleMenuRepository.findAll();

		if (!menus.isEmpty()) {
			this.consoleMenuRepository.deleteAll();
		}

		log.info("No menu elents found...adding");
		try {
			log.info("Adding menu for role ADMIN");
			ConsoleMenu menu = new ConsoleMenu();
			menu.setId("1");
			menu.setJson(loadFromResources("menu/menu_admin.json"));
			menu.setRoleType(roleRepository.findById(Role.Type.ROLE_ADMINISTRATOR.toString()));
			this.consoleMenuRepository.save(menu);
		} catch (Exception e) {
			log.error("Error adding menu for role ADMIN");
		}
		try {
			log.info("Adding menu for role DEVELOPER");
			ConsoleMenu menu = new ConsoleMenu();
			menu.setId("2");
			menu.setJson(loadFromResources("menu/menu_developer.json"));
			menu.setRoleType(roleRepository.findById(Role.Type.ROLE_DEVELOPER.toString()));
			this.consoleMenuRepository.save(menu);
		} catch (Exception e) {
			log.error("Error adding menu for role DEVELOPER");
		}
		try {
			log.info("Adding menu for role USER");
			ConsoleMenu menu = new ConsoleMenu();
			menu.setId("3");
			menu.setJson(loadFromResources("menu/menu_user.json"));
			menu.setRoleType(roleRepository.findById(Role.Type.ROLE_USER.toString()));
			this.consoleMenuRepository.save(menu);
		} catch (Exception e) {
			log.error("Error adding menu for role USER");
		}
		try {
			log.info("Adding menu for role ANALYTIC");
			ConsoleMenu menu = new ConsoleMenu();
			menu.setId("4");
			menu.setJson(loadFromResources("menu/menu_analytic.json"));
			menu.setRoleType(roleRepository.findById(Role.Type.ROLE_DATASCIENTIST.toString()));
			this.consoleMenuRepository.save(menu);
		} catch (Exception e) {
			log.error("Error adding menu for role ANALYTIC");
		}
		try {
			log.info("Adding menu for role DATAVIEWER");
			ConsoleMenu menu = new ConsoleMenu();
			menu.setId("5");
			menu.setJson(loadFromResources("menu/menu_dataviewer.json"));
			menu.setRoleType(roleRepository.findById(Role.Type.ROLE_DATAVIEWER.toString()));
			this.consoleMenuRepository.save(menu);
		} catch (Exception e) {
			log.error("Error adding menu for role DATAVIEWER");
		}
	}

	private String loadFromResources(String name) {
		try {
			return new String(Files.readAllBytes(Paths.get(getClass().getClassLoader().getResource(name).toURI())),
					Charset.forName("UTF-8"));

		} catch (Exception e) {
			try {
				return new String(IOUtils.toString(getClass().getClassLoader().getResourceAsStream(name)).getBytes(),
						Charset.forName("UTF-8"));
			} catch (IOException e1) {
				log.error("**********************************************");
				log.error("Error loading resource: " + name + ".Please check if this error affect your database");
				log.error(e.getMessage());
				return null;
			}
		}
	}

	private byte[] loadFileFromResources(String name) {
		try {
			return Files.readAllBytes((Paths.get(getClass().getClassLoader().getResource(name).toURI())));

		} catch (Exception e) {
			log.error("Error loading resource: " + name + ".Please check if this error affect your database");
			log.error(e.getMessage());
			return null;
		}
	}

	public void init_Dashboard() {
		log.info("init Dashboard");
		List<Dashboard> dashboards = this.dashboardRepository.findAll();
		if (dashboards.isEmpty()) {
			log.info("No dashboards...adding");
			Dashboard dashboard = new Dashboard();
			dashboard.setIdentification("TempDeveloperDashboard");
			dashboard.setDescription("Dashboard analytics restaurants");
			dashboard.setJsoni18n("");
			dashboard.setCustomcss("");
			dashboard.setCustomjs("");
			dashboard.setModel(
					"{\"header\":{\"title\":\"My new s4c Dashboard\",\"enable\":true,\"height\":56,\"logo\":{\"height\":48},\"backgroundColor\":\"hsl(220, 23%, 20%)\",\"textColor\":\"hsl(0, 0%, 100%)\",\"iconColor\":\"hsl(0, 0%, 100%)\",\"pageColor\":\"hsl(0, 0%, 100%)\"},\"navigation\":{\"showBreadcrumbIcon\":true,\"showBreadcrumb\":true},\"pages\":[{\"title\":\"New Page\",\"icon\":\"apps\",\"background\":{\"file\":[]},\"layers\":[{\"gridboard\":[{\"$$hashKey\":\"object:64\"},{\"x\":0,\"y\":0,\"cols\":20,\"rows\":7,\"id\":\""
							+ getGadget().getId()
							+ "\",\"content\":\"bar\",\"type\":\"bar\",\"header\":{\"enable\":true,\"title\":{\"icon\":\"\",\"iconColor\":\"hsl(220, 23%, 20%)\",\"text\":\"My Gadget\",\"textColor\":\"hsl(220, 23%, 20%)\"},\"backgroundColor\":\"hsl(0, 0%, 100%)\",\"height\":\"25\"},\"backgroundColor\":\"white\",\"padding\":0,\"border\":{\"color\":\"#c7c7c7de\",\"width\":1,\"radius\":5},\"$$hashKey\":\"object:107\"}],\"title\":\"baseLayer\",\"$$hashKey\":\"object:23\"}],\"selectedlayer\":0,\"combinelayers\":false,\"$$hashKey\":\"object:4\"}],\"gridOptions\":{\"gridType\":\"fit\",\"compactType\":\"none\",\"margin\":3,\"outerMargin\":true,\"mobileBreakpoint\":640,\"minCols\":20,\"maxCols\":100,\"minRows\":20,\"maxRows\":100,\"maxItemCols\":5000,\"minItemCols\":1,\"maxItemRows\":5000,\"minItemRows\":1,\"maxItemArea\":25000,\"minItemArea\":1,\"defaultItemCols\":4,\"defaultItemRows\":4,\"fixedColWidth\":250,\"fixedRowHeight\":250,\"enableEmptyCellClick\":false,\"enableEmptyCellContextMenu\":false,\"enableEmptyCellDrop\":true,\"enableEmptyCellDrag\":false,\"emptyCellDragMaxCols\":5000,\"emptyCellDragMaxRows\":5000,\"draggable\":{\"delayStart\":100,\"enabled\":true,\"ignoreContent\":true,\"dragHandleClass\":\"drag-handler\"},\"resizable\":{\"delayStart\":0,\"enabled\":true},\"swap\":false,\"pushItems\":true,\"disablePushOnDrag\":false,\"disablePushOnResize\":false,\"pushDirections\":{\"north\":true,\"east\":true,\"south\":true,\"west\":true},\"pushResizeItems\":false,\"displayGrid\":\"none\",\"disableWindowResize\":false,\"disableWarnings\":false,\"scrollToNewItems\":true,\"api\":{}},\"interactionHash\":{\"1\":[],\"livehtml_1526292431685\":[],\"b163b6e4-a8d2-4c3b-b964-5efecf0dd3a0\":[]}}");
			dashboard.setPublic(true);
			dashboard.setUser(getUserDeveloper());

			dashboardRepository.save(dashboard);
		}
	}

	private Gadget getGadget() {
		List<Gadget> gadgets = this.gadgetRepository.findAll();
		return gadgets.get(0);
	}

	private User getUserDeveloper() {
		if (userCollaborator == null)
			userCollaborator = this.userCDBRepository.findByUserId("developer");
		return userCollaborator;
	}

	private User getUserAdministrator() {
		if (userAdministrator == null)
			userAdministrator = this.userCDBRepository.findByUserId("administrator");
		return userAdministrator;
	}

	private User getUser() {
		if (user == null)
			user = this.userCDBRepository.findByUserId("user");
		return user;
	}

	private User getUserAnalytics() {
		if (userAnalytics == null)
			userAnalytics = this.userCDBRepository.findByUserId("analytics");
		return userAnalytics;
	}

	private User getUserPartner() {
		if (userPartner == null)
			userPartner = this.userCDBRepository.findByUserId("partner");
		return userPartner;
	}

	private User getUserSysAdmin() {
		if (userSysAdmin == null)
			userSysAdmin = this.userCDBRepository.findByUserId("sysadmin");
		return userSysAdmin;
	}

	private User getUserOperations() {
		if (userOperation == null)
			userOperation = this.userCDBRepository.findByUserId("operations");
		return userOperation;
	}

	private Token getTokenAdministrator() {
		if (tokenAdministrator == null)
			tokenAdministrator = this.tokenRepository.findByToken("acbca01b-da32-469e-945d-05bb6cd1552e");
		return tokenAdministrator;
	}

	private Ontology getOntologyAdministrator() {
		if (ontologyAdministrator == null)
			ontologyAdministrator = this.ontologyRepository.findByIdentification("OntologyMaster");
		return ontologyAdministrator;
	}

	private GadgetDatasource getGadgetDatasourceDeveloper() {
		if (gadgetDatasourceDeveloper == null)
			gadgetDatasourceDeveloper = this.gadgetDatasourceRepository.findAll().get(0);
		return gadgetDatasourceDeveloper;
	}

	public void init_DataModel() {

		log.info("init DataModel");
		List<DataModel> dataModels = this.dataModelRepository.findAll();
		if (dataModels.isEmpty()) {
			log.info("No DataModels ...");
			DataModel dataModel = new DataModel();
			dataModel.setName("Alarm");
			dataModel.setTypeEnum(DataModel.MainType.General);
			dataModel.setJsonSchema(loadFromResources("datamodels/DataModel_Alarm.json"));
			dataModel.setDescription("Base Alarm: assetId, timestamp, severity, source, details and status..");
			dataModel.setLabels("Alarm,General,IoT");
			dataModel.setUser(getUserAdministrator());
			dataModelRepository.save(dataModel);
			//
			dataModel = new DataModel();
			dataModel.setName("Audit");
			dataModel.setTypeEnum(DataModel.MainType.General);
			dataModel.setJsonSchema(loadFromResources("datamodels/DataModel_Audit.json"));
			dataModel.setDescription("Base Audit");
			dataModel.setLabels("Audit,General,IoT");
			dataModel.setUser(getUserAdministrator());
			dataModelRepository.save(dataModel);
			//
			dataModel = new DataModel();
			dataModel.setName("DeviceLog");
			dataModel.setTypeEnum(DataModel.MainType.IoT);
			dataModel.setJsonSchema(loadFromResources("datamodels/DataModel_DeviceLog.json"));
			dataModel.setDescription("Data model for device logging");
			dataModel.setLabels("General,IoT,Log");
			dataModel.setUser(getUserAdministrator());
			dataModelRepository.save(dataModel);
			//
			dataModel = new DataModel();
			dataModel.setName("Device");
			dataModel.setTypeEnum(DataModel.MainType.IoT);
			dataModel.setJsonSchema(loadFromResources("datamodels/DataModel_Device.json"));
			dataModel.setDescription("Base Device");
			dataModel.setLabels("Audit,General,IoT,Smart Cities");
			dataModel.setUser(getUserAdministrator());
			dataModelRepository.save(dataModel);
			//
			dataModel = new DataModel();
			dataModel.setName("EmptyBase");
			dataModel.setTypeEnum(DataModel.MainType.General);
			dataModel.setJsonSchema(loadFromResources("datamodels/DataModel_EmptyBase.json"));
			dataModel.setDescription("Base DataModel");
			dataModel.setLabels("General,IoT");
			dataModel.setUser(getUserAdministrator());
			dataModelRepository.save(dataModel);
			//
			dataModel = new DataModel();
			dataModel.setName("Feed");
			dataModel.setTypeEnum(DataModel.MainType.IoT);
			dataModel.setJsonSchema(loadFromResources("datamodels/DataModel_Feed.json"));
			dataModel.setDescription("Base Feed");
			dataModel.setLabels("Audit,General,IoT,Smart Cities");
			dataModel.setUser(getUserAdministrator());
			dataModelRepository.save(dataModel);
			//
			dataModel = new DataModel();
			dataModel.setName("Twitter");
			dataModel.setTypeEnum(DataModel.MainType.SocialMedia);
			dataModel.setJsonSchema(loadFromResources("datamodels/DataModel_Twitter.json"));
			dataModel.setDescription("Twitter DataModel");
			dataModel.setLabels("Twitter,Social Media");
			dataModel.setUser(getUserAdministrator());
			dataModelRepository.save(dataModel);
			//
			dataModel = new DataModel();
			dataModel.setName("BasicSensor");
			dataModel.setTypeEnum(DataModel.MainType.IoT);
			dataModel.setJsonSchema(loadFromResources("datamodels/DataModel_BasicSensor.json"));
			dataModel.setDescription("DataModel for sensor sending measures for an assetId");
			dataModel.setLabels("General,IoT,Smart Cities");
			dataModel.setUser(getUserAdministrator());
			dataModelRepository.save(dataModel);
			//
			dataModel = new DataModel();
			dataModel.setName("GSMA-AirQualityObserved");
			dataModel.setTypeEnum(DataModel.MainType.GSMA);
			dataModel.setJsonSchema(loadFromResources("datamodels/DataModel_GSMA-AirQualityObserved.json"));
			dataModel.setDescription("An observation of air quality conditions at a certain place and time");
			dataModel.setLabels("General,IoT,GSMA,Smart Cities");
			dataModel.setUser(getUserAdministrator());
			dataModelRepository.save(dataModel);
			//
			dataModel = new DataModel();
			dataModel.setName("GSMA-AirQualityStation");
			dataModel.setTypeEnum(DataModel.MainType.GSMA);
			dataModel.setJsonSchema(loadFromResources("datamodels/DataModel_GSMA-AirQualityStation.json"));
			dataModel.setDescription("Air Quality Station observing quality conditions at a certain place and time");
			dataModel.setLabels("General,IoT,GSMA,Smart Cities");
			dataModel.setUser(getUserAdministrator());
			dataModelRepository.save(dataModel);
			//
			dataModel = new DataModel();
			dataModel.setName("GSMA-AirQualityThreshold");
			dataModel.setTypeEnum(DataModel.MainType.GSMA);
			dataModel.setJsonSchema(loadFromResources("datamodels/DataModel_GSMA-AirQualityThreshold.json"));
			dataModel.setDescription(
					"Provides the air quality thresholds in Europe. Air quality thresholds allow to calculate an air quality index (AQI).");
			dataModel.setLabels("General,IoT,GSMA,Smart Cities");
			dataModel.setUser(getUserAdministrator());
			dataModelRepository.save(dataModel);
			//
			dataModel = new DataModel();
			dataModel.setName("GSMA-Device");
			dataModel.setTypeEnum(DataModel.MainType.GSMA);
			dataModel.setJsonSchema(loadFromResources("datamodels/DataModel_GSMA-Device.json"));
			dataModel.setDescription(
					"A Device is a tangible object which contains some logic and is producer and/or consumer of data. A Device is always assumed to be capable of communicating electronically via a network.");
			dataModel.setLabels("General,IoT,GSMA,Smart Cities");
			dataModel.setUser(getUserAdministrator());
			dataModelRepository.save(dataModel);
			//
			dataModel = new DataModel();
			dataModel.setName("GSMA-KPI");
			dataModel.setTypeEnum(DataModel.MainType.GSMA);
			dataModel.setJsonSchema(loadFromResources("datamodels/DataModel_GSMA-KPI.json"));
			dataModel.setDescription(
					"Key Performance Indicator (KPI) is a type of performance measurement. KPIs evaluate the success of an organization or of a particular activity in which it engages.");
			dataModel.setLabels("General,IoT,GSMA,Smart Cities");
			dataModel.setUser(getUserAdministrator());
			dataModelRepository.save(dataModel);
			//
			dataModel = new DataModel();
			dataModel.setName("GSMA-OffstreetParking");
			dataModel.setTypeEnum(DataModel.MainType.GSMA);
			dataModel.setJsonSchema(loadFromResources("datamodels/DataModel_GSMA-OffstreetParking.json"));
			dataModel.setDescription(
					"A site, off street, intended to park vehicles, managed independently and with suitable and clearly marked access points (entrances and exits).");
			dataModel.setLabels("General,IoT,Smart Cities");
			dataModel.setUser(getUserAdministrator());
			dataModelRepository.save(dataModel);
			//
			dataModel = new DataModel();
			dataModel.setName("GSMA-Road");
			dataModel.setTypeEnum(DataModel.MainType.GSMA);
			dataModel.setJsonSchema(loadFromResources("datamodels/DataModel_GSMA-Road.json"));
			dataModel.setDescription("Contains a harmonised geographic and contextual description of a road.");
			dataModel.setLabels("General,IoT,Smart Cities");
			dataModel.setUser(getUserAdministrator());
			dataModelRepository.save(dataModel);
			//
			dataModel = new DataModel();
			dataModel.setName("GSMA-StreetLight");
			dataModel.setTypeEnum(DataModel.MainType.GSMA);
			dataModel.setJsonSchema(loadFromResources("datamodels/DataModel_GSMA-StreetLight.json"));
			dataModel.setDescription("GSMA Model that represents an urban streetlight");
			dataModel.setLabels("General,IoT,Smart Cities");
			dataModel.setUser(getUserAdministrator());
			dataModelRepository.save(dataModel);
			//
			//
			dataModel = new DataModel();
			dataModel.setName("GSMA-Vehicle");
			dataModel.setTypeEnum(DataModel.MainType.GSMA);
			dataModel.setJsonSchema(loadFromResources("datamodels/DataModel_GSMA-Vehicle.json"));
			dataModel.setDescription("A harmonised description of a Vehicle");
			dataModel.setLabels("General,IoT,Smart Cities");
			dataModel.setUser(getUserAdministrator());
			dataModelRepository.save(dataModel);
			//
			dataModel = new DataModel();
			dataModel.setName("GSMA-WasteContainer");
			dataModel.setTypeEnum(DataModel.MainType.GSMA);
			dataModel.setJsonSchema(loadFromResources("datamodels/DataModel_GSMA-WasteContainer.json"));
			dataModel.setDescription("GSMA WasteContainer");
			dataModel.setLabels("General,IoT,Smart Cities");
			dataModel.setUser(getUserAdministrator());
			dataModelRepository.save(dataModel);
			//
			dataModel = new DataModel();
			dataModel.setName("GSMA-WeatherObserved");
			dataModel.setTypeEnum(DataModel.MainType.GSMA);
			dataModel.setJsonSchema(loadFromResources("datamodels/DataModel_GSMA-WeatherObserved.json"));
			dataModel.setDescription("An observation of weather conditions at a certain place and time.");
			dataModel.setLabels("General,IoT,Smart Cities");
			dataModel.setUser(getUserAdministrator());
			dataModelRepository.save(dataModel);
			//
			dataModel = new DataModel();
			dataModel.setName("GSMA-WeatherStation");
			dataModel.setTypeEnum(DataModel.MainType.GSMA);
			dataModel.setJsonSchema(loadFromResources("datamodels/DataModel_GSMA-WeatherStation.json"));
			dataModel.setDescription("GSMA Weather Station Model");
			dataModel.setLabels("General,IoT,Smart Cities");
			dataModel.setUser(getUserAdministrator());
			dataModelRepository.save(dataModel);
			//
			dataModel = new DataModel();
			dataModel.setName("Request");
			dataModel.setTypeEnum(DataModel.MainType.General);
			dataModel.setJsonSchema(loadFromResources("datamodels/DataModel_Request.json"));
			dataModel.setDescription("Request for something.");
			dataModel.setLabels("General,IoT");
			dataModel.setUser(getUserAdministrator());
			dataModelRepository.save(dataModel);
			//
			dataModel = new DataModel();
			dataModel.setName("Response");
			dataModel.setTypeEnum(DataModel.MainType.General);
			dataModel.setJsonSchema(loadFromResources("datamodels/DataModel_Response.json"));
			dataModel.setDescription("Response for a request.");
			dataModel.setLabels("General,IoT");
			dataModel.setUser(getUserAdministrator());
			dataModelRepository.save(dataModel);
			//
			dataModel = new DataModel();
			dataModel.setName("MobileElement");
			dataModel.setTypeEnum(DataModel.MainType.IoT);
			dataModel.setJsonSchema(loadFromResources("datamodels/DataModel_MobileElement.json"));
			dataModel.setDescription("Generic Mobile Element representation.");
			dataModel.setLabels("General,IoT");
			dataModel.setUser(getUserAdministrator());
			dataModelRepository.save(dataModel);
			//
			dataModel = new DataModel();
			dataModel.setName("Log");
			dataModel.setTypeEnum(DataModel.MainType.General);
			dataModel.setJsonSchema(loadFromResources("datamodels/DataModel_Log.json"));
			dataModel.setDescription("Log representation.");
			dataModel.setLabels("General,IoT");
			dataModel.setUser(getUserAdministrator());
			dataModelRepository.save(dataModel);
			//
			dataModel = new DataModel();
			dataModel.setName("Issue");
			dataModel.setTypeEnum(DataModel.MainType.General);
			dataModel.setJsonSchema(loadFromResources("datamodels/DataModel_Issue.json"));
			dataModel.setDescription("Issue representation.");
			dataModel.setLabels("General,IoT");
			dataModel.setUser(getUserAdministrator());
			dataModelRepository.save(dataModel);
		}
	}

	public void init_Gadget() {
		log.info("init Gadget");
		List<Gadget> gadgets = this.gadgetRepository.findAll();
		if (gadgets.isEmpty()) {
			log.info("No gadgets ...");
			Gadget gadget = new Gadget();
			gadget.setIdentification("My Gadget");
			gadget.setPublic(false);
			gadget.setDescription("gadget cousin score");
			gadget.setType("bar");
			gadget.setConfig(
					"{\"scales\":{\"yAxes\":[{\"id\":\"#0\",\"display\":true,\"type\":\"linear\",\"position\":\"left\",\"scaleLabel\":{\"labelString\":\"\",\"display\":true}}]}}");
			gadget.setUser(getUserDeveloper());
			gadgetRepository.save(gadget);
		}
	}

	public void init_GadgetDatasource() {

		log.info("init GadgetDatasource");
		List<GadgetDatasource> gadgetDatasource = this.gadgetDatasourceRepository.findAll();
		if (gadgetDatasource.isEmpty()) {
			log.info("No gadget querys ...");
			GadgetDatasource gadgetDatasources = new GadgetDatasource();
			gadgetDatasources.setIdentification("DsRawRestaurants");
			gadgetDatasources.setMode("query");
			gadgetDatasources.setQuery("select * from Restaurants");
			gadgetDatasources.setDbtype("RTDB");
			gadgetDatasources.setRefresh(0);
			gadgetDatasources.setOntology(null);
			gadgetDatasources.setMaxvalues(150);
			gadgetDatasources.setConfig("[]");
			gadgetDatasources.setUser(getUserDeveloper());
			gadgetDatasourceRepository.save(gadgetDatasources);
		}

	}

	public void init_GadgetMeasure() {

		log.info("init GadgetMeasure");
		List<GadgetMeasure> gadgetMeasures = this.gadgetMeasureRepository.findAll();
		if (gadgetMeasures.isEmpty()) {
			log.info("No gadget measures ...");
			GadgetMeasure gadgetMeasure = new GadgetMeasure();
			gadgetMeasure.setDatasource(getGadgetDatasourceDeveloper());
			gadgetMeasure.setConfig(
					"{\"fields\":[\"cuisine\",\"grades[0].score\"],\"name\":\"score\",\"config\":{\"backgroundColor\":\"#000000\",\"borderColor\":\"#000000\",\"pointBackgroundColor\":\"#000000\",\"yAxisID\":\"#0\"}}");
			gadgetMeasure.setGadget(getGadget());
			gadgetMeasureRepository.save(gadgetMeasure);
		}

	}

	public void init_OntologyCategory() {

		log.info("init OntologyCategory");
		List<OntologyCategory> categories = this.ontologyCategoryRepository.findAll();
		if (categories.isEmpty()) {
			log.info("No ontology categories found..adding");
			OntologyCategory category = new OntologyCategory();
			category.setId(1);
			category.setIdentificator("ontologias_categoria_cultura");
			category.setDescription("ontologias_categoria_cultura_desc");
			this.ontologyCategoryRepository.save(category);
		}

	}

	public void init_Ontology() {

		log.info("init Ontology");
		List<Ontology> ontologies = this.ontologyRepository.findAll();
		List<DataModel> dataModels;

		log.info("No ontologies..adding");
		Ontology ontology = new Ontology();
		if (this.ontologyRepository.findByIdentification("OntologyMaster") == null) {
			ontology.setId("1");
			ontology.setJsonSchema("{}");
			ontology.setIdentification("OntologyMaster");
			ontology.setDescription("Ontology created as Master Data");
			ontology.setActive(true);
			ontology.setRtdbClean(true);
			ontology.setRtdbToHdb(true);
			ontology.setPublic(true);
			ontology.setUser(getUserDeveloper());
			ontology.setAllowsCypherFields(false);
			ontologyRepository.save(ontology);
		}
		if (this.ontologyRepository.findByIdentification("Ticket") == null) {
			ontology = new Ontology();
			ontology.setJsonSchema(loadFromResources("examples/OntologySchema_Ticket.json"));
			ontology.setDescription("Ontology created for Ticketing");
			ontology.setIdentification("Ticket");
			ontology.setActive(true);
			ontology.setRtdbClean(true);
			ontology.setRtdbToHdb(true);
			ontology.setPublic(true);
			ontology.setDataModel(this.dataModelRepository.findByName("EmptyBase").get(0));
			ontology.setUser(getUserDeveloper());
			ontology.setAllowsCypherFields(false);
			ontologyRepository.save(ontology);
		}
		if (this.ontologyRepository.findByIdentification("ContPerf") == null) {
			ontology = new Ontology();
			ontology.setJsonSchema(loadFromResources("examples/OntologySchema_ContPerf.json"));
			ontology.setDescription("Ontology created for performance testing");
			ontology.setIdentification("ContPerf");
			ontology.setActive(true);
			ontology.setRtdbClean(false);
			ontology.setRtdbToHdb(false);
			ontology.setPublic(true);
			ontology.setDataModel(this.dataModelRepository.findByName("EmptyBase").get(0));
			ontology.setUser(getUserAdministrator());
			ontology.setAllowsCypherFields(false);
			ontologyRepository.save(ontology);
		}
		if (this.ontologyRepository.findByIdentification("HelsinkiPopulation") == null) {
			ontology = new Ontology();
			ontology.setJsonSchema(loadFromResources("examples/OntologySchema_HelsinkiPopulation.json"));
			ontology.setDescription("Ontology HelsinkiPopulation for testing");
			ontology.setIdentification("HelsinkiPopulation");
			ontology.setActive(true);
			ontology.setRtdbClean(false);
			ontology.setRtdbToHdb(false);
			ontology.setPublic(true);
			ontology.setUser(getUserDeveloper());
			ontology.setAllowsCypherFields(false);

			dataModels = dataModelRepository.findByName("EmptyBase");
			if (!dataModels.isEmpty()) {
				ontology.setDataModel(dataModels.get(0));
				ontologyRepository.save(ontology);
			}

		}
		if (this.ontologyRepository.findByIdentification("TweetSentiment") == null) {
			ontology = new Ontology();
			ontology.setJsonSchema(loadFromResources("examples/OntologySchema_TweetSentiment.json"));
			ontology.setDescription("TweetSentiment");
			ontology.setIdentification("TweetSentiment");
			ontology.setActive(true);
			ontology.setRtdbClean(false);
			ontology.setRtdbToHdb(false);
			ontology.setPublic(true);
			ontology.setUser(getUserDeveloper());
			ontology.setAllowsCypherFields(false);

			dataModels = dataModelRepository.findByName("EmptyBase");
			if (!dataModels.isEmpty()) {
				ontology.setDataModel(dataModels.get(0));
				ontologyRepository.save(ontology);
			}
		}
		if (this.ontologyRepository.findByIdentification("GeoAirQuality") == null) {
			ontology = new Ontology();
			ontology.setJsonSchema(loadFromResources("examples/OntologySchema_GeoAirQuality.json"));
			ontology.setDescription("Air quality retrieved from https://api.waqi.info/search");
			ontology.setIdentification("GeoAirQuality");
			ontology.setActive(true);
			ontology.setRtdbClean(false);
			ontology.setRtdbToHdb(false);
			ontology.setPublic(true);
			ontology.setUser(getUserDeveloper());
			ontology.setAllowsCypherFields(false);

			dataModels = dataModelRepository.findByName("EmptyBase");
			if (!dataModels.isEmpty()) {
				ontology.setDataModel(dataModels.get(0));
				ontologyRepository.save(ontology);
			}
		}
		if (this.ontologyRepository.findByIdentification("CityPopulation") == null) {
			ontology = new Ontology();
			ontology.setJsonSchema(loadFromResources("examples/OntologySchema_CityPopulation.json"));
			ontology.setDescription(
					"Population of Urban Agglomerations with 300,000 Inhabitants or More in 2014, by Country, 1950-2030 (thousands)");
			ontology.setIdentification("CityPopulation");
			ontology.setActive(true);
			ontology.setRtdbClean(false);
			ontology.setRtdbToHdb(false);
			ontology.setPublic(true);
			ontology.setUser(getUserDeveloper());
			ontology.setAllowsCypherFields(false);

			dataModels = dataModelRepository.findByName("EmptyBase");
			if (!dataModels.isEmpty()) {
				ontology.setDataModel(dataModels.get(0));
				ontologyRepository.save(ontology);
			}
		}
		if (this.ontologyRepository.findByIdentification("AirQuality_gr2") == null) {
			ontology = new Ontology();
			ontology.setJsonSchema(loadFromResources("examples/OntologySchema_AirQuality_gr2.json"));
			ontology.setDescription("AirQuality_gr2");
			ontology.setIdentification("AirQuality_gr2");
			ontology.setActive(true);
			ontology.setRtdbClean(false);
			ontology.setRtdbToHdb(false);
			ontology.setPublic(true);
			ontology.setUser(getUserDeveloper());
			ontology.setAllowsCypherFields(false);

			dataModels = dataModelRepository.findByName("EmptyBase");
			if (!dataModels.isEmpty()) {
				ontology.setDataModel(dataModels.get(0));
				ontologyRepository.save(ontology);
			}
		}
		if (this.ontologyRepository.findByIdentification("AirQuality") == null) {
			ontology = new Ontology();
			ontology.setJsonSchema(loadFromResources("examples/OntologySchema_AirQuality.json"));
			ontology.setDescription("AirQuality");
			ontology.setIdentification("AirQuality");
			ontology.setActive(true);
			ontology.setRtdbClean(false);
			ontology.setRtdbToHdb(false);
			ontology.setPublic(true);
			ontology.setUser(getUserDeveloper());
			ontology.setAllowsCypherFields(false);

			dataModels = dataModelRepository.findByName("EmptyBase");
			if (!dataModels.isEmpty()) {
				ontology.setDataModel(dataModels.get(0));
				ontologyRepository.save(ontology);
			}
		}
		if (this.ontologyRepository.findByIdentification("AirCOMeter") == null) {
			ontology = new Ontology();
			ontology.setJsonSchema(loadFromResources("examples/OntologySchema_AirCOMeter.json"));
			ontology.setDescription("AirCOMeter");
			ontology.setIdentification("AirCOMeter");
			ontology.setActive(true);
			ontology.setRtdbClean(false);
			ontology.setRtdbToHdb(false);
			ontology.setPublic(true);
			ontology.setUser(getUserDeveloper());
			ontology.setAllowsCypherFields(false);

			dataModels = dataModelRepository.findByName("EmptyBase");
			if (!dataModels.isEmpty()) {
				ontology.setDataModel(dataModels.get(0));
				ontologyRepository.save(ontology);
			}
		}
	}

	public void init_OntologyUserAccess() {
		log.info("init OntologyUserAccess");
		/*
		 * List<OntologyUserAccess> users=this.ontologyUserAccessRepository.findAll();
		 * if(users.isEmpty()) { log.info("No users found...adding"); OntologyUserAccess
		 * user=new OntologyUserAccess(); user.setUser("6");
		 * user.setOntology(ontologyRepository.findAll().get(0));
		 * user.setOntologyUserAccessTypeId(ontologyUserAccessTypeId);
		 * this.ontologyUserAccessRepository.save(user); }
		 */
	}

	public void init_OntologyUserAccessType() {

		log.info("init OntologyUserAccessType");
		List<OntologyUserAccessType> types = this.ontologyUserAccessTypeRepository.findAll();
		if (types.isEmpty()) {
			log.info("No user access types found...adding");
			OntologyUserAccessType type = new OntologyUserAccessType();
			type.setId(1);
			type.setName("ALL");
			type.setDescription("Todos los permisos");
			this.ontologyUserAccessTypeRepository.save(type);
			type = new OntologyUserAccessType();
			type.setId(2);
			type.setName("QUERY");
			type.setDescription("Todos los permisos");
			this.ontologyUserAccessTypeRepository.save(type);
			type = new OntologyUserAccessType();
			type.setId(3);
			type.setName("INSERT");
			type.setDescription("Todos los permisos");
			this.ontologyUserAccessTypeRepository.save(type);
		}

	}

	public void init_DashboardUserAccessType() {

		log.info("init DashboardUserAccessType");
		List<DashboardUserAccessType> types = this.dashboardUserAccessTypeRepository.findAll();
		if (types.isEmpty()) {
			log.info("No user access types found...adding");
			DashboardUserAccessType type = new DashboardUserAccessType();
			type.setId(1);
			type.setName("EDIT");
			type.setDescription("view and edit access");
			this.dashboardUserAccessTypeRepository.save(type);
			type = new DashboardUserAccessType();
			type.setId(2);
			type.setName("VIEW");
			type.setDescription("view access");
			this.dashboardUserAccessTypeRepository.save(type);

		}

	}

	public void init_RoleUser() {
		log.info("init init_RoleUser");
		List<Role> types = this.roleRepository.findAll();
		if (types.isEmpty()) {
			try {

				log.info("No roles en tabla.Adding...");
				Role type = new Role();
				type.setIdEnum(Role.Type.ROLE_ADMINISTRATOR);
				type.setName("Administrator");
				type.setDescription("Administrator of the Platform");
				roleRepository.save(type);
				//
				type = new Role();
				type.setIdEnum(Role.Type.ROLE_DEVELOPER);
				type.setName("Developer");
				type.setDescription("Advanced User of the Platform");
				roleRepository.save(type);
				//
				type = new Role();
				type.setIdEnum(Role.Type.ROLE_USER);
				type.setName("User");
				type.setDescription("Basic User of the Platform");
				roleRepository.save(type);
				//
				type = new Role();
				type.setIdEnum(Role.Type.ROLE_DATASCIENTIST);
				type.setName("Analytics");
				type.setDescription("Analytics User of the Platform");
				roleRepository.save(type);
				//
				type = new Role();
				type.setIdEnum(Role.Type.ROLE_PARTNER);
				type.setName("Partner");
				type.setDescription("Partner in the Platform");
				roleRepository.save(type);
				//
				//
				type = new Role();
				type.setIdEnum(Role.Type.ROLE_SYS_ADMIN);
				type.setName("SysAdmin");
				type.setDescription("System Administradot of the Platform");
				roleRepository.save(type);
				//
				type = new Role();
				type.setIdEnum(Role.Type.ROLE_OPERATIONS);
				type.setName("Operations");
				type.setDescription("Operations for the Platform");
				roleRepository.save(type);
				//
				type = new Role();
				type.setIdEnum(Role.Type.ROLE_DEVOPS);
				type.setName("DevOps");
				type.setDescription("DevOps for the Platform");
				roleRepository.save(type);
				//
				// UPDATE of the ROLE_ANALYTICS
				Role typeSon = roleRepository.findById(Role.Type.ROLE_DATASCIENTIST.toString());
				Role typeParent = roleRepository.findById(Role.Type.ROLE_DEVELOPER.toString());
				typeSon.setRoleParent(typeParent);
				roleRepository.save(typeSon);

				type = new Role();
				type.setIdEnum(Role.Type.ROLE_DATAVIEWER);
				type.setName("DataViewer");
				type.setDescription("DataViewer User of the Platform");
				roleRepository.save(type);

			} catch (Exception e) {
				log.error("Error initRoleType:" + e.getMessage());
				roleRepository.deleteAll();
				throw new RuntimeException("Error creating Roles...Stopping");
			}

		}
	}

	public void init_Token() {

		log.info("init token");
		List<Token> tokens = this.tokenRepository.findAll();
		if (tokens.isEmpty()) {
			log.info("No Tokens, adding ...");
			if (this.clientPlatformRepository.findAll().isEmpty())
				throw new RuntimeException("You need to create ClientPlatform before Token");

			ClientPlatform client = this.clientPlatformRepository.findByIdentification("Ticketing App");
			Set<Token> hashSetTokens = new HashSet<Token>();

			Token token = new Token();
			token.setClientPlatform(client);
			token.setToken("e7ef0742d09d4de5a3687f0cfdf7f626");
			token.setActive(true);
			hashSetTokens.add(token);
			client.setTokens(hashSetTokens);
			tokenRepository.save(token);

			client = this.clientPlatformRepository.findByIdentification("Device Master");
			token = new Token();
			token.setClientPlatform(client);
			token.setToken("a16b9e7367734f04bc720e981fcf483f");
			tokenRepository.save(token);
		}

	}

	public void init_UserToken() {

		log.info("init user token");
		List<UserToken> tokens = this.userTokenRepository.findAll();
		if (tokens.isEmpty()) {
			List<User> userList = this.userCDBRepository.findAll();

			for (Iterator<User> iterator = userList.iterator(); iterator.hasNext();) {
				User user = (User) iterator.next();

				UserToken userToken = new UserToken();

				userToken.setToken(UUID.randomUUID().toString().replaceAll("-", ""));
				userToken.setUser(user);
				userToken.setCreatedAt(Calendar.getInstance().getTime());

				try {
					userTokenRepository.save(userToken);
				} catch (Exception e) {
					log.info("Could not create user token for user " + user.getUserId());
				}
			}
		}
	}

	public void init_User() {
		log.info("init UserCDB");
		List<User> types = this.userCDBRepository.findAll();
		User type = null;
		if (types.isEmpty()) {
			try {
				log.info("No types en tabla.Adding...");
				type = new User();
				type.setUserId("administrator");
				type.setPassword("changeIt!");
				type.setFullName("Generic Administrator of the Platform");
				type.setEmail("administrator@sofia2.com");
				type.setActive(true);
				type.setRole(this.roleRepository.findById(Role.Type.ROLE_ADMINISTRATOR.toString()));

				userCDBRepository.save(type);
				//
				type = new User();
				type.setUserId("developer");
				type.setPassword("changeIt!");
				type.setFullName("Developer of the Platform");
				type.setEmail("developer@sofia2.com");
				type.setActive(true);
				type.setRole(this.roleRepository.findById(Role.Type.ROLE_DEVELOPER.toString()));
				userCDBRepository.save(type);
				//
				type = new User();
				type.setUserId("demo_developer");
				type.setPassword("changeIt!");
				type.setFullName("Demo Developer of the Platform");
				type.setEmail("demo_developer@sofia2.com");
				type.setActive(true);
				type.setRole(this.roleRepository.findById(Role.Type.ROLE_DEVELOPER.toString()));
				userCDBRepository.save(type);
				//
				type = new User();
				type.setUserId("user");
				type.setPassword("changeIt!");
				type.setFullName("Generic User of the Platform");
				type.setEmail("user@sofia2.com");
				type.setActive(true);
				type.setRole(this.roleRepository.findById(Role.Type.ROLE_USER.toString()));
				userCDBRepository.save(type);
				//
				type = new User();
				type.setUserId("demo_user");
				type.setPassword("changeIt!");
				type.setFullName("Demo User of the Platform");
				type.setEmail("demo_user@sofia2.com");
				type.setActive(true);
				type.setRole(this.roleRepository.findById(Role.Type.ROLE_USER.toString()));
				userCDBRepository.save(type);
				//
				type = new User();
				type.setUserId("analytics");
				type.setPassword("changeIt!");
				type.setFullName("Generic Analytics User of the Platform");
				type.setEmail("analytics@sofia2.com");
				type.setActive(true);
				type.setRole(this.roleRepository.findById(Role.Type.ROLE_DATASCIENTIST.toString()));

				userCDBRepository.save(type);
				//
				type = new User();
				type.setUserId("partner");
				type.setPassword("changeIt!");
				type.setFullName("Generic Partner of the Platform");
				type.setEmail("partner@sofia2.com");
				type.setActive(true);
				type.setRole(this.roleRepository.findById(Role.Type.ROLE_PARTNER.toString()));

				userCDBRepository.save(type);
				//
				type = new User();
				type.setUserId("sysadmin");
				type.setPassword("changeIt!");
				type.setFullName("Generic SysAdmin of the Platform");
				type.setEmail("sysadmin@sofia2.com");
				type.setActive(true);
				type.setRole(this.roleRepository.findById(Role.Type.ROLE_SYS_ADMIN.toString()));

				userCDBRepository.save(type);
				//
				type = new User();
				type.setUserId("operations");
				type.setPassword("changeIt!");
				type.setFullName("Operations of the Platform");
				type.setEmail("operations@sofia2.com");
				type.setActive(true);
				type.setRole(this.roleRepository.findById(Role.Type.ROLE_OPERATIONS.toString()));
				userCDBRepository.save(type);
				//
				type = new User();
				type.setUserId("dataviewer");
				type.setPassword("changeIt!");
				type.setFullName("DataViewer User of the Platform");
				type.setEmail("dataviewer@sofia2.com");
				type.setActive(true);
				type.setRole(this.roleRepository.findById(Role.Type.ROLE_DATAVIEWER.toString()));
				userCDBRepository.save(type);
				//
			} catch (Exception e) {
				log.error("Error UserCDB:" + e.getMessage());
				userCDBRepository.deleteAll();
				throw new RuntimeException("Error creating users...ignoring creation rest of Tables");
			}
		}
	}

	public void init_market() {
		log.info("init MarketPlace");
		List<MarketAsset> marketAssets = this.marketAssetRepository.findAll();
		if (marketAssets.isEmpty()) {
			log.info("No market Assets...adding");
			MarketAsset marketAsset = new MarketAsset();

			// Getting Started Guide

			marketAsset = new MarketAsset();

			marketAsset.setId("1");
			marketAsset.setIdentification("GettingStartedGuide");

			marketAsset.setUser(getUserAdministrator());

			marketAsset.setPublic(true);
			marketAsset.setState(MarketAsset.MarketAssetState.APPROVED);
			marketAsset.setMarketAssetType(MarketAsset.MarketAssetType.DOCUMENT);
			marketAsset.setPaymentMode(MarketAsset.MarketAssetPaymentMode.FREE);

			marketAsset.setJsonDesc(loadFromResources("market/details/GettingStartedGuide.json"));

			marketAsset.setImage(loadFileFromResources("market/img/select4cities.jpg"));
			marketAsset.setImageType("jpg");

			marketAssetRepository.save(marketAsset);

			// Sofia4Cities Architecture

			marketAsset = new MarketAsset();

			marketAsset.setId("2");
			marketAsset.setIdentification("Sofia4CitiesArchitecture");

			marketAsset.setUser(getUserAdministrator());

			marketAsset.setPublic(true);
			marketAsset.setState(MarketAsset.MarketAssetState.APPROVED);
			marketAsset.setMarketAssetType(MarketAsset.MarketAssetType.DOCUMENT);
			marketAsset.setPaymentMode(MarketAsset.MarketAssetPaymentMode.FREE);

			marketAsset.setJsonDesc(loadFromResources("market/details/Sofia4CitiesArchitecture.json"));

			marketAsset.setImage(loadFileFromResources("market/img/select4cities.jpg"));
			marketAsset.setImageType("jpg");

			marketAssetRepository.save(marketAsset);

			// SOFIA4CITIES WITH DOCKER

			marketAsset = new MarketAsset();

			marketAsset.setId("3");
			marketAsset.setIdentification("Sofia4CitiesWithDocker");

			marketAsset.setUser(getUserAdministrator());

			marketAsset.setPublic(true);
			marketAsset.setState(MarketAsset.MarketAssetState.APPROVED);
			marketAsset.setMarketAssetType(MarketAsset.MarketAssetType.DOCUMENT);
			marketAsset.setPaymentMode(MarketAsset.MarketAssetPaymentMode.FREE);

			marketAsset.setJsonDesc(loadFromResources("market/details/Sofia4CitiesWithDocker.json"));

			marketAsset.setImage(loadFileFromResources("market/img/docker.png"));
			marketAsset.setImageType("png");

			marketAssetRepository.save(marketAsset);

			// API JAVA

			marketAsset = new MarketAsset();

			marketAsset.setId("4");
			marketAsset.setIdentification("API JAVA");

			marketAsset.setUser(getUserAdministrator());

			marketAsset.setPublic(true);
			marketAsset.setState(MarketAsset.MarketAssetState.APPROVED);
			marketAsset.setMarketAssetType(MarketAsset.MarketAssetType.APPLICATION);
			marketAsset.setPaymentMode(MarketAsset.MarketAssetPaymentMode.FREE);

			marketAsset.setJsonDesc(loadFromResources("market/details/JavaAPI.json"));

			marketAsset.setImage(loadFileFromResources("market/img/jar-file.jpg"));
			marketAsset.setImageType("jpg");

			marketAsset.setContent(loadFileFromResources("market/docs/java-client.zip"));
			marketAsset.setContentId("java-client.zip");

			marketAssetRepository.save(marketAsset);

			// DIGITAL TWIN

			marketAsset = new MarketAsset();

			marketAsset.setId("5");
			marketAsset.setIdentification("DIGITAL TWIN EXAMPLE");

			marketAsset.setUser(getUserAdministrator());

			marketAsset.setPublic(true);
			marketAsset.setState(MarketAsset.MarketAssetState.APPROVED);
			marketAsset.setMarketAssetType(MarketAsset.MarketAssetType.APPLICATION);
			marketAsset.setPaymentMode(MarketAsset.MarketAssetPaymentMode.FREE);

			marketAsset.setJsonDesc(loadFromResources("market/details/DigitalTwin.json"));

			marketAsset.setImage(loadFileFromResources("market/img/gears.png"));
			marketAsset.setImageType("png");

			marketAsset.setContent(loadFileFromResources("market/docs/TurbineHelsinki.zip"));
			marketAsset.setContentId("TurbineHelsinki.zip");

			marketAssetRepository.save(marketAsset);

			// API NodeRED

			marketAsset = new MarketAsset();

			marketAsset.setId("6");
			marketAsset.setIdentification("API NodeRED");

			marketAsset.setUser(getUserAdministrator());

			marketAsset.setPublic(true);
			marketAsset.setState(MarketAsset.MarketAssetState.APPROVED);
			marketAsset.setMarketAssetType(MarketAsset.MarketAssetType.DOCUMENT);
			marketAsset.setPaymentMode(MarketAsset.MarketAssetPaymentMode.FREE);

			marketAsset.setJsonDesc(loadFromResources("market/details/API NodeRED.json"));

			marketAsset.setImage(loadFileFromResources("market/img/gears.png"));
			marketAsset.setImageType("png");

			marketAsset.setContent(loadFileFromResources("market/docs/API NodeRED sofia4cities.zip"));
			marketAsset.setContentId("API NodeRED sofia4cities.zip");

			marketAssetRepository.save(marketAsset);

			// OAUTH2 Authentication

			marketAsset = new MarketAsset();

			marketAsset.setId("7");
			marketAsset.setIdentification("OAuth2AndJWT");

			marketAsset.setUser(getUserAdministrator());

			marketAsset.setPublic(true);
			marketAsset.setState(MarketAsset.MarketAssetState.APPROVED);
			marketAsset.setMarketAssetType(MarketAsset.MarketAssetType.DOCUMENT);
			marketAsset.setPaymentMode(MarketAsset.MarketAssetPaymentMode.FREE);

			marketAsset.setJsonDesc(loadFromResources("market/details/Oauth2Authentication.json"));

			marketAsset.setContent(loadFileFromResources("market/docs/oauth2-authentication.zip"));
			marketAsset.setContentId("oauth2-authentication.zip");

			marketAssetRepository.save(marketAsset);

			// Device simulator Jar

			marketAsset = new MarketAsset();

			marketAsset.setId("8");
			marketAsset.setIdentification("Device Simulator");

			marketAsset.setUser(getUserAdministrator());

			marketAsset.setPublic(true);
			marketAsset.setState(MarketAsset.MarketAssetState.APPROVED);
			marketAsset.setMarketAssetType(MarketAsset.MarketAssetType.APPLICATION);
			marketAsset.setPaymentMode(MarketAsset.MarketAssetPaymentMode.FREE);

			marketAsset.setImage(loadFileFromResources("market/img/jar-file.jpg"));
			marketAsset.setImageType("jpg");
			marketAsset.setJsonDesc(loadFromResources("market/details/DeviceSimulator.json"));

			marketAsset.setContent(loadFileFromResources("market/docs/device-simulator.zip"));
			marketAsset.setContentId("device-simulator.zip");

			marketAssetRepository.save(marketAsset);

			// JSON document example for Data import tool

			marketAsset = new MarketAsset();

			marketAsset.setId("9");
			marketAsset.setIdentification("Countries JSON");

			marketAsset.setUser(getUserAdministrator());

			marketAsset.setPublic(true);
			marketAsset.setState(MarketAsset.MarketAssetState.APPROVED);
			marketAsset.setMarketAssetType(MarketAsset.MarketAssetType.DOCUMENT);
			marketAsset.setPaymentMode(MarketAsset.MarketAssetPaymentMode.FREE);

			marketAsset.setJsonDesc(loadFromResources("market/details/Countries.json"));

			marketAsset.setImage(loadFileFromResources("market/img/json.png"));
			marketAsset.setImageType("png");
			marketAsset.setContent(loadFileFromResources("market/docs/countries.json"));
			marketAsset.setContentId("countries.json");

			marketAssetRepository.save(marketAsset);

			// Stress Application
			createMarketAsset("10", "StressApplication", MarketAsset.MarketAssetState.APPROVED,
					MarketAsset.MarketAssetType.URLAPPLICATION, MarketAsset.MarketAssetPaymentMode.FREE, true,
					"market/details/StressApplication.json", null, null, null, null);
			// Chat bot
			createMarketAsset("11", "ChatBot", MarketAsset.MarketAssetState.APPROVED,
					MarketAsset.MarketAssetType.URLAPPLICATION, MarketAsset.MarketAssetPaymentMode.FREE, true,
					"market/details/ChatBot.json", null, null, null, null);

			// Chat bot telegram
			/*
			 * createMarketAsset("12", "ChatBotTelegram",
			 * MarketAsset.MarketAssetState.APPROVED, MarketAsset.MarketAssetType.DOCUMENT,
			 * MarketAsset.MarketAssetPaymentMode.FREE, true,
			 * "market/details/ChatBotTelegram.json", null, null,
			 * "market/docs/countries.json", "countries.json");
			 */
			// Digital Twin Web
			createMarketAsset("12", "SenseHatDemo", MarketAsset.MarketAssetState.APPROVED,
					MarketAsset.MarketAssetType.WEBPROJECT, MarketAsset.MarketAssetPaymentMode.FREE, true,
					"market/details/SenseHatDemo.json", null, null, null, null);

			// Digital Twin Sense Hat
			createMarketAsset("13", "DigitalTwinSenseHat", MarketAsset.MarketAssetState.APPROVED,
					MarketAsset.MarketAssetType.APPLICATION, MarketAsset.MarketAssetPaymentMode.FREE, true,
					"market/details/DigitalTwinSenseHat.json", "market/img/jar-file.jpg", "jpg",
					"market/docs/SensehatHelsinki.zip", "SensehatHelsinki.zip");

			// videos
			createMarketAsset("14", "Tutorials", MarketAsset.MarketAssetState.APPROVED,
					MarketAsset.MarketAssetType.DOCUMENT, MarketAsset.MarketAssetPaymentMode.FREE, true,
					"market/details/Tutorials.json", null, null, null, null);

			// Health Check Android Application
			createMarketAsset("15", "HealthCheckAndroidApplication", MarketAsset.MarketAssetState.APPROVED,
					MarketAsset.MarketAssetType.APPLICATION, MarketAsset.MarketAssetPaymentMode.FREE, true,
					"market/details/HealthCheckApplication.json", null, null, "market/docs/HealthCheckApp.zip",
					"HealthCheckApp.zip");

			createMarketAsset("16", "management", MarketAsset.MarketAssetState.APPROVED,
					MarketAsset.MarketAssetType.WEBPROJECT, MarketAsset.MarketAssetPaymentMode.FREE, true,
					"market/details/IssueManagement.json", null, null, null, null);

			createMarketAsset("17", "QuickviewPlatform", MarketAsset.MarketAssetState.APPROVED,
					MarketAsset.MarketAssetType.DOCUMENT, MarketAsset.MarketAssetPaymentMode.FREE, true,
					"market/details/QuickviewPlatform.json", null, null, null, null);

		}
	}

	private void createMarketAsset(String id, String identification, MarketAsset.MarketAssetState state,
			MarketAsset.MarketAssetType assetType, MarketAsset.MarketAssetPaymentMode paymentMode, boolean isPublic,
			String jsonDesc, String image, String imageType, String content, String contentId) {

		MarketAsset marketAsset = new MarketAsset();

		marketAsset.setId(id);
		marketAsset.setIdentification(identification);

		marketAsset.setUser(getUserAdministrator());

		marketAsset.setPublic(isPublic);
		marketAsset.setState(state);
		marketAsset.setMarketAssetType(assetType);
		marketAsset.setPaymentMode(paymentMode);

		marketAsset.setJsonDesc(loadFromResources(jsonDesc));
		if (image != null) {
			marketAsset.setImage(loadFileFromResources(image));
			marketAsset.setImageType(imageType);
		}

		if (content != null) {
			marketAsset.setContent(loadFileFromResources(content));
			marketAsset.setContentId(contentId);
		}
		marketAssetRepository.save(marketAsset);
	}

	/*
	 * public void init_Template() { log.info("init template"); List<Template>
	 * templates= this.templateRepository.findAll();
	 * 
	 * if (templates.isEmpty()) { try {
	 * 
	 * log.info("No templates Adding..."); Template template= new Template();
	 * template.setIdentification("GSMA-Weather Forecast"); template.setType("0");
	 * template.
	 * setJsonschema("{    '$schema': 'http://json-schema.org/draft-04/schema#', 'title': 'Weather Forecast',    'type': 'object',    'properties': {        'id': {            'type': 'string'        },        'type': {            'type': 'string'        },        'address': {            'type': 'object',            'properties': {                'addressCountry': {                    'type': 'string'                },                'postalCode': {                    'type': 'string'                },                'addressLocality': {                    'type': 'string'                }            },            'required': [                'addressCountry',                'postalCode',                'addressLocality'            ]        },        'dataProvider': {            'type': 'string'        },        'dateIssued': {            'type': 'string'        },        'dateRetrieved': {            'type': 'string'        },        'dayMaximum': {            'type': 'object',            'properties': {                'feelsLikeTemperature': {                    'type': 'integer'                },                'temperature': {                    'type': 'integer'                },                'relativeHumidity': {                    'type': 'number'                }            },            'required': [                'feelsLikeTemperature',                'temperature',                'relativeHumidity'            ]        },        'dayMinimum': {            'type': 'object',            'properties': {                'feelsLikeTemperature': {                    'type': 'integer'                },                'temperature': {                    'type': 'integer'                },                'relativeHumidity': {                    'type': 'number'                }            },            'required': [                'feelsLikeTemperature',                'temperature',                'relativeHumidity'            ]        },        'feelsLikeTemperature': {            'type': 'integer'        },        'precipitationProbability': {            'type': 'number'        },        'relativeHumidity': {            'type': 'number'        },        'source': {            'type': 'string'        },        'temperature': {            'type': 'integer'        },        'validFrom': {            'type': 'string'        },        'validTo': {            'type': 'string'        },        'validity': {            'type': 'string'        },        'weatherType': {            'type': 'string'        },        'windDirection': {            'type': 'null'        },        'windSpeed': {            'type': 'integer'        }    },    'required': [        'id',        'type',        'address',        'dataProvider',        'dateIssued',        'dateRetrieved',        'dayMaximum',        'dayMinimum',        'feelsLikeTemperature',        'precipitationProbability',        'relativeHumidity',        'source',        'temperature',        'validFrom',        'validTo',        'validity',        'weatherType',        'windDirection',        'windSpeed'    ]}"
	 * ); template.
	 * setDescription("This contains a harmonised description of a Weather Forecast."
	 * ); template.setCategory("plantilla_categoriaGSMA");
	 * template.setIsrelational(false); templateRepository.save(template); ///
	 * template=new Template(); template.setIdentification("TagsProjectBrandwatch");
	 * template.setType("1"); template.
	 * setJsonschema("{  '$schema': 'http://json-schema.org/draft-04/schema#',  'title': 'TagsProjectBrandwatch Schema',  'type': 'object',  'required': [    'TagsProjectBrandwatch'  ],  'properties': {    'TagsProjectBrandwatch': {      'type': 'string',      '$ref': '#/datos'    }  },  'datos': {    'description': 'Info TagsProjectBrandwatch',    'type': 'object',    'required': [      'id',      'name'    ],    'properties': {      'id': {        'type': 'integer'      },      'name': {        'type': 'string'      }    }  }}"
	 * ); template.
	 * setDescription("Plantilla para almacenar los TAG definidos en un PROJECT Brandwatch"
	 * ); template.setCategory("plantilla_categoriaSocial");
	 * template.setIsrelational(false); templateRepository.save(template);
	 * 
	 * 
	 * 
	 * } catch (Exception e) { templateRepository.deleteAll(); }
	 * 
	 * } }
	 */

	public void init_notebook() {
		log.info("init notebook");
		List<Notebook> notebook = this.notebookRepository.findAll();
		if (notebook.isEmpty()) {

			try {
				User user = getUserAnalytics();
				Notebook n = new Notebook();

				n.setUser(user);
				n.setIdentification("Analytics s4c notebook tutorial");
				// Default zeppelin notebook tutorial ID
				n.setIdzep("2A94M5J1Z");
				notebookRepository.save(n);
			} catch (Exception e) {
				log.info("Could not create notebook");
			}

		}
	}
}
