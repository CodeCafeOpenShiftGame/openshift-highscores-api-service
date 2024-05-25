# High Scores API Service

An API service that showcases how [Quarkus](https://quarkus.io/) can make it easy to expose REST endpoints and tie them to MongoDB. This project also shows other benefits we get from Quarkus like hot deploys, active-record like ORM access, service health checks, and easy containerization/deployment on OpenShift.

I initially started designing this API with [Apicurito](https://github.com/Apicurio)) and exported that as an OpenAPI specification. Then after I coded my app, I now let Quarkus auto-create that specification and swagger-webpage for me on the fly.

![Screenshot](.screens/terminalshot.png)

## Running this Locally
You can try this on you local machine by just starting MongoDB and then starting the API service. The 3 commands below do that.

Start a local mongoDB server with:
>`mkdir -p ./data/db`
>`mongod --dbpath ./data/db` 

Run the api service in dev mode that enables live coding using:
>`./mvnw quarkus:dev`


## Deploying to OpenShift 
### Deploying from local code to OpenShift
*(you will need to specify config for the app to talk to mongodb, you can do that with application.properties or with env vars)*

(*if your dev env is linux, and you want to deploy the native executable, switch the `Dockerfile.jvm` to `Dockerfile.native`*)

1) Build things:
    >`mvn package`
    >
    >`oc new-build --binary --name=highscores-api-service -l app=highscores-api-service`
    >
    >`oc patch bc/highscores-api-service -p "{\"spec\":{\"strategy\":{\"dockerStrategy\":{\"dockerfilePath\":\"src/main/docker/Dockerfile.jvm\"}}}}"`
    >
    >`oc start-build highscores-api-service --from-dir=. --follow`

2) Create OpenShift/Kubernetes resources:
    >`oc new-app --image-stream=highscores-api-service:latest -e CHECKSUM_SECRET=somethingsecret -e QUICKAUTH_USER=changeme -e QUICKAUTH_PASSWORD=changeme`

3) Expose access to outside of cluster
    >`oc expose service highscores-api-service`
    >
    >`export HS_URL="http://$(oc get route | grep highscores-api-service | awk '{print $2}')"`
    >
    >`curl $HS_URL && echo`

4) This service won't function until it can store its data into a MongoDB. We can easily deploy one on OpenShift and have OpenShift provide service discovery. And then we configure this app's deployment with the user/password details for connecting to the DB.
    > `oc new-app -e MONGODB_USER=thisisauser -e MONGODB_PASSWORD=thisis4password -e MONGODB_DATABASE=highscores -e MONGODB_ADMIN_PASSWORD=thisis4password mongo:latest`
    >
    > `oc set env dc/highscores-api-service QUARKUS_MONGODB_CONNECTION_STRING=mongodb://thisisauser:thisis4password@mongodb:27017/highscores`


### Configuration (env and app properties)
There are several env variables you can (and should) use to configure this at runtime. You can also set these via the application.properties file.
* QUICKAUTH_ENFORCING, to turn on/off basic auth on the POST
* CHECKSUM_ENFORCING, to turn on/off the checksum on the POST
* QUARKUS_MONGODB_CONNECTION_STRING, the details of where and how to connect to the database
* QUICKAUTH_USER, user for basic auth
* QUICKAUTH_PASSWORD, pass for basic auth
* CHECKSUM_SECRET, secret using in validating our POST data

## Testing
I like to use a nice CLI tool called HTTPie. If you have it below are some useful commands.

### Testing POST with basic auth turned on
```
http -a dudash:123456 POST http://localhost:5000/scores score=1000 name=JAS
```

### Testing POST with basic auth and checksums turned on
```
http -a dudash:123456 POST http://localhost:5000/scores score=1000 name=JAS checksum=d1791337d1e50340bc9b78d2b0d34c2740158d1e
```

## Other Notes
### The POST route for scores now has basic auth enabled on it by default
* So you will need to pass username/password in to POST scores
* You can disable it with an environment variable (see the application.properties file)

### The POST route for scores now has a checksum required by default
* So if you have a legacy client it should be updated
* You can disable the check with an environment variable (see the application.properties file)


### There are other ways to deploy this to OpenShift (alternative - deploying from GitHub)
*(note: you will need to specify config for the app to talk to mongodb, you can do that with application.properties or with env vars)*

1) [Build things](https://quarkus.io/guides/deploying-to-openshift-s2i) (it needs a lot of memory):
    >`oc new-app quay.io/quarkus/ubi-quarkus-native-s2i:19.3-java8~https://github.com/CodeCafeOpenShiftGame/openshift-highscores-api-service.git --name=highscores-api-service -e CHECKSUM_SECRET=somethingsecret -e QUICKAUTH_USER=changeme -e QUICKAUTH_PASSWORD=changeme`
    >
    >`oc patch bc/highscores-api-service -p '{"spec":{"resources":{"limits":{"cpu":"4", "memory":"6Gi"}}}}'`
    >
    >`oc start-build highscores-api-service`
    >
    >`oc logs -f bc/highscores-api-service`

2) Expose access to outside of cluster
    >`oc expose service highscores-api-service`
    >
    >`export HS_URL="http://$(oc get route | grep highscores-api-service | awk '{print $2}')"`
    >
    >`curl $HS_URL && echo`

3) This service won't function until it can store its data into a MongoDB. We can easily deploy one on OpenShift and have OpenShift provide service discovery. And then we configure this app's deployment with the user/password details for connecting to the DB.
    > `oc new-app -e MONGODB_USER=thisisauser -e MONGODB_PASSWORD=thisis4password -e MONGODB_DATABASE=highscores -e MONGODB_ADMIN_PASSWORD=thisis4password mongo:latest`
    >
    > `oc set env deployment/highscores-api-service QUARKUS_MONGODB_CONNECTION_STRING=mongodb://thisisauser:thisis4password@mongodb:27017/highscores`



## Thanks and Credit
This service was built based on guidance from the [Quarkus example here](https://quarkus.io/guides/openapi-swaggerui#loading-openapi-schema-from-static-files).
