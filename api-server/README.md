coepi-backend-aws/api-server
============================

A standalone frontend for the CoEpi Cloud API, suitable for deploying to Kubernetes or any other containerized solution.

## Getting Started

The API server can be run locally from Gradle. Note you will need to have AWS configured correctly so that the service can access the DynamoDB table from your local machine.

```shell script
./gradlew api-server:run
```

## Deploy

This project uses [jib](https://github.com/GoogleContainerTools/jib) for containerization, which makes it easy to build and deploy images without necessarily needing Docker installed.

### Publishing to an Image Repository

To publish to an image repository, use the `jib` task:

```shell script
./gradlew api-server:jib -P jib.to.image=YOUR_REPO_NAME/coepi-cloud-api
```

## Running Locally in Docker 

Jib can also build an image to your local Docker daemon so you can try it out locally. Note that this requires you have Docker installed on your machine.

```shell script
./gradlew api-server:jibDockerBuild
```

This will build an image and tag it `coepi-cloud-api`. To run locally, you'll need to pass it your AWS credentials:

```shell script
docker run -p 8080:8080 \
  -e AWS_ACCESS_KEY=<your access key> \
  -e AWS_SECRET_ACCESS_KEY=<your secret access key> \
  -e AWS_REGION=us-west-1 \
  coepi-cloud-api
``` 

