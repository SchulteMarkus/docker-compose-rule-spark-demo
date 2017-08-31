# [Docker Compose JUnit Rule Spark demo](https://github.com/SchulteMarkus/docker-compose-rule-spark-demo) [![Build Status](https://travis-ci.org/SchulteMarkus/docker-compose-rule-spark-demo.svg?branch=master)](https://travis-ci.org/SchulteMarkus/docker-compose-rule-spark-demo)
Demonstrating [Docker Compose JUnit Rule](https://github.com/palantir/docker-compose-rule) in a 
[Spark](http://sparkjava.com/) project.

## Challenge

You got a [Java](https://www.java.com/)-microservice, which you want to run using 
[Docker](https://www.docker.com/). You want to integration-test this (your appliation running in 
Docker), using your build-tool [Apache Maven](https://maven.apache.org/) and your test-framework 
[JUnit](http://junit.org/). You are using [Git](https://git-scm.com/).

This testing shall be capable of parallel builds (on different git-branches) at the same time. You want to test exact the 
git-commit-id you are working on, or your CI-system has checked out.

## Demo setup

For this demo, [Spark](http://sparkjava.com/) is used, "A micro framework for creating web 
applications\[...\]". The application is very simple, it serves */hello* as a "Hello World"-endpoint,
see [App.java](src/main/java/schulte/markus/dockercomposerulesparkdemo/App.java).

## Demo usage

**Required**
- [Git](https://git-scm.com/)
- [Maven](https://maven.apache.org/)
- [Docker](https://www.docker.com/)

```bash
# Builds a docker-image for this git-commit-id, integration-tests against a related container
docker-compose-rule-spark-demo $ mvn verify
...
[INFO] DOCKER> [schulte.markus/docker-compose-rule-spark-demo:8379f8a]: Built image sha256:6d570
...
[INFO] Running schulte.markus.dockercomposerulesparkdemo.AppIT
...
[INFO] BUILD SUCCESS
```

## Solution

1. Create a docker-image (service), containing the current state of the application, tagged with the 
git-commit-id
2. Integration-test the service, running in a related docker-container

In this documentation, *8379f8a* is the current git-commit-id
 
### 1. Create docker-images, tagged with git-commit-id

- First of all, you need to create an executable jar for your Spark-application. You can do so by 
using [maven-shade-plugin](https://maven.apache.org/plugins/maven-shade-plugin/), see 
[pom.xml](pom.xml). This way, you wil have a *target/docker-compose-rule-spark-demo.jar*, which you 
can run via `java -jar target/docker-compose-rule-spark-demo.jar`.
```bash
docker-compose-rule-spark-demo $ mvn package
...
[INFO] --- maven-shade-plugin:3.0.0:shade (default) @ docker-compose-rule-spark-demo ---
[INFO] Including com.sparkjava:spark-core:jar:2.6.0 in the shaded jar.
...
[INFO] Replacing original artifact with shaded artifact.
[INFO] Replacing /home/markus-tarent/workspace/docker-compose-rule-spark-demo/target/docker-compose-rule-spark-demo.jar with /home/markus-tarent/workspace/docker-compose-rule-spark-demo/target/docker-compose-rule-spark-demo-shaded.jar

docker-compose-rule-spark-demo $ ls target/ | grep .jar
docker-compose-rule-spark-demo.jar # Executable jar
original-docker-compose-rule-spark-demo.jar
```
- Now you need to have information about your git-commit-id both while maven-building as well as
useable in your later integration-test. For this purpose, you can use 
[Maven git commit id plugin](https://github.com/ktoso/maven-git-commit-id-plugin), see 
[pom.xml](pom.xml). The way this Maven-plugin is used, you will have maven-variables while building,
containing information about git (*${git.commit.id}* for example), as well as a 
*target/classes/git.properties*-file, containing needed information, useable at runtime.
```bash
docker-compose-rule-spark-demo $ mvn compile
...
[INFO] --- git-commit-id-plugin:2.1.9:revision (default) @ docker-compose-rule-spark-demo ---
...

docker-compose-rule-spark-demo $ cat target/classes/git.properties | grep commit.id
git.commit.id.abbrev=8379f8a
git.commit.id=8379f8ae469a71d10c63b875abe643724efd4092
git.commit.id.describe=8379f8a
```
- Have a [Dockerfile](Dockerfile) for building a docker-image for your service.
- Now, build your docker-image as part of `maven package`-phase. For this purpose 
[fabric8io/docker-maven-plugin](https://github.com/fabric8io/docker-maven-plugin) is used, see 
[pom.xml](pom.xml). The way this Maven-plugin is used, a docker-image will be created while 
`maven package`-phase, tagged with the current git-commit-id. Note the required [.maven-dockerignore](.maven-dockerignore).
```bash
docker-compose-rule-spark-demo $ mvn package
...
[INFO] --- docker-maven-plugin:0.21.0:build (default) @ docker-compose-rule-spark-demo ---
[INFO] Building tar: /home/markus-tarent/workspace/docker-compose-rule-spark-demo/target/docker/schulte.markus/docker-compose-rule-spark-demo/8379f8a/tmp/docker-build.tar
[INFO] DOCKER> [schulte.markus/docker-compose-rule-spark-demo:8379f8a]: Created docker-build.tar in 107 milliseconds
[INFO] DOCKER> [schulte.markus/docker-compose-rule-spark-demo:8379f8a]: Built image sha256:ee9c7
[INFO] DOCKER> [schulte.markus/docker-compose-rule-spark-demo:8379f8a]: Removed old image sha256:6d570
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS

docker-compose-rule-spark-demo $ docker images schulte.markus/docker-compose-rule-spark-demo
REPOSITORY                                      TAG                 IMAGE ID            CREATED              SIZE
schulte.markus/docker-compose-rule-spark-demo   8379f8a             f534f2fa3d3e        About a minute ago   83.9MB
```
 
### 2. Integration-test

- [AppIT](src/test/java/schulte/markus/dockercomposerulesparkdemo/AppIT.java) will be your 
integration-test, using [JUnit](http://junit.org/). Don't forget to configure your 
[Maven Failsafe Plugin](https://maven.apache.org/surefire/maven-failsafe-plugin/) for running
while `maven verify`-phase (see [pom.xml](pom.xml)).
- First you have to get information about your git-commit-id. This is extracted from
*target/classes/git.properties*, using 
[GitHelper](src/test/java/schulte/markus/dockercomposerulesparkdemo/GitHelper.java).
- Now, you can finally use [Docker Compose JUnit Rule](https://github.com/palantir/docker-compose-rule)
in your test. This starts the correct spark-hello-world-service, defined in your 
[src/test/resources/docker-compose.yml](src/test/resources/docker-compose.yml). The correct 
version (git-commit-id) is given to the docker-compose.yml, by passing in an environment variable.  
```bash
docker-compose-rule-spark-demo $ mvn verify
...
[INFO] -------------------------------------------------------
[INFO]  T E S T S
[INFO] -------------------------------------------------------
[INFO] Running schulte.markus.dockercomposerulesparkdemo.AppIT
...
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 3.419 s - in schulte.markus.dockercomposerulesparkdemo.AppIT
[INFO] 
[INFO] Results:
[INFO] 
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0
[INFO] 
[INFO] 
[INFO] --- maven-failsafe-plugin:2.20:verify (default) @ docker-compose-rule-spark-demo ---
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS

docker-compose-rule-spark-demo $ docker ps #While AppIT had run
3b953a1958f9        schulte.markus/docker-compose-rule-spark-demo:8379f8a   "java -jar /app.jar"   Less than a second ago   Up Less than a second   0.0.0.0:32771->4567/tcp   bdc647fb_spark-hello-world-service_1
```
