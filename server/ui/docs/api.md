---
sidebar_position: 2
---

# API Usage

The validation service listens to `POST`-requests to any server uri. You need to supply the xml/object to validate in
the post body.
The service expects a single plain input in the post body, e.g. `multipart/form-data` is not supported.

Examples:

* `cURL`

```shell script
curl --location --request POST 'http://localhost:8080' \
--header 'Content-Type: application/xml' \
--data-binary '@/target.xml'
```

* `java` (Apache HttpClient)

```java
HttpClient httpClient=HttpClientBuilder.create().build();
        HttpPost postRequest=new HttpPost("http://localhost:8080/");
        FileEntity entity=new FileEntity(Paths.get("some.xml").toFile(),ContentType.APPLICATION_XML);
        postRequest.setEntity(entity);
        HttpResponse response=httpClient.execute(postRequest);
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