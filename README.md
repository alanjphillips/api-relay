# api-relay
Trying out various public APIs using Akka Http Streams in Scala. Focusing on experimenting with these tools for now

-- TO BUILD A DOCKER IMAGE
> sbt docker:publishLocal

-- FIND LOCAL IMAGE AND RUN CONTAINER
> docker images
> docker run api-relay:1.0

-- TO GET CONTAINER IP ADDRESS
> docker inspect <CONTAINER_ID> 

GET http://<CONTAINER_IP>:9000/uber/products
