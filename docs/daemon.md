# Validator daemon
You can also start the validator as a HTTP-Server. This is based [JDK http server](https://docs.oracle.com/javase/8/docs/jre/api/net/httpserver/spec/com/sun/net/httpserver/HttpServer.html) functionality 
and should work with OpenJDK based Java distributions. Keep that mind, if you want to deploy this 
in production scenarios with heavy load.

## Basic usage
To just use the validator daemon as is, start the _Daemon-Mode_ with the `-D` option and supply a suitable
 [validator configuration](configurations.md)

```shell
java -jar  validationtool-<version>-standalone.jar  -s <scenario-config-file> -D
```

Per default the HTTP-Server listens on _localhost_ at Port 8080.

You can configure the daemon with `-H` for IP Adress and `-P` for port number:

```shell
java -jar  validationtool-<version>-standalone.jar  -s <scenario-config-file> -D -H 192.168.1.x -P 8081
```

## Customized usage
You can also leverage the API to create a customized version of the Daemon. Just instantiate, configure and start the daemon like this:

````java
Configuration config = Configuration.load(...);

Daemon daemon = new Daemon();
daemon.setPort("8090");
// further config goes here
daemon.startServer(config);
```` 

The possible customizations are:

* `bindAddress` - the interface to bind the daemon to
* `port` - the port to expose
* `threadCount` - number of worker threads to handle results
* `guiEnabled` - enable or disable the basic gui with usage information

## Access the http interface
The validation service listens to `POST`-requests to any server uri. You need to supply the xml/object to validate in the post body. 
The service expects a single xml input in the post body, e.g. `multipart/form-data` is not supported.

Examples:

* `cURL`
```shell script
curl --location --request POST 'http://localhost:8080' \
--header 'Content-Type: application/xml' \
--data-binary '@/target.xml'
```

* `java` (Apache HttpClient)
```java
HttpClient httpClient = HttpClientBuilder.create().build();
HttpPost postRequest = new HttpPost("http://localhost:8080/");
FileEntity entity = new FileEntity(Paths.get("some.xml").toFile(), ContentType.APPLICATION_XML);
postRequest.setEntity(entity);
HttpResponse response = httpClient.execute(postRequest);
System.out.println(IOUtils.toString(response.getEntity().getContent()));
```

* `javascript`
```javascript
var myHeaders = new Headers();
myHeaders.append("Content-Type", "application/xml");

var file = "<file contents here>";

var requestOptions = {
  method: 'POST',
  headers: myHeaders,
  body: file,
  redirect: 'follow'
};

fetch("http://localhost:8080", requestOptions)
  .then(response => response.text())
  .then(result => console.log(result))
  .catch(error => console.log('error', error));
```
## Authorization
There is no mechanism to check, whether client is allowed to consume the service or not. The user is responsible to secure access to the service
This can be done using infrastructural service like a forwarding proxy (e.g. `nginx` or `apache http server`) or by implementing a custom solution

## Monitoring and administration
The validation service can be integrated in monitoring solutions like `Icinga` or `Nagios`. There is a `health` endpoint exposed under `/server/health` wich returns
some basic information about the service like memory consumption, general information about the version and a status `UP` as an XML file.

## GUI
The daemon provides a simple GUI when issuing `GET` requests providing the following:
 
 1. usage information 
 1. information about the actual [validator configuration](configurations.md) used by this daemon
 1. a simple form to test the daemon with custom inputs
 
 The GUI can be disabled with using the API (see above) or via CLI
 
 ```shell script
java -jar  validationtool-<version>-standalone.jar  -s <scenario-config-file> -D --disable-gui
```