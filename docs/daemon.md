# Validator HTTP Daemon

You can start the validator as an HTTP-Server. This server is based on the [JDK HTTP server](https://docs.oracle.com/javase/8/docs/jre/api/net/httpserver/spec/com/sun/net/httpserver/HttpServer.html) functionality 
and should work with OpenJDK based distributions. Keep this in mind, if you want to deploy this in production scenarios with heavy load.

## Basic usage

To use the validator daemon as is, start the _Daemon-Mode_ with the `-D` option and supply a suitable
 [validator configuration](configurations.md).

```shell
java -jar  validationtool-<version>-standalone.jar  -s <scenario-config-file> -D
```

Per default the HTTP-Server listens on _localhost_ at Port 8080.

You can configure the daemon with `-H` for IP Adress and `-P` for port number:

```shell
java -jar  validationtool-<version>-standalone.jar  -s <scenario-config-file> -D -H 192.168.1.x -P 8081
```

## Customized usage

You can also leverage the API to create a customized version of the daemon. Just instantiate, configure and start the daemon like this:

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
* `guiEnabled` - enable or disable the basic GUI with usage information

## Access the HTTP interface

The validation service listens to `POST`-requests on any server URL. You need to supply the xml/object to validate in the HTTP body. 
The last segment of the request URI is treated as the name of the input. E.g. requests to `/myfile.xml`, `/mypath/myfile.xml` and `/mypath/myfile.xml?someParam=1`
would all result in an input named `myfile.xml`. If you don't specify a specific request URI (e.g. POST to `/`), the name is auto generated for you. 

The service expects a single XML input in the HTTP body, e.g. `multipart/form-data` is NOT supported.

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

* `PHP` (Symfony HttpClient)
```php
$httpClient = HttpClient::create();

$response = $httpClient->request('POST', 'http://localhost:8080', [
  'headers' => [
    'Content-Type' => 'application/xml',
  ],
  'body' => fopen('/path/to/some.xml', 'r'),
]);

echo $response->getContent();

```

## Status codes
| code | description |
|-|-|
| 200  | The xml file is acceptable according to the scenario configurations |
| 400 | Bad request. the request contains errors, e.g. no content supplied  |
| 405 | Method not allowed. Thec check service is only answering on POST requests |
| 406 | The xml file is NOT acceptable according to the scenario configurations| 
| 422 | Unprocessable entity. Indicates an error while processing the xml file. This hints to errors in the scenario configuration |
| 500 | Internal server error. Something went wrong |

## Authorization
There is no mechanism to check, whether client is allowed to consume the service or not. The user is responsible to secure access to the service.
This can be done using infrastructural service like a forwarding proxies (e.g. `nginx` or `Apache http server`) or by implementing a custom solution.

## Monitoring and administration

The validation service can be integrated in monitoring solutions like `Icinga` or `Nagios`. There is a `health` endpoint exposed under `/server/health` wich returns some basic information about the service like memory consumption, general information about the version and a status `UP` as an XML file.

## GUI

The daemon provides a simple GUI when issuing `GET` requests providing the following:
 
 1. usage information 
 1. information about the actual [validator configuration](configurations.md) used by this daemon
 1. a simple form to test the daemon with custom inputs
 
 The GUI can be disabled using the API (see above) or via CLI:
 
 ```shell script
java -jar  validationtool-<version>-standalone.jar  -s <scenario-config-file> -D --disable-gui
```
