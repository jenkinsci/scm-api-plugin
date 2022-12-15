# Jenkins SCM API Plugin

 This plugin provides a new enhanced API for interacting with SCM systems.

 If you are writing a plugin that implements this API, please see [the implementation guide](docs/implementation.adoc)

 If you are writing a plugin that consumes this API, please see [the consumer guide](docs/consumer.adoc)

# Build

To build the plugin locally:

    mvn clean verify

# Test local instance

To test in a local Jenkins instance

    mvn hpi:run
