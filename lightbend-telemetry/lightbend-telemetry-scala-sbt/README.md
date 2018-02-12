# Lightbend Telemetry (Cinnamon)

TODO: Finish README.md (this is just a skeleton with some notes and tips from Ruth's Readme Template)

https://developer.lightbend.com/docs/cinnamon/latest/home.html
https://developer.lightbend.com/docs/cinnamon/latest/getting-started/play.html

TODO: Add an example of circuit breaker metrics in a second service?


## Running

Running `sbt runAll` works as usual in this project, but you will _not_ see any endpoint metrics.

This is due to an interaction between the way Cinnamon instruments code using a Java agent, and the way Lagom runs services in-process within sbt.

Instead, `sbt-native-packager` provides a `stage` command that can be used to build an unzipped production distribution directory, including a script to run the service.

This service has no other dependencies (on Cassandra, Kafka, or other services) and has configured `NoServiceLocator`. The only additional configuration required is a secret key for Play (not actually used in this service, but Play won't start without one configured).

```
sbt stage
endpoint-metrics-impl/target/universal/stage/bin/endpoint-metrics-impl \
  -Dplay.http.secret.key=$(openssl rand -base64 32)
```

## Usage examples

Using `curl`:

```
$ curl -i -w '\n' http://localhost:9000/ok
HTTP/1.1 200 OK
Date: Thu, 01 Feb 2018 00:38:37 GMT
Server: akka-http/10.0.11
Content-Length: 0


$ curl -i -w '\n' http://localhost:9000/fail
HTTP/1.1 500 Internal Server Error
Date: Thu, 01 Feb 2018 00:38:39 GMT
Server: akka-http/10.0.11
Content-Type: application/json
Content-Length: 59

{"name":"EndpointMetricsTestServiceImpl","detail":"failed"}
$ curl -i -w '\n' http://localhost:9000/not-found
HTTP/1.1 404 Not Found
Date: Thu, 01 Feb 2018 00:38:43 GMT
Server: akka-http/10.0.11
Content-Type: application/json
Content-Length: 40

{"name":"NotFound","detail":"not found"}
$ curl -i -w '\n' http://localhost:9000/maybe-fail?probability=0 # never fails
HTTP/1.1 200 OK
Date: Thu, 01 Feb 2018 00:38:47 GMT
Server: akka-http/10.0.11
Content-Length: 0


$ curl -i -w '\n' http://localhost:9000/maybe-fail?probability=1 # always fails
HTTP/1.1 500 Internal Server Error
Date: Thu, 01 Feb 2018 00:39:05 GMT
Server: akka-http/10.0.11
Content-Type: application/json
Content-Length: 59

{"name":"EndpointMetricsTestServiceImpl","detail":"failed"}
$ curl -i -w '\n' http://localhost:9000/maybe-fail?probability=0.5 # fails approximately half of the time
HTTP/1.1 500 Internal Server Error
Date: Thu, 01 Feb 2018 00:39:10 GMT
Server: akka-http/10.0.11
Content-Type: application/json
Content-Length: 59

{"name":"EndpointMetricsTestServiceImpl","detail":"failed"}
$ curl -i -w '\n' http://localhost:9000/maybe-fail?probability=0.5
HTTP/1.1 200 OK
Date: Thu, 01 Feb 2018 00:39:12 GMT
Server: akka-http/10.0.11
Content-Length: 0


$ curl -i -w '\n' http://localhost:9000/welcome-message
HTTP/1.1 200 OK
Date: Thu, 01 Feb 2018 00:39:34 GMT
Server: akka-http/10.0.11
Content-Type: text/plain
Content-Length: 12

Hello, world
$ curl -i -w '\n' -X PUT -H 'Content-Type: text/plain' -d 'Welcome' http://localhost:9000/welcome-message
HTTP/1.1 200 OK
Date: Thu, 01 Feb 2018 00:39:54 GMT
Server: akka-http/10.0.11
Content-Type: text/plain
Content-Length: 7

Welcome
$ curl -i -w '\n' http://localhost:9000/welcome-message
HTTP/1.1 200 OK
Date: Thu, 01 Feb 2018 00:40:04 GMT
Server: akka-http/10.0.11
Content-Type: text/plain
Content-Length: 7

Welcome
$ curl -i -w '\n' http://localhost:9000/static/path
HTTP/1.1 200 OK
Date: Thu, 01 Feb 2018 00:40:16 GMT
Server: akka-http/10.0.11
Content-Type: application/json
Content-Length: 2

{}
$ curl -i -w '\n' http://localhost:9000/path/with-parameter
HTTP/1.1 200 OK
Date: Thu, 01 Feb 2018 00:40:27 GMT
Server: akka-http/10.0.11
Content-Type: application/json
Content-Length: 26

{"param":"with-parameter"}
$ curl -i -w '\n' http://localhost:9000/multi/segment/path/parameter/sequence
HTTP/1.1 200 OK
Date: Thu, 01 Feb 2018 00:40:41 GMT
Server: akka-http/10.0.11
Content-Type: application/json
Content-Length: 34

{"path":"path/parameter/sequence"}
$ curl -i -w '\n' http://localhost:9000/regex/$(uuidgen)
HTTP/1.1 200 OK
Date: Thu, 01 Feb 2018 00:41:06 GMT
Server: akka-http/10.0.11
Content-Type: application/json
Content-Length: 47

{"uuid":"3359de6e-9625-4c24-8b1a-06d9654961cf"}
$ curl -i -w '\n' http://localhost:9000/regex/$(uuidgen)
HTTP/1.1 200 OK
Date: Thu, 01 Feb 2018 00:41:09 GMT
Server: akka-http/10.0.11
Content-Type: application/json
Content-Length: 47

{"uuid":"aeef4fd0-14b2-4e07-821e-21cc7749dd15"}
$ curl -i -w '\n' http://localhost:9000/regex/invalid
HTTP/1.1 404 Not Found
Server: akka-http/10.0.11
Date: Thu, 01 Feb 2018 00:41:22 GMT
Content-Type: text/html; charset=UTF-8
Content-Length: 7639

# trimmed long HTML output from default Play routing error page

$ curl -i -w '\n' 'http://localhost:9000/query?param=parameter-value&names=Alice&names=Bob'
HTTP/1.1 200 OK
Date: Thu, 01 Feb 2018 00:42:04 GMT
Server: akka-http/10.0.11
Content-Type: application/json
Content-Length: 51

{"param":"parameter-value","names":["Alice","Bob"]}
$ curl -i -w '\n' "http://localhost:9000/all/parameter-value/$(uuidgen)/multi/segment/path?names=Alice&names=Bob&names=Carol&names=David"
HTTP/1.1 200 OK
Date: Thu, 01 Feb 2018 00:43:24 GMT
Server: akka-http/10.0.11
Content-Type: application/json
Content-Length: 141

{"param":"parameter-value","uuid":"fc0a731c-9342-47a7-983d-f4672af7cd43","path":"multi/segment/path","names":["Alice","Bob","Carol","David"]}
```

## Example output

```
2/1/18 1:12:57 PM ==============================================================

-- Gauges ----------------------------------------------------------------------
metrics.akka.systems.application.dispatchers.akka_actor_default-dispatcher.active-threads
             value = 0
metrics.akka.systems.application.dispatchers.akka_actor_default-dispatcher.parallelism
             value = 8
metrics.akka.systems.application.dispatchers.akka_actor_default-dispatcher.pool-size
             value = 4
metrics.akka.systems.application.dispatchers.akka_actor_default-dispatcher.queued-tasks
             value = 0
metrics.akka.systems.application.dispatchers.akka_actor_default-dispatcher.running-threads
             value = 0
metrics.akka.systems.application.dispatchers.akka_io_pinned-dispatcher.active-threads
             value = 1
metrics.akka.systems.application.dispatchers.akka_io_pinned-dispatcher.pool-size
             value = 1
metrics.akka.systems.application.dispatchers.akka_io_pinned-dispatcher.running-threads
             value = 0

-- Histograms ------------------------------------------------------------------
metrics.akka-http.systems.application.http-servers.0_0_0_0_0_0_0_1_9000.request-paths._all__param_$uuid<[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[34][0-9a-fA-F]{3}-[89abAB][0-9a-fA-F]{3}-[0-9a-fA-F]{12}>_*path?names.endpoint-response-time
             count = 1
               min = 3589430
               max = 3589430
              mean = 3589430.00
            stddev = 0.00
            median = 3589430.00
              75% <= 3589430.00
              95% <= 3589430.00
              98% <= 3589430.00
              99% <= 3589430.00
            99.9% <= 3589430.00
metrics.akka-http.systems.application.http-servers.0_0_0_0_0_0_0_1_9000.request-paths._fail.endpoint-response-time
             count = 1
               min = 238960571
               max = 238960571
              mean = 238960571.00
            stddev = 0.00
            median = 238960571.00
              75% <= 238960571.00
              95% <= 238960571.00
              98% <= 238960571.00
              99% <= 238960571.00
            99.9% <= 238960571.00
metrics.akka-http.systems.application.http-servers.0_0_0_0_0_0_0_1_9000.request-paths._maybe-fail?probability.endpoint-response-time
             count = 11
               min = 2262196
               max = 8461306
              mean = 3924573.27
            stddev = 1875467.34
            median = 3369990.00
              75% <= 3509098.00
              95% <= 8461306.00
              98% <= 8461306.00
              99% <= 8461306.00
            99.9% <= 8461306.00
metrics.akka-http.systems.application.http-servers.0_0_0_0_0_0_0_1_9000.request-paths._multi_segment_*path.endpoint-response-time
             count = 1
               min = 3515594
               max = 3515594
              mean = 3515594.00
            stddev = 0.00
            median = 3515594.00
              75% <= 3515594.00
              95% <= 3515594.00
              98% <= 3515594.00
              99% <= 3515594.00
            99.9% <= 3515594.00
metrics.akka-http.systems.application.http-servers.0_0_0_0_0_0_0_1_9000.request-paths._not-found.endpoint-response-time
             count = 1
               min = 4382314
               max = 4382314
              mean = 4382314.00
            stddev = 0.00
            median = 4382314.00
              75% <= 4382314.00
              95% <= 4382314.00
              98% <= 4382314.00
              99% <= 4382314.00
            99.9% <= 4382314.00
metrics.akka-http.systems.application.http-servers.0_0_0_0_0_0_0_1_9000.request-paths._ok.endpoint-response-time
             count = 1
               min = 170677402
               max = 170677402
              mean = 170677402.00
            stddev = 0.00
            median = 170677402.00
              75% <= 170677402.00
              95% <= 170677402.00
              98% <= 170677402.00
              99% <= 170677402.00
            99.9% <= 170677402.00
metrics.akka-http.systems.application.http-servers.0_0_0_0_0_0_0_1_9000.request-paths._path__param.endpoint-response-time
             count = 1
               min = 10319935
               max = 10319935
              mean = 10319935.00
            stddev = 0.00
            median = 10319935.00
              75% <= 10319935.00
              95% <= 10319935.00
              98% <= 10319935.00
              99% <= 10319935.00
            99.9% <= 10319935.00
metrics.akka-http.systems.application.http-servers.0_0_0_0_0_0_0_1_9000.request-paths._query?param&names.endpoint-response-time
             count = 1
               min = 5542746
               max = 5542746
              mean = 5542746.00
            stddev = 0.00
            median = 5542746.00
              75% <= 5542746.00
              95% <= 5542746.00
              98% <= 5542746.00
              99% <= 5542746.00
            99.9% <= 5542746.00
metrics.akka-http.systems.application.http-servers.0_0_0_0_0_0_0_1_9000.request-paths._regex_$uuid<[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[34][0-9a-fA-F]{3}-[89abAB][0-9a-fA-F]{3}-[0-9a-fA-F]{12}>.endpoint-response-time
             count = 5
               min = 2717041
               max = 3069194
              mean = 2880373.14
            stddev = 153611.70
            median = 2842356.00
              75% <= 3059262.00
              95% <= 3069194.00
              98% <= 3069194.00
              99% <= 3069194.00
            99.9% <= 3069194.00
metrics.akka-http.systems.application.http-servers.0_0_0_0_0_0_0_1_9000.request-paths._static_path.endpoint-response-time
             count = 1
               min = 7437786
               max = 7437786
              mean = 7437786.00
            stddev = 0.00
            median = 7437786.00
              75% <= 7437786.00
              95% <= 7437786.00
              98% <= 7437786.00
              99% <= 7437786.00
            99.9% <= 7437786.00
metrics.akka-http.systems.application.http-servers.0_0_0_0_0_0_0_1_9000.request-paths._welcome-message.endpoint-response-time
             count = 4
               min = 2257162
               max = 34793891
              mean = 11740123.59
            stddev = 13677525.23
            median = 3261307.00
              75% <= 34793891.00
              95% <= 34793891.00
              98% <= 34793891.00
              99% <= 34793891.00
            99.9% <= 34793891.00
metrics.akka-http.systems.application.http-servers.0_0_0_0_0_0_0_1_9000.response-time
             count = 29
               min = 2257162
               max = 238960571
              mean = 12541157.91
            stddev = 33050527.94
            median = 3509098.00
              75% <= 6058485.00
              95% <= 38284834.00
              98% <= 170677402.00
              99% <= 238960571.00
            99.9% <= 238960571.00
metrics.akka-http.systems.application.http-servers.0_0_0_0_0_0_0_1_9000.response-time-2xx
             count = 20
               min = 2257162
               max = 170677402
              mean = 7990178.27
            stddev = 21242085.66
            median = 3506367.00
              75% <= 5542746.00
              95% <= 34793891.00
              98% <= 34793891.00
              99% <= 170677402.00
            99.9% <= 170677402.00
metrics.akka-http.systems.application.http-servers.0_0_0_0_0_0_0_1_9000.response-time-3xx
             count = 0
               min = 0
               max = 0
              mean = 0.00
            stddev = 0.00
            median = 0.00
              75% <= 0.00
              95% <= 0.00
              98% <= 0.00
              99% <= 0.00
            99.9% <= 0.00
metrics.akka-http.systems.application.http-servers.0_0_0_0_0_0_0_1_9000.response-time-4xx
             count = 2
               min = 4382314
               max = 38284834
              mean = 32683972.58
            stddev = 12590221.11
            median = 38284834.00
              75% <= 38284834.00
              95% <= 38284834.00
              98% <= 38284834.00
              99% <= 38284834.00
            99.9% <= 38284834.00
metrics.akka-http.systems.application.http-servers.0_0_0_0_0_0_0_1_9000.response-time-5xx
             count = 7
               min = 2981646
               max = 238960571
              mean = 26639181.17
            stddev = 70482870.18
            median = 3369990.00
              75% <= 3438631.00
              95% <= 238960571.00
              98% <= 238960571.00
              99% <= 238960571.00
            99.9% <= 238960571.00

-- Meters ----------------------------------------------------------------------
metrics.akka-http.systems.application.http-servers.0_0_0_0_0_0_0_1_9000.connections
             count = 29
         mean rate = 0.21 events/second
     1-minute rate = 0.21 events/second
     5-minute rate = 0.33 events/second
    15-minute rate = 0.37 events/second
metrics.akka-http.systems.application.http-servers.0_0_0_0_0_0_0_1_9000.request-paths._all__param_$uuid<[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[34][0-9a-fA-F]{3}-[89abAB][0-9a-fA-F]{3}-[0-9a-fA-F]{12}>_*path?names.endpoint-responses
             count = 1
         mean rate = 0.18 events/second
     1-minute rate = 0.20 events/second
     5-minute rate = 0.20 events/second
    15-minute rate = 0.20 events/second
metrics.akka-http.systems.application.http-servers.0_0_0_0_0_0_0_1_9000.request-paths._fail.endpoint-responses
             count = 1
         mean rate = 0.01 events/second
     1-minute rate = 0.02 events/second
     5-minute rate = 0.13 events/second
    15-minute rate = 0.17 events/second
metrics.akka-http.systems.application.http-servers.0_0_0_0_0_0_0_1_9000.request-paths._maybe-fail?probability.endpoint-responses
             count = 11
         mean rate = 0.09 events/second
     1-minute rate = 0.06 events/second
     5-minute rate = 0.16 events/second
    15-minute rate = 0.19 events/second
metrics.akka-http.systems.application.http-servers.0_0_0_0_0_0_0_1_9000.request-paths._multi_segment_*path.endpoint-responses
             count = 1
         mean rate = 0.02 events/second
     1-minute rate = 0.10 events/second
     5-minute rate = 0.18 events/second
    15-minute rate = 0.19 events/second
metrics.akka-http.systems.application.http-servers.0_0_0_0_0_0_0_1_9000.request-paths._not-found.endpoint-responses
             count = 1
         mean rate = 0.01 events/second
     1-minute rate = 0.03 events/second
     5-minute rate = 0.13 events/second
    15-minute rate = 0.18 events/second
metrics.akka-http.systems.application.http-servers.0_0_0_0_0_0_0_1_9000.request-paths._ok.endpoint-responses
             count = 1
         mean rate = 0.01 events/second
     1-minute rate = 0.02 events/second
     5-minute rate = 0.13 events/second
    15-minute rate = 0.17 events/second
metrics.akka-http.systems.application.http-servers.0_0_0_0_0_0_0_1_9000.request-paths._path__param.endpoint-responses
             count = 1
         mean rate = 0.02 events/second
     1-minute rate = 0.09 events/second
     5-minute rate = 0.17 events/second
    15-minute rate = 0.19 events/second
metrics.akka-http.systems.application.http-servers.0_0_0_0_0_0_0_1_9000.request-paths._query?param&names.endpoint-responses
             count = 1
         mean rate = 0.08 events/second
     1-minute rate = 0.18 events/second
     5-minute rate = 0.20 events/second
    15-minute rate = 0.20 events/second
metrics.akka-http.systems.application.http-servers.0_0_0_0_0_0_0_1_9000.request-paths._regex_$uuid<[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[34][0-9a-fA-F]{3}-[89abAB][0-9a-fA-F]{3}-[0-9a-fA-F]{12}>.endpoint-responses
             count = 5
         mean rate = 0.12 events/second
     1-minute rate = 0.25 events/second
     5-minute rate = 0.36 events/second
    15-minute rate = 0.39 events/second
metrics.akka-http.systems.application.http-servers.0_0_0_0_0_0_0_1_9000.request-paths._static_path.endpoint-responses
             count = 1
         mean rate = 0.02 events/second
     1-minute rate = 0.08 events/second
     5-minute rate = 0.17 events/second
    15-minute rate = 0.19 events/second
metrics.akka-http.systems.application.http-servers.0_0_0_0_0_0_0_1_9000.request-paths._welcome-message.endpoint-responses
             count = 4
         mean rate = 0.05 events/second
     1-minute rate = 0.07 events/second
     5-minute rate = 0.16 events/second
    15-minute rate = 0.19 events/second
metrics.akka-http.systems.application.http-servers.0_0_0_0_0_0_0_1_9000.requests
             count = 29
         mean rate = 0.21 events/second
     1-minute rate = 0.21 events/second
     5-minute rate = 0.33 events/second
    15-minute rate = 0.37 events/second
metrics.akka-http.systems.application.http-servers.0_0_0_0_0_0_0_1_9000.responses
             count = 29
         mean rate = 0.21 events/second
     1-minute rate = 0.21 events/second
     5-minute rate = 0.33 events/second
    15-minute rate = 0.37 events/second
metrics.akka-http.systems.application.http-servers.0_0_0_0_0_0_0_1_9000.responses-2xx
             count = 20
         mean rate = 0.15 events/second
     1-minute rate = 0.15 events/second
     5-minute rate = 0.18 events/second
    15-minute rate = 0.19 events/second
metrics.akka-http.systems.application.http-servers.0_0_0_0_0_0_0_1_9000.responses-3xx
             count = 0
         mean rate = 0.00 events/second
     1-minute rate = 0.00 events/second
     5-minute rate = 0.00 events/second
    15-minute rate = 0.00 events/second
metrics.akka-http.systems.application.http-servers.0_0_0_0_0_0_0_1_9000.responses-4xx
             count = 2
         mean rate = 0.01 events/second
     1-minute rate = 0.01 events/second
     5-minute rate = 0.01 events/second
    15-minute rate = 0.00 events/second
metrics.akka-http.systems.application.http-servers.0_0_0_0_0_0_0_1_9000.responses-5xx
             count = 7
         mean rate = 0.05 events/second
     1-minute rate = 0.04 events/second
     5-minute rate = 0.14 events/second
    15-minute rate = 0.18 events/second

```

# TBD

Readme Template instructions follow:

## Introduction


Start with a brief overview that includes:
- What the reader will learn from trying the example
- What the example demonstrates and what will happen when they run it. If it uses a build tool or third-party service which might be new to them, provide a brief description of why it is included and what it is.
- The high level steps they will follow, for example:
  - Check prerequisites and install any third-party that they don’t already have
  - Clone the repo locally
  - Configure
  - Start any required supporting services
  - Run the example on the command line
  - Use a browser or whatever to observe behavior
  - Shut down

Arrange your subheadings to mimic the high-level steps. However, if you find that the readme will be extremely long, you might also want to break it up into two or more “pages”. In that case, you will need to also provide clear navigation between pages. For example, you might have a mini-toc before the introduction in the form of a bulleted list of links:

This readme includes three sections:
- Getting ready - understanding the example and getting your environment ready
- Configuring and running
- Exercising and shutting down

Then, optionally, provide the mini-toc at the top of each, but for sure, provide links at the bottom of each section to the next, and at the top of each section back to the previous. Paradox apparently includes a way to do a TOC for a multi-page readme, so you don’t have to do it manually.

In each section, provide a brief overview of what they will be doing. If there are alternate ways to do something, describe them in the intro and provide them in alternate subheadings. If a section contains more than two steps, number them.

Tips on writing steps:
1.  Number them.
    When the result of the step is important for them to note, provide it in an indented sentence below the step.
2.  Try to make the step description as simple as possible.
    If you must add explanation or example code do it as an indented paragraph below the step description.
3.  Avoid putting “gotchas” in the steps.
    Put them in the intro so they know them ahead of time.
4.  Avoid false starts.
    For example, getting to this point and saying you don’t need to follow step 3 and 4 if you already …. Instead, if it is a group of steps, put it in an “optional” section, which you explain in the section intro. Or start the step description with “Optionally ...”

Notes: If you need to add an important note in the middle or after the steps, make sure that you mention it in the intro as well.

## Prerequisites

List the required OS version, language version, build tool version, and anything else that they might not have on their local machine.

### Third-party reference

Since we don’t know whether the reader has the third-party stuff installed already, this section is optional for them. That’s why I’ve made it a child of Prerequisites. You could have several of these headings, such as “Installing Kubernetes”, “Installing sbt”, or you could do it all in the same section. Either way, at the least provide a link to where they can download the software. And, point them to the third-party doc for installation instructions.

## Obtaining the example

Describe how to clone the repo locally. Provide the link first, then the instructions. That way, if they are familiar with git, they can skip the instructions. This doesn’t have to be written every time, we should write it once and just reuse it.

## Configuring

Describe what they will be configuring. If only simple configuration is required, just do it as steps. Be careful to explain which piece they are configuring. If configuration to several components is required, break it up into subheadings.

## Start third-party services

This is an optional section that you only need to add if there is some complexity involved. If this is just a simple task, add the steps in the section about starting the example.

## Running the example

Again, an overview of what they will be doing, then the steps.

## Exercising the example

Explain how they can observe what the example is doing.

## Shutting down

Explain how to shut down safely and clean up their environment

## Conclusion

Provide a brief description of what they did, what they learned, and where they can go to learn more.
