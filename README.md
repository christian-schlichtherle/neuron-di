# Neuron DI

[![Build Status](https://travis-ci.org/christian-schlichtherle/neuron-di.svg?branch=master)](https://travis-ci.org/christian-schlichtherle/neuron-di)

Neuron DI is an ultra-light dependency injection framework which emphasizes the beauty and power of _lazy dependency 
resolution_ and _caching_.
It takes advantage of functional programming features available in Java SE 8, Scala 2.11 and Scala 2.12 in order to
provide a simple, yet versatile API for these target environments. 

Neuron DI can be used standalone or embedded in other DI frameworks, e.g. in Guice 3.0+.
When using it standalone, programmers enjoy a simple, yet complete domain specific language (DSL) for writing 
self-contained binding definitions.
Self-contained binding definitions are checked by the (Java or Scala) compiler, which alleviates the need for testing 
them separately in many cases. 

When using Neuron DI embedded, programmers can add lazy dependency resolution and caching to their DI framework of 
choice.
Guice users also enjoy extensions to the Guice binding DSL which make it simpler to use, especially when using Guice 
with Scala.

For documentation, please consult the Wiki:

- [Documentation][Wiki]

Release Notes are available on GitHub:

- [![Release Notes](https://img.shields.io/github/release/christian-schlichtherle/neuron-di.svg?maxAge=3600)](https://github.com/christian-schlichtherle/neuron-di/releases)

Release artifacts are hosted on [Maven Central](https://search.maven.org/#search%7Cga%7C1%7Cg%3A%22global.namespace.neuron-di%22):

- [![Neuron DI for Java](https://img.shields.io/maven-central/v/global.namespace.neuron-di/neuron-di.svg?label=Neuron%20DI%20for%20Java&maxAge=3600)](https://search.maven.org/#search%7Cga%7C1%7Cg%3A%22global.namespace.neuron-di%22%20AND%20a%3A%22neuron-di%22)
- [![Neuron DI for Scala 2.11](https://img.shields.io/maven-central/v/global.namespace.neuron-di/neuron-di-scala_2.11.svg?label=Neuron%20DI%20for%20Scala%202.11&maxAge=3600)](https://search.maven.org/#search%7Cga%7C1%7Cg%3A%22global.namespace.neuron-di%22%20AND%20a%3A%22neuron-di-scala_2.11%22)
- [![Neuron DI for Scala 2.12.0-RC1](https://img.shields.io/maven-central/v/global.namespace.neuron-di/neuron-di-scala_2.12.0-RC1.svg?label=Neuron%20DI%20for%20Scala%202.12.0-RC1&maxAge=3600)](https://search.maven.org/#search%7Cga%7C1%7Cg%3A%22global.namespace.neuron-di%22%20AND%20a%3A%22neuron-di-scala_2.12.0-RC1%22)
- [![Neuron DI @ Guice for Java](https://img.shields.io/maven-central/v/global.namespace.neuron-di/neuron-di-guice.svg?label=Neuron%20DI%20@%20Guice%20for%20Java&maxAge=3600)](https://search.maven.org/#search%7Cga%7C1%7Cg%3A%22global.namespace.neuron-di%22%20AND%20a%3A%22neuron-di-guice%22)
- [![Neuron DI @ Guice for Scala 2.11](https://img.shields.io/maven-central/v/global.namespace.neuron-di/neuron-di-guice-scala_2.11.svg?label=Neuron%20DI%20@%20Guice%20for%20Scala%202.11&maxAge=3600)](https://search.maven.org/#search%7Cga%7C1%7Cg%3A%22global.namespace.neuron-di%22%20AND%20a%3A%22neuron-di-guice-scala_2.11%22)
- [![Neuron DI @ Guice for Scala 2.12.0-RC1](https://img.shields.io/maven-central/v/global.namespace.neuron-di/neuron-di-guice-scala_2.12.0-RC1.svg?label=Neuron%20DI%20@%20Guice%20for%20Scala%202.12.0-RC1&maxAge=3600)](https://search.maven.org/#search%7Cga%7C1%7Cg%3A%22global.namespace.neuron-di%22%20AND%20a%3A%22neuron-di-guice-scala_2.12.0-RC1%22)

Neuron DI is covered by the Apache License, Version 2.0:

- [![Apache License 2.0](https://img.shields.io/github/license/christian-schlichtherle/neuron-di.svg?maxAge=3600)](https://www.apache.org/licenses/LICENSE-2.0)

[Wiki]: https://github.com/christian-schlichtherle/neuron-di/wiki
