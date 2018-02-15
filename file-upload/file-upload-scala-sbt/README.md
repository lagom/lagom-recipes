# Lagom Recipe: How to upload a file

It is not trivial how to handle file uploads in Lagom, Lagom is an abstraction over the transport and it doesn't include direct support for `mulipart-form-data`. 

This recipe demonstrates how to fallback from [Lagom's `ServiceCall`](https://www.lagomframework.com/documentation/1.4.x/scala/ServiceImplementation.html#Implementing-services) implementation to it's building block: [Play Actions](https://www.playframework.com/documentation/2.6.x/ScalaActions#What-is-an-Action?). The final solution uses an [asynchronous Action](https://www.playframework.com/documentation/2.6.x/ScalaAsync#Make-controllers-asynchronous) so we can control what execution context is used to run the file handling logic.

While this recipe focuses on solving the problem of handling file uploads you can learn from the solution presented here to fallback to Play Actions for other edge cases not supported by Lagom out of the box.

## Code details

The changes required on a Lagom service to handle File upload are:

* in the Service Descriptor at [FileuploadService](./fileupload-api/src/main/scala/com/example/fileupload/api/FileuploadService.scala) we add an endpoint explicitly stating it must be a `POST`. This is necessary because the `ServiceCall[T,Q]` for the file upload method doesn't get any payload on the request and LAgom would default to `GET`. See the inline comments on the sources for more details.
* in the Service implementation at [FileuploadServiceImpl](./fileupload-impl/src/main/scala/com/example/fileupload/impl/FileuploadServiceImpl.scala) the implementation uses a [`PlayServiceCall`](https://www.lagomframework.com/documentation/1.4.x/scala/api/com/lightbend/lagom/scaladsl/server/PlayServiceCall.html) to overwrite Lagom's default bridge between Play's runtime and Lagom's `ServiceCall`

See the sources of [FileuploadService](./fileupload-api/src/main/scala/com/example/fileupload/api/FileuploadService.scala) and [FileuploadServiceImpl](./fileupload-impl/src/main/scala/com/example/fileupload/impl/FileuploadServiceImpl.scala) for more detailed, in-place comments. 


This recipe is also interesting as a demonstration on how to use a non-Lagom client to execute low-level HTTP requests. To test the implemented service there's a test in [FileuploadServiceSpec](./fileupload-impl/src/test/scala/com/example/fileupload/impl/FileuploadServiceSpec.scala) which takes `sampleFile.txt` and uploads it using play-WS. The server response contains the absolute path of the stored file so the test can assert the upload completed successfully and the stored bytes are equal to the uploaded bytes.


Here we don't go in detail into all the features Play provides to handle file upload in neither the client or the server sides. For example, this recipe uses default values that will limit the size of the uploaded file. 

To know more about tuning file upload in Play see:

* [Writing a custom body parser](https://www.playframework.com/documentation/2.6.x/ScalaBodyParsers#Writing-a-custom-body-parser)
* [Choosing a Body parser / Max content Length](https://www.playframework.com/documentation/2.6.x/ScalaBodyParsers#Max-content-length)
* [Handling File Upload](https://www.playframework.com/documentation/2.6.x/ScalaFileUpload)

You may also be interested in the [Play-specific example](https://github.com/playframework/play-scala-fileupload-example) on handling file uploads.

## Testing the recipe

##### unit tests

You can test this recipe using the provided tests:

```
sbt test
```

##### manual tests

You can also test this recipe manually using 2 separate terminals.

On one terminal start the service:

```
sbt runAll
```

On a separate terminal, use `curl` to POST a file (in this example we're posting `build.sbt`:

```
curl -X POST -F "data=@build.sbt" -v  http://localhost:9000/api/files
```


## Know more

Other recipes that mix Lagom and Play features:

* [Using Play's I18N support for Lagom data](i18n) is another example of [PlayServiceCall](https://www.lagomframework.com/documentation/1.4.x/scala/api/com/lightbend/lagom/scaladsl/server/PlayServiceCall.html)
* [Using CORS in Lagom](./cors).
* [Header and Status Code manipulation and testing](./http-header-handling/).

