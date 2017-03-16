# Jenkins SCM API Plugin

 This plugin provides a new enhanced API for interacting with SCM systems. See also this [plugin's wiki page][wiki]
 
 If you are writing a plugin that implements this API, please see [the implementation guide](docs/implementation.adoc)
 
 If you are writing a plugin that consumes this API, please see [the consumer guide](docs/consumer.adoc)

# Environment

The following build environment is required to build this plugin

* `java-1.6` and `maven-3.0.5`

# Build

To build the plugin locally:

    mvn clean verify

To build the plugin using Docker:

    docker build .

# Release

To release the plugin:

    mvn release:prepare release:perform -B

# Test local instance

To test in a local Jenkins instance

    mvn hpi:run

  [wiki]: http://wiki.jenkins-ci.org/display/JENKINS/SCM+API+Plugin
