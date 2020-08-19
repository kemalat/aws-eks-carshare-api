# Car Share API

### Notes
- See ... for prerequisites to perform successful deployment 
- Gradle used as build automation system
- Docker image is created with Gradle using plugin and Dockerfile provided
- For easy testing, H2 embedded SQL database used 

### Technology stack

- openjdk 11, Docker, AWS Elastic Managed Kubernetes Service(EKS)
- spring boot 2.3.x, gradle 6.3, Lombok 1.18, log4j 2.x

### Todo
- Storing userId in ride plan table instead of username
- Implementing OAuth2 web service to service API security
- Cancelling ride share reservation by passenger
- Accept/Deny reservation for ride share offering party
- Editing ride share offer properties
- Writing unit tests
- Performing load tests

## Naming Conventions

- service name: rideshare-service-h2
- package name: com.rideshare
- gradle's project name: rideshare-service
    - jar name: build/libs/rideshare-service-0.0.1-SNAPSHOT.jar
- deployment name in k8s: rideshare-service
- url format: /ride-share/plans, ride-share/{rideId}/publish, /ride-share/proposed-plans
    
### OOP Architecture

- Ride, Passenger,City as Entity
- RidePlanRepository
    - RidePlanService (as JPA cache layer or call APIs)
        - RidePlanController (as REST APIs)
- PassengerRepository
    - RidePlanService (as JPA cache layer or call APIs)
        - RidePlanController (as REST APIs)
- CityRepository
    - RidePlanService (as JPA cache layer or call APIs)
        - RidePlanController (as REST APIs)

## How To Start

**On development environment**, 


1. to Run the application <br>
   * `./gradlew bootRun` to start,
   * `./gradlew bootJar` to create jar lib, and run `java -jar build/libs/*.jar`
2. to test run `curl localhost:8080`
   * `curl localhost:8083/dummy` or `curl localhost:8083/dummy/name`
   * `curl localhost:8083/ride-share/proposed-plans`
3. to access h2-db check `localhost:8083/h2`  with jdbc string `jdbc:h2:./data`

**On AWS Kubernetes**,

1. Prepare Docker images 
    - Edit `gradle.properties` to change `dockerHubUsername`
    - Edit `settings.gradle` to change `rootProject.name`
    - Run `./gradlew dockerTag`. 
    - List  `docker images` and find image created 
    - Run docker images to test `docker run -d -p 8083:8083 IMAGE_NAME:TAG`
       - `e.g. $ docker run -d -p 8083:8083 kemalat/rideshare-service-h2:0.0.1-SNAPSHOT`

2. Authenticate your Docker client to AWS ECR registry.
    - Run `aws ecr get-login --no-include-email`. 
    - Run docker login command returned by `get-login ` 
       - `docker login -u AWS -p [password_string] https://[aws_account_id].dkr.ecr.us-east-2.amazonaws.com`

3. Create a repository to store docker image, Tag the container images and Push to ECR
    - Run `aws ecr create-repository --repository-name kemalat/rideshare-service-h2`
    - Get `repositoryUri` from the returned JSON output
    - Run `docker tag kemalat/rideshare-service-h2:0.0.1-SNAPSHOT [AWS-Account-ID].dkr.ecr.us-east-2.amazonaws.com/kemalat/rideshare-service-h2`
    - Run `docker push [AWS-Account-ID].dkr.ecr.us-east-2.amazonaws.com/kemalat/rideshare-service-h2`
   
4. Create EKS Cluster, Deploy and start service
    - Run `eksctl create cluster -f cluster.yaml` to create K8 cluster on AWS managed EKS environment
    - Change `image` in deployment.yaml and run deploy `kubectl create -f deployment.yaml`
    - Run `kubectl create -f service.yaml` to start service

5. Test and verify the deployment
    - Run `kubectl get pods` to get pod name in running status (e.g. `rideshare-service-h2-58fbf44545-f22qp`)
    - Check logs with command `kubectl logs rideshare-service-h2-58fbf44545-f22qp`
    - Run `kubectl get service rideshare-service-h2` to get EXTERNAL-IP then `curl EXTERNAL-IP:8083/dummy` to test.

6. Delete K8 service and terminate EKS cluster to prevent incurring the costs.
    - Run `kubectl get svc --all-namespaces` to get service name which has the EXTERNAL-IP
    - Run `kubectl delete svc rideshare-service-h2` to delete service 
    - Run `eksctl delete cluster --name rideshare-cluster`

## How to Test 

Curl scripts are provided for testing provided APIs. City matrix map should be created in advance
before starting the API tests.

### /ride-share/create-map

```
curl --location --request POST 'localhost:8083/ride-share/create-map' \
--header 'Content-Type: application/json' \
--data-raw '{
  "row" : 3,
  "column" : 3
}'
```

### /ride-share/plans

```
curl --location --request POST 'localhost:8083/ride-share/plans' \
--header 'Content-Type: application/json' \
--data-raw '{
              "departure" : "City-1",
              "arrival" : "City-4",
              "userName" : "kemal",
              "rideDate" : "2019-05-28T17:39:44.937",
              "details" : "4 person trip from City-1 to City-4",
              "availSeat" : 4
            }'
```

### /ride-share/{rideId}/publish

```
curl --location --request PUT 'localhost:8083/ride-share/1/publish'
```

### /ride-share/{rideId}/unpublish

```
curl --location --request PUT 'localhost:8083/ride-share/1/unpublish'
```

### /ride-share/proposed-plans?departure=City-3&arrival=City-1

```
curl --location --request GET 'localhost:8083/ride-share/proposed-plans?departure=City-3&arrival=City-1'
```

### ride-share/join

```
curl --location --request POST 'http://127.0.0.1:8081/api/ride/join' \
--header 'Content-Type: application/json' \
--data-raw '{
  "rideId" : 2,
  "userName" : "Rider"
}'
```





