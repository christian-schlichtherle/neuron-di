# README

Neuron DI is an ultra-light dependency injection framework which emphasizes
the beauty and power of lazy dependency resolution and caching.
It takes advantage of functional programming features available in Java SE 8 and 
Scala 2.11 in order to provide tailor-made APIs for these target environments. 

Neuron DI can be used standalone or embedded in other DI frameworks, e.g. 
Guice.
When using it standalone, programmers enjoy a simple, yet complete domain
specific language (DSL) for writing self-contained binding definitions.
Self-contained binding definitions are checked by the (Java or Scala) compiler, 
which alleviates the need for testing them separately in many cases. 

When using Neuron DI embedded, programmers can add lazy dependency resolution 
and caching to their DI framework of choice.
Guice users also enjoy extensions to the Guice binding DSL which make it simpler 
to use, especially when using Guice with Scala.

For documentation, please consult the [Wiki].

Neuron DI is covered by the [Apache License, Version 2.0].

Release artifacts are hosted on [Maven Central](https://search.maven.org/#search%7Cga%7C1%7Cg%3A%22global.namespace.neuron-di%22). 

## Dependencies for the Java API

Assuming a system property is defined for `neuron-di.version` (see [releases]), 
the Maven coordinates for the standalone Neuron DI artifact are as follows:

    <dependency>
        <groupId>global.namespace.neuron-di</groupId>
        <artifactId>neuron-di</artifactId>
        <version>${neuron-di.version}</version>
    </dependency>

If you want to embed Neuron DI in Guice, you have to use this instead:

    <dependency>
        <groupId>global.namespace.neuron-di</groupId>
        <artifactId>neuron-di-guice</artifactId>
        <version>${neuron-di.version}</version>
    </dependency>

## Dependencies for the Scala API

Assuming a system property is defined for `neuron-di.version` (see [releases]) and `scala.compat.version`, 
the Maven coordinates for the standalone Neuron DI artifact are as follows:

    <dependency>
        <groupId>global.namespace.neuron-di</groupId>
        <artifactId>neuron-di_${scala.compat.version}</artifactId>
        <version>${neuron-di.version}</version>
    </dependency>

If you want to embed Neuron DI in Guice, you have to use this instead:

    <dependency>
        <groupId>global.namespace.neuron-di</groupId>
        <artifactId>neuron-di-guice_${scala.compat.version}</artifactId>
        <version>${neuron-di.version}</version>
    </dependency>

[Apache License, Version 2.0]: https://www.apache.org/licenses/LICENSE-2.0
[Releases]: https://github.com/christian-schlichtherle/neuron-di/releases
[Wiki]: https://github.com/christian-schlichtherle/neuron-di/wiki
