[![Build Status](https://travis-ci.org/christian-schlichtherle/neuron-di.svg?branch=master)](https://travis-ci.org/christian-schlichtherle/neuron-di)

# Neuron DI

Neuron DI is an ultra-light dependency injection framework which emphasizes the beauty and power of type-safe, lazy 
dependency resolution with optional compile-time checking - see [Features and Benefits].
It takes advantage of functional programming features in Java SE 8 and Scala 2.11 in order to provide an API with a 
simple, yet complete Domain Specific Language for each target environment. 

Neuron DI can be used standalone or combined with other DI frameworks.
When using it standalone, programmers enjoy a simple, yet complete domain specific language (DSL) for writing
self-contained bindings.
Self-contained bindings are checked by the (Java or Scala) compiler, which alleviates the need for testing 
them separately in many cases. 

When combining Neuron DI with other DI frameworks, programmers can increase the power and flexibility of their framework
of choice with lazy dependency resolution, caching and dependency injection into Java interfaces or Scala traits.
Users of Guice also enjoy extensions to the Guice binding DSL which make it simpler to use, especially when using Guice 
with Scala.

For documentation, please consult the Wiki:

- [Documentation Wiki][Wiki]

Release Notes are available on GitHub:

- [![Release Notes](https://img.shields.io/github/release/christian-schlichtherle/neuron-di.svg?maxAge=3600)](https://github.com/christian-schlichtherle/neuron-di/releases/latest)

Release artifacts are deployed to [Maven Central](https://search.maven.org/#search%7Cga%7C1%7Cg%3A%22global.namespace.neuron-di%22):

- [![Neuron DI for Java](https://img.shields.io/maven-central/v/global.namespace.neuron-di/neuron-di.svg?label=Neuron%20DI%20for%20Java&maxAge=3600)](https://search.maven.org/#search%7Cga%7C1%7Cg%3A%22global.namespace.neuron-di%22%20AND%20a%3A%22neuron-di%22)
- [![Neuron DI for Scala 2.11](https://img.shields.io/maven-central/v/global.namespace.neuron-di/neuron-di-scala_2.11.svg?label=Neuron%20DI%20for%20Scala%202.11&maxAge=3600)](https://search.maven.org/#search%7Cga%7C1%7Cg%3A%22global.namespace.neuron-di%22%20AND%20a%3A%22neuron-di-scala_2.11%22)
- [![Neuron DI for Scala 2.12](https://img.shields.io/maven-central/v/global.namespace.neuron-di/neuron-di-scala_2.12.svg?label=Neuron%20DI%20for%20Scala%202.12&maxAge=3600)](https://search.maven.org/#search%7Cga%7C1%7Cg%3A%22global.namespace.neuron-di%22%20AND%20a%3A%22neuron-di-scala_2.12%22)
- [![Neuron DI @ Guice for Java](https://img.shields.io/maven-central/v/global.namespace.neuron-di/neuron-di-guice.svg?label=Neuron%20DI%20@%20Guice%20for%20Java&maxAge=3600)](https://search.maven.org/#search%7Cga%7C1%7Cg%3A%22global.namespace.neuron-di%22%20AND%20a%3A%22neuron-di-guice%22)
- [![Neuron DI @ Guice for Scala 2.11](https://img.shields.io/maven-central/v/global.namespace.neuron-di/neuron-di-guice-scala_2.11.svg?label=Neuron%20DI%20@%20Guice%20for%20Scala%202.11&maxAge=3600)](https://search.maven.org/#search%7Cga%7C1%7Cg%3A%22global.namespace.neuron-di%22%20AND%20a%3A%22neuron-di-guice-scala_2.11%22)
- [![Neuron DI @ Guice for Scala 2.12](https://img.shields.io/maven-central/v/global.namespace.neuron-di/neuron-di-guice-scala_2.12.svg?label=Neuron%20DI%20@%20Guice%20for%20Scala%202.12&maxAge=3600)](https://search.maven.org/#search%7Cga%7C1%7Cg%3A%22global.namespace.neuron-di%22%20AND%20a%3A%22neuron-di-guice-scala_2.12%22)

Neuron DI is covered by the Apache License, Version 2.0:

- [![Apache License 2.0](https://img.shields.io/github/license/christian-schlichtherle/neuron-di.svg?maxAge=3600)](https://www.apache.org/licenses/LICENSE-2.0)

[Wiki]: https://github.com/christian-schlichtherle/neuron-di/wiki
[Features and Benefits]: https://github.com/christian-schlichtherle/neuron-di/wiki/Features
