# api-relay
Trying out various public APIs using Akka Http Streams in Scala. Focusing on experimenting with these tools for now


OPTION 1: TO BUILD A DOCKER IMAGE
> sbt docker:publishLocal

-- FOR REFERENCE, THE DOCKER FILE AND OPT FOLDER CONTAINING APP CAN BE FOUND UNDER
target/docker/stage

-- FIND LOCAL IMAGE AND RUN CONTAINER LOCALLY
> docker images
> docker run api-relay:1.0

-- TO GET CONTAINER IP ADDRESS
> docker inspect CONTAINER_ID 
GET http://CONTAINER_IP:9000/uber/products


OPTION 2: TO BUILD ZIP CONTAINING START SCRIPT AND LIBS
> sbt universal:packageBin

-- APP PACKAGED IN ZIP CAN BE FOUND UNDER
/target/universal