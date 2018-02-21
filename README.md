# Geocoder

[![Build Status](https://travis-ci.org/Panthro/geocoder.svg?branch=master)](https://travis-ci.org/Panthro/geocoder)


## Running

```bash
export GOOGLE_API_KEY=<your_api_key>
mvn clean spring-boot:run
```

1. Open [Swagger UI](http://petstore.swagger.io/)
2. Use the url `http://localhost:8080/camel/api-docs` in the __Explore__ box
3. Try out the `/geocode` api. 


## Testing

```bash
export GOOGLE_API_KEY=<your_api_key>
mvn clean verify
```


## Building

The code is already tested and built with Travis-CI, but of course for a production like environment 
running in a PaaS it would be better to have a docker already built with the CI, so there is a [DockerFile](./src/main/docker/DockerFile) 
with some known tricks to execute in a lightweight environment and with `JAVA_OPTS` extension points to hack the memory problem java has inside containers


Also the `docker-maven-plugin` from [Spotify](https://github.com/spotify/docker-maven-plugin) is setup so you can use the command below on the CI to build the docker file:
```bash
    mvn clean package docker:build
``` 


### Spring Controller vs Camel Route

As you may notice, there are 2 implementations of the Rest api:

- [GeocodeApiRoute](./src/main/java/com/geocoding/geocoder/routes/GeocodeApiRoute.java): `/camel/geocode` This one is based only on apache camel, without any spring in the middle. I thought it would be fun to have camel as a _proxy_ to just make everything work with a single class.
- [GeocodeController](./src/main/java/com/geocoding/geocoder/rest/GeocodeController.java):  `/api/geocode` A spring-web `@RestController` mapping the endpoint to the actual camel route.

__Note:__ Only the `GeocodeApiRoute` has __swagger__ mapping, swagger on a `@RestController` is a trivial thing.
 

## Expectation vs Reality

I know you were expecting a mapped POJO on the output of the JSON instead of a "proxy hack", but I didn't have that much of time to build it and decided to spend the time in the fun part.


### Pending
For this to be a real microservice there are a lot of "missing parts", but it depends on the env it would be deployed.

- Add spring actuator depending on your environment
    - Totally depending on the environment, usually is added, but might be an overhead on the initialization if not needed
- Add central configuration (Spring cloud config, consul KV store, etc)
    - It is more a matter of architectural choice than implementation of this microservice, usually there's an architecture library/parent that all MS inherit this things already configured
- Connect to a discovery service (eureka, consul, etc) using one of the spring starters
    - But depending on the environment this is not needed. For isntance in a real PASS (like openshift or even rancher) you can use their own DNS, but with the drawback to lose the client side load balancing (Ribbon)
- Better logging: Configuration and centralization, either with an aspect and a logback configuration to write json logs, or directly with a log collector (fluentd, logstash, etc)
 

