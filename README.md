# DAPNET Legacy Transmitter Service
The DAPNET Legacy Transmitter Service is responsible for providing a backwards compatibility layer to DAPNET v1 transmitters. It implements a transmitter server that is compatible with the legacy protocol used by the DAPNET Core. Feature support is limited to call-based pager messages in the scope of the legacy protocol. If possible, transmitters should be upgraded to the new DAPNET protocol/architecture for optimal support.

## Build
To build the service from source, you will need git, a Java SDK (Java 11+) and Maven installed. Once these requirements are met and properly set up, clone the repository and run `mvn clean package` to build a binary distribution package.

## Installation
After obtaining the binary packages, either by downloading a release build or building your own, extract the service to a location of your choice. For running the service, you will need a Java runtime (Java 11+).

After extracting the service binaries, the service must be configured. An example configuration file is provided.

	# RabbitMQ settings
	amqp.host=server-name
	amqp.user=user
	amqp.password=pa$$w0rd
	amqp.exchange=dapnet.calls
	
	# Service URLs
	services.bootstrap=http://transmitter-service/transmitters/_bootstrap
	services.heartbeat=http://transmitter-service/transmitters/_heartbeat
	
	# Transmitter server
	serverPort=43434
	numberOfSyncLoops=5
	# Send speed in bps: 0=512, 1=1200, 2=2400
	sendSpeed=1

Note that you have to adjust the settings, i.e. use the proper server names and credentials.

## Usage
Once the configuration file is created as described in the previous section, you can start the service with the following command:

	java -Ddapnet.config_file=legacy-service.properties -jar legacy-transmitter-service-x.x.x.jar

If the parameter `-Ddapnet.config_file` is not specified, the default value of `legacy-service.properties` is used.

## License
The DAPNET Legacy Transmitter Service is licensed under the GNU GPLv3. A copy of the license is included in the repository.
