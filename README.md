Neuron DI is an ultra-light dependency injection framework which emphasizes
the beauty and power of lazy dependency resolution and caching.
It takes advantage of functional programming features available in Java SE 8 and 
Scala 2.11 in order to provide customized APIs for these target environments. 

Neuron DI can be used standalone or embedded in other DI frameworks, e.g. 
Guice.
When using it standalone, programmers enjoy a simple, yet versatile domain
specific language (DSL) for writing self-contained binding definitions.
Thanks to the (Java or Scala) compiler, self-contained binding definitions 
alleviate the need for testing them. 

When using Neuron DI embedded, programmers can extend their familiar DI
framework with lazy dependency resolution and caching.
Guice users also enjoy extensions to the Guice binding DSL which make it simpler 
to use, especially for Scala.

Neuron DI is covered by the [Apache License, Version 2.0].

Release artifacts are hosted on Maven Central. 
Assuming a system property is defined for `neuron-di.version` (see [releases]), 
the Maven coordinates for the standalone Neuron DI artifact are as follows:

    <dependency>
        <groupId>de.schlichtherle</groupId>
        <artifactId>neuron-di</artifactId>
        <version>${neuron-di.version}</version>
    </dependency>

If you want to embed Neuron DI in Guice, you have to add this:

    <dependency>
        <groupId>de.schlichtherle</groupId>
        <artifactId>neuron-di-guice</artifactId>
        <version>${neuron-di.version}</version>
    </dependency>

[Apache License, Version 2.0]: https://www.apache.org/licenses/LICENSE-2.0
[releases]: https://github.com/christian-schlichtherle/neuron-di/releases
