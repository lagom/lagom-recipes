# Lagom Recipe: How to upload a file

It is not trivial how to handle file uploads in Lagom, Lagom is an abstraction over the transport and it doesn't include direct support for `mulipart-form-data`. 

This recipe demonstrates how to fallback from [Lagom's `ServiceCall`](https://www.lagomframework.com/documentation/1.4.x/scala/ServiceImplementation.html#Implementing-services) implementation to it's building block: [Play Actions](https://www.playframework.com/documentation/2.6.x/ScalaActions#What-is-an-Action?). The final solution uses an [asynchronous Action](https://www.playframework.com/documentation/2.6.x/ScalaAsync#Make-controllers-asynchronous) so we can control what execution context is used to run the file handling logic.

While this recipe focuses on solving the problem of handling file uploads you can learn from the solution presented here to fallback to Play Actions for other edge cases not supported by Lagom out of the box.

## Code details

The changes required on a Lagom service to handle File upload are:

* in the Service Descriptor at [FileuploadService](./fileupload-api/src/main/scala/com/example/fileupload/api/FileuploadService.scala) we add an endpoint explicitly stating it must be a `POST`.
* in the Service implementation at [FileuploadServiceImpl](./fileupload-impl/src/main/scala/com/example/fileupload/impl/FileuploadServiceImpl.scala) the implementation uses a [`PlayServiceCall`](https://www.lagomframework.com/documentation/1.4.x/scala/api/com/lightbend/lagom/scaladsl/server/PlayServiceCall.html) to overwrite Lagom's default bridge between Play's runtime and Lagom's `ServiceCall`

See the sources of [FileuploadService](./fileupload-api/src/main/scala/com/example/fileupload/api/FileuploadService.scala) and [FileuploadServiceImpl](./fileupload-impl/src/main/scala/com/example/fileupload/impl/FileuploadServiceImpl.scala) for more detailed, in-place comments.  

## Testing the recipe

You can test this recipe using 2 separate terminals.

On one terminal start the service:

```
sbt runAll
```

On a separate terminal, use `curl` to POST a file (in this example we're posting `build.sbt`:

```
curl -X POST -F "data=@build.sbt"  http://localhost:9000/api/files
```

Note the request must be a `POST`. Also note that uploaded files are stored on `./target/file-upload-data/uploads` under a random name. Make sure to cleanup that folder using the command `sbt clean` when you are done with your tests.

This recipe doesn't go in detail into all the features Play provides to handle file upload. For example, this recipe uses default values that will limit the size of the uploaded file. To know more about tuning file upload in Play see:

* [Writing a custom body parser](https://www.playframework.com/documentation/2.6.x/ScalaBodyParsers#Writing-a-custom-body-parser)
* [Choosing a Body parser / Max content Length](https://www.playframework.com/documentation/2.6.x/ScalaBodyParsers#Max-content-length)
* [Handling File Upload](https://www.playframework.com/documentation/2.6.x/ScalaFileUpload)


## Know more

Other recipes that mix Lagom and Play features:

* [Using Play's I18N support for Lagom data](i18n) is another example of [PlayServiceCall](https://www.lagomframework.com/documentation/1.4.x/scala/api/com/lightbend/lagom/scaladsl/server/PlayServiceCall.html)
* [Using CORS in Lagom](./cors).
* [Header and Status Code manipulation and testing](./http-header-handling/).

