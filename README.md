# [Docker Compose JUnit Rule](https://github.com/palantir/docker-compose-rule) [Spark](http://sparkjava.com/) demo
Demonstrating [Docker Compose JUnit Rule](https://github.com/palantir/docker-compose-rule) in a 
[Spark](http://sparkjava.com/) project

## Challenge

You got a [Java](https://www.java.com/)-microservice, which you want to run using 
[Docker](https://www.docker.com/). You want to integration-test this (your appliation running in 
Docker), using your build-tool [Apache Maven](https://maven.apache.org/) and your test-framework 
[JUnit](http://junit.org/). You are using [Git](https://git-scm.com/).

This testing shall be capable of multiple builds at the same time. You want to test exact the 
git-commit-id you are working on, or your CI-system has checked out. 

## Demo-setup

For this demo, [Spark](http://sparkjava.com/) is used, "A micro framework for creating web 
applications\[...\]". The application is very simple, it serves */hello* as a "Hello Wolrd"-endpoint,
see [App.java](src/main/java/schulte/markus/dockercomposerulesparkdemo/App.java).

## Solution

1. Create a docker-image, containing the current state out the application, tagged with the 
exclusive git-commit-id
2. Integration-test against your application / service, running in this docker-image
 
### 1. Create docker-images, tagged with git-commit-id

- First of all, you need to create an executable jar for your Spark-appliation. You can do so by 
using [maven-shade-plugin](https://maven.apache.org/plugins/maven-shade-plugin/), see 
[pom.xml](pom.xml). This way, you wil have a *target/docker-compose-rule-spark-demo.jar*, which you 
can run via `java -jar target/docker-compose-rule-spark-demo.jar`.
- Now you need to have information about your git-commit-id both while maven-building as well as
useable in your later integration-test. For this purpose, you can use 
[Maven git commit id plugin](https://github.com/ktoso/maven-git-commit-id-plugin), see 
[pom.xml](pom.xml). The way this Maven-plugin is used, you will have maven-variables while building,
containing information about git (${git.commit.id} for example), as well as a 
*target/classes/git.properties*-file, containing needed information, useable at runtime.
- Now, build your docker-image as part of `maven package`-phase. For this purpose 
[fabric8io/docker-maven-plugin](https://github.com/fabric8io/docker-maven-plugin) is used, see 
[pom.xml](pom.xml). The way this Maven-plugin is used, a docker-image will be created while 
`maven package`-phase, tagged with the current git-commit-id.
 
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
