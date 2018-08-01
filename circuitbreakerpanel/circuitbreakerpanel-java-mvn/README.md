# CircuitBreakerPanel recipe for Lagom with Java

CircuitBreakerPanel can be used for with any arbitrary api to apply the circuit breaker pattern to it.
Instead of wasting resources on a particular part of the service that's failing you can use the circuit breaker pattern to
fail immediately in case a certain dependency of 1 or more of your endpoints is down.

## Implementation details
The [`circuitbreakerpanel-impl/src/main/resources/application.conf`](src/main/resources/application.conf) contains configuration for your circuitbreaker:

This configuration is specified in detail in the [`lagom documentation`](https://www.lagomframework.com/documentation/1.4.x/java/ServiceClients.html#Circuit-Breaker-Configuration)

In this sample we use the circuitBreakerPanel in [`DemoServceimpl`](src/main/java/com/lightbend/lagom/recipes/cbpanel/impl/DemoServiceImpl)
when invoking our repository layer.
In a real scenario this repository will be interacting with a persistence layer.



## Testing the recipe

To start the service:

```
mvn lagom:runAll
```
To push the circuitBreaker into an open state just hit the service with a path parameter which is larger than 20 
characters in length.
e.g
```
curl http://localhost:9000/random/i_am_going_home_now_because_i_am_sleepy
```
Hitting the service with above 3 times and the circuitbreaker goes into an open state, when this happens you will
see the CircuitBreakerOpen exception which indicates calls are failing fast.

But if you hit the service with a parameter of size one , even though the repository throws an exception, the failure
won't add towards the circuitbreakers open state.
e.g.
```
curl http://localhost:9000/random/i
```


