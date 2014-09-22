SINTEF Featureserver (name TBD)
===============================

Webservice that serves features from netCDF files as JSON, to be used for client-side display 
(mapping, graphing).

The service uses a Jetty webserver and Jersey for REST resources.


Requirements
------------
* Java 8 / JDK 1.8
* Maven 3


Getting Started
---------------
Compile and run

```
$ cd {project root}
$ mvn clean package
$ java -jar target/featureserver-0.1-SNAPSHOT-jar-with-dependencies.jar
```
_(There is a launch script called `start.sh` which can be used as well. It only contains the 
launch command)_

Launch flags:
* `--webserver-port`: Which port do you want to listen on? Defaults to `10100`. Optional.

To check that the server is running: `http://localhost:10100/health/shallow`
Salinity resource example: `http://localhost:10100/feature/salinity?startx=0&endx=5&starty=0&endy=5
&depth=2&time=2`

Contributing
------------
Fork on github to your own repository. Make changes. Submit pull request. This way we can have a 
nice code review process going. 


Troubleshooting maven 
---------------------
To install maven on OSX:  
`$ brew install maven`

Make sure maven uses JDK 1.8:  
`$ mvn --version` - look for Java Version

If maven doesn't use the correct jdk version, try this:  
`$ export JAVA_HOME=$(/usr/libexec/java_home -v 1.8)`
