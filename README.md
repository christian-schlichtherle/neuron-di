[![Apache License 2.0](https://img.shields.io/github/license/christian-schlichtherle/neuron-di.svg?maxAge=3600)](https://www.apache.org/licenses/LICENSE-2.0)
[![Build Status](https://api.travis-ci.org/christian-schlichtherle/neuron-di.svg)](https://travis-ci.org/christian-schlichtherle/neuron-di)

# Neuron DI

As a Java developer, does Spring or Guice give you a headache?
Maybe you are tired of annotation frenzy or slow startup times for application and test code?  

As a Scala developer, do you frown upon the Cake pattern?
Maybe you are looking for something simple, yet scalable while retaining compile-time dependency checking?

Neuron DI is a tiny library for dependency injection in Java and Scala which helps you structure your application or 
library code with ease, whether it's small or large.
It takes a radically different approach to the problem than any JSR 330 based framework (Spring, Guice, ...).

## Features

Neuron DI provides the following features:

- dependency injection into abstract methods without parameters, called _synapse methods_
- lazy resolution of dependencies
- caching of dependencies: not-thread-safe, thread-safe or thread-local
- looking up dependencies in any object by delegation
- dependency checking at compile-time (Scala only)
- dependency injection into third-party code, e.g. `java.util.function.Supplier`
- peaceful coexistence with any other DI framework/library

Neuron DI **frees** your code from the following **pains**:

- constructor injection
- method injection
- field injection
- qualifier annotations, e.g. @Named
- scope annotations, e.g. @Singleton
- application contexts
- tight coupling with a DI framework/library

## Example

### About Neurons And Synapses

Consider the following sample code, taken from [Neuron DI Examples for Java]:

```java
import java.util.*;
import java.util.function.BiFunction;

public interface GreetingService extends BiFunction<List<Locale>, Optional<String>, String> {

    Locale defaultLocale();

    Map<Locale, List<String>> greetingMessages();

    default String apply(List<Locale> languageRanges, Optional<String> who) {
        // Some code calling `defaultLocale()` and `greetingMesssages()`
        [...]
    }
}
```

`GreetingService` has two abstract methods without parameters: `defaultLocale()` and `greetingMessages()`.
These methods return dependencies of the code in the `apply([...])` method (not shown here).
In production code, you should apply the interface segregation principle and move the implementation to another class or 
interface (again, not shown here).

In Neuron DI, abstract methods without parameters are called _synapse methods_ and their enclosing types are called
_neuron classes_ or _neuron interfaces_.
This concept is entirely abstract:
As you can see there is no dependency of the `GreetingService` interface on the API of Neuron DI.

One of the benefits of this approach is that synapse methods not only give the returned dependency a location and a 
(return) type, but also a (method) _name_.
This is why Neuron DI doesn't need any qualifier annotations such as `@Named`.    

Another benefit is that dependency resolution is inherently _lazy by design_, but may be _eager by choice_:
When a neuron is constructed, you get to choose if you want to delegate each call to a synapse method to another method 
in another object (lazy dependency resolution) or if you simply want to return a pre-computed value (eager dependency 
resolution) - see below for an example.

Following is an HTTP controller which depends on a `GreetingService`:

```java
import example.web.framework.HttpController;

public interface GreetingController extends HttpController {

    GreetingService greetingService();

    default int get() throws Exception {
        var g = new Greeting();
        g.message = greetingService().apply(acceptLanguages(), requestParam("who"));
        applicationJson().encode(g);
        return 200;
    }
}
```

As you can see, the implementation of the `get()` method depends on the return value of the synapse method 
`greetingService()`. 

### About Modules and the Incubator

In a large code base, you will have many neuron classes and interfaces and you will need to wire them into a dependency 
graph somewhere.
This is where the module pattern comes into place:
A module bundles a group of factory methods for application components which may depend on each other into a single 
object.

In design pattern parlance, the module pattern is a blend of the factory pattern and the mediator pattern because a 
module not only creates (and optionally caches) application components, but may also delegate back to itself whenever 
some of their dependencies need to get resolved.

Consider the following sample code:

```java
import global.namespace.neuron.di.java.Caching;
import global.namespace.neuron.di.java.Neuron;

import java.util.*;

import static global.namespace.neuron.di.java.Incubator.wire;

@Neuron
abstract class Module {

    private static final Locale defaultLocale = Locale.ENGLISH;

    private static final Map<Locale, List<String>> greetingMessages = Map.of(
            ENGLISH, List.of("Hello, %s!", "world"),
            GERMAN, List.of("Hallo, %s!", "Welt")
    );

    @Caching
    GreetingService greetingService() {
        return wire(GreetingService.class).using(this);
    }
}
```

The factory method in this case is `greetingService()`:
Its implementation calls the static method `wire([...])` in the class `Incubator` from the API of Neuron DI.
The term `wire(GreetingService.class).using(this)` specifically tells the incubator to make a `GreetingService` and look 
up any of its dependencies in `this` module object.
This technique is called _dependency delegation_.
There are other options to wire a neuron, but dependency delegation is easy to use, yet extremely versatile.

When looking up dependencies, the `using([...])` method accepts any method without parameters or any field with the same
name than the synapse method and an assignment compatible return value - regardless of any modifiers.
In this case, it finds the private, static fields `defaultLocale` and `greetingMessages`.
Such fields or methods are called _dependency provider fields_ or _dependency provider methods_.

A dependency provider method may be abstract, in which case it's also a synapse method and hence the model is a neuron 
class or interface.
You can either use classic inheritance and implement the synapse method in a subclass or subinterface or you can use 
another `wire([...]).using([...])` term to wire the module by looking up its dependencies in some delegate object, 
ideally another module.
This kind of module stacking is extremely powerful because the modules may have different scopes:
For example, the delegate module may be application scoped while the delegating module may be request scoped.
This is why Neuron DI doesn't need any scope annotations such as `@Singleton`.

In this case, the module is application scoped and the `GreetingService` is immutable, so it's a good idea to cache it
for future use.
The `@Caching` annotation makes sure that the body of the method `greetingService()` is called at most once.
For Scala developers, the effect is the same as the `lazy val` definition.
The `@Caching` annotation requires the `@Neuron` annotation on the class or interface.
So although this module class does not have any synapse methods, it's a neuron class by declaration.

Last, but not least, a module does not need to extend or implement a specific class or interface.
Modules classes or interfaces are implementations of the module pattern, not of a particular type.

### About Booting Applications

The `Module` class is abstract to prevent you from accidentally using `new Module()` - this would ignore the `@Caching` 
annotation.
Also, we haven't seen how a `GreetingController` is created.
So how do these objects get created?
The solution is in the `Main` class:

```java
import example.web.framework.HttpServer;

import java.io.IOException;

import static global.namespace.neuron.di.java.Incubator.breed;

public abstract class Main extends Module implements HttpServer {

    public static void main(String... args) throws IOException {
        breed(Main.class)
                .with(GreetingController.class)
                    .route("/greeting")
                        .get(GreetingController::get)
                .start(args.length > 0 ? Integer.parseInt(args[0]) : 8080);
    }
}
```  

The main class extends our module class, so it inherits the `@Neuron` and `@Caching` annotations and thus, it should be 
abstract to prevent you from accidentally using `new Main()`.
To create our application instance, the `main([...])` method calls the method `breed(...)` in the class `Incubator`.
This method is suitable for creating a neuron without any synapse methods.
A main class should not have any synapse methods, that is, unsatisfied dependencies, so it's a perfect match. 

The remaining method calls use the [domain-specific language] (DSL) inherited from the `HttpServer` interface to 
configure the routing of HTTP calls and start a web server.
The DSL instructs the web server to create an instance of the `GreetingController` interface and call its `get()` method
for each `GET` request to the URI path `/greeting`.

The actual creation of the controller instance is hidden by the DSL and looks similar to this:

```java
package example.web.framework;

import com.sun.net.httpserver.HttpExchange;

import java.util.Map;

import static global.namespace.neuron.di.java.Incubator.wire;

interface HttpHandler<C extends HttpController> {

    Class<C> controller();

    HttpServer server();

    default void apply(HttpExchange exchange) throws Exception {
        [...]
        var responseBody = [...]
        var controller = wire(controller())
                .bind(HttpController::exchange).to(exchange)
                .bind(HttpController::responseBody).to(responseBody)
                .using(server())
        [...]
    }
}
```

The `apply([...])` method uses the method `wire([...])` again, but this time the term is a bit more complex:

+ The controller class to instantiate is not a class literal, but provided by the method `controller()`.
+ The synapse method `HttpController.exchange()` is bound to the method parameter `exchange`.
+ The synapse method `HttpController.responseBody` is bound to the local variable `responseBody`.
+ Any other synapse methods will be bound to dependency provider methods or fields in the server object, which is our 
  `Main` instance.

## Documentation

For documentation, please consult the following resources:

- [Neuron DI Examples for Java]
- [Documentation Wiki][Wiki]

Release Notes are available on GitHub:

- [![Release Notes](https://img.shields.io/github/release/christian-schlichtherle/neuron-di.svg?maxAge=3600)](https://github.com/christian-schlichtherle/neuron-di/releases/latest)

Release artifacts are deployed to [Maven Central](https://search.maven.org/#search%7Cga%7C1%7Cg%3A%22global.namespace.neuron-di%22):

- [![Neuron DI for Java](https://img.shields.io/maven-central/v/global.namespace.neuron-di/neuron-di.svg?label=Neuron%20DI%20for%20Java&maxAge=3600)](https://search.maven.org/#search%7Cga%7C1%7Cg%3A%22global.namespace.neuron-di%22%20AND%20a%3A%22neuron-di%22)
- [![Neuron DI for Scala 2.11](https://img.shields.io/maven-central/v/global.namespace.neuron-di/neuron-di-scala_2.11.svg?label=Neuron%20DI%20for%20Scala%202.11&maxAge=3600)](https://search.maven.org/#search%7Cga%7C1%7Cg%3A%22global.namespace.neuron-di%22%20AND%20a%3A%22neuron-di-scala_2.11%22)
- [![Neuron DI for Scala 2.12](https://img.shields.io/maven-central/v/global.namespace.neuron-di/neuron-di-scala_2.12.svg?label=Neuron%20DI%20for%20Scala%202.12&maxAge=3600)](https://search.maven.org/#search%7Cga%7C1%7Cg%3A%22global.namespace.neuron-di%22%20AND%20a%3A%22neuron-di-scala_2.12%22)
- [![Neuron DI for Scala 2.13](https://img.shields.io/maven-central/v/global.namespace.neuron-di/neuron-di-scala_2.13.svg?label=Neuron%20DI%20for%20Scala%202.13&maxAge=3600)](https://search.maven.org/#search%7Cga%7C1%7Cg%3A%22global.namespace.neuron-di%22%20AND%20a%3A%22neuron-di-scala_2.13%22)
- [![Neuron DI @ Guice for Java](https://img.shields.io/maven-central/v/global.namespace.neuron-di/neuron-di-guice.svg?label=Neuron%20DI%20@%20Guice%20for%20Java&maxAge=3600)](https://search.maven.org/#search%7Cga%7C1%7Cg%3A%22global.namespace.neuron-di%22%20AND%20a%3A%22neuron-di-guice%22)
- [![Neuron DI @ Guice for Scala 2.11](https://img.shields.io/maven-central/v/global.namespace.neuron-di/neuron-di-guice-scala_2.11.svg?label=Neuron%20DI%20@%20Guice%20for%20Scala%202.11&maxAge=3600)](https://search.maven.org/#search%7Cga%7C1%7Cg%3A%22global.namespace.neuron-di%22%20AND%20a%3A%22neuron-di-guice-scala_2.11%22)
- [![Neuron DI @ Guice for Scala 2.12](https://img.shields.io/maven-central/v/global.namespace.neuron-di/neuron-di-guice-scala_2.12.svg?label=Neuron%20DI%20@%20Guice%20for%20Scala%202.12&maxAge=3600)](https://search.maven.org/#search%7Cga%7C1%7Cg%3A%22global.namespace.neuron-di%22%20AND%20a%3A%22neuron-di-guice-scala_2.12%22)
- [![Neuron DI @ Guice for Scala 2.13](https://img.shields.io/maven-central/v/global.namespace.neuron-di/neuron-di-guice-scala_2.13.svg?label=Neuron%20DI%20@%20Guice%20for%20Scala%202.13&maxAge=3600)](https://search.maven.org/#search%7Cga%7C1%7Cg%3A%22global.namespace.neuron-di%22%20AND%20a%3A%22neuron-di-guice-scala_2.13%22)

Neuron DI is covered by the Apache License, Version 2.0.

[Wiki]: ../../wiki
[Features and Benefits]: ../../wiki/Features
[Neuron DI Examples for Java]: https://github.com/christian-schlichtherle/neuron-di-examples
[Domain-Specific Language]: https://en.wikipedia.org/wiki/Domain-specific_language
