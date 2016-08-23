*ARCHIVED*: NOT IN DEVELOPMENT

This project has moved to https://github.com/spring-cloud/spring-cloud-contract

Accurest
========

[![Build Status](https://travis-ci.org/Codearte/accurest.svg?branch=master)](https://travis-ci.org/Codearte/accurest) [![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.codearte.accurest/accurest-gradle-plugin/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.codearte.accurest/accurest-gradle-plugin)
[![Join the chat at https://gitter.im/Codearte/accurest](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/Codearte/accurest?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

Consumer Driven Contracts verifier for Java

To make a long story short - Accurest is a tool for Consumer Driven Contract (CDC) development. Accurest ships an easy DSL for describing REST contracts for JVM-based applications.
 Since version 1.0.7 it also supports messaging.
 
The contract DSL is used by Accurest for two things:

1. generating WireMock's JSON stub definitions / stubbed messaging endpoints, allowing rapid development of the consumer side,
generating JUnit / Spock's acceptance tests for the server - to verify if your API implementation is compliant with the contract.
2. moving TDD to an architecture level.

For more information please go to the [Documentation](http://codearte.github.io/accurest/)

## Requirements

### Wiremock

In order to use Accurest with Wiremock you have to have __Wiremock in version at least 2.0.0-beta__ . Of course the higher the better :)

## Additional projects

### Stub Runner

Allows you to download WireMock stubs from the provided Maven repository and runs them in WireMock servers.

### Stub Runner JUnit

Stub Runner with JUnit rules

### Stub Runner Spring

Spring Configuration that automatically starts stubs upon Spring Context build up

### Stub Runner Spring Cloud

Spring Cloud AutoConfiguration that automatically starts stubs upon Spring Context build up and allows you to call the stubs
as if they were registered in your service discovery

### [Accurest Maven Plugin](https://github.com/Codearte/accurest-maven-plugin)

Maven project support with standalone Accurest Stub Runner and Accurest Contracts to Wiremock mappings converter
