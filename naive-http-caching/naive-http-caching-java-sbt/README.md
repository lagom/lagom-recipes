# Header Manipulation and HTTP testing

This project demonstrates how to manipulate HTTP headers and response Status codes using Lagom. It also demonstrates how to write tests that assert the low-level HTTP dialogue (headers, status codes, etc...) is what you expect. 

The code base contains only two classes of interest:

 * `HelloServiceImpl.java` implements an endpoint which reads requests headers. It also manipulates response headers and status codes. There's more information on how to do that in [Lagom docs](https://www.lagomframework.com/documentation/current/java/ServiceImplementation.html#Handling-headers). In this example, the header manipulation is used to implement a naïve caching mechanism that uses HTTP headers to control the client cache. 
 * `HelloServiceTest.java` tests the behavior in `HelloServiceImpl.java` using Lagom's client for the happy path and a non-Lagom HTTP client (in this case a plain `java.net.HttpUrlConnection`) to access all the fields on the low-level HTTP protocol.  
