[![Apache License 2.0](https://img.shields.io/github/license/christian-schlichtherle/neuron-di.svg?maxAge=3600)](https://www.apache.org/licenses/LICENSE-2.0)
[![Build Status](https://api.travis-ci.org/christian-schlichtherle/neuron-di.svg)](https://travis-ci.org/christian-schlichtherle/neuron-di)

# Neuron DI

As a Java developer, does Spring or Guice give you a headache?
Maybe you are tired of annotation frenzy or slow startup times of your application and test code?  

As a Scala developer, do you frown upon the Cake pattern?
Maybe you are looking for something simpler, yet scalable while retaining compile-time dependency injection?

Neuron DI is a tiny library for dependency injection (DI) in Java and Scala which helps you structure your application
or library code with ease, whether it's small or large.
It takes a radically different approach to the problem of DI than any [JSR 330] based framework like 
[Spring, Guice etc](http://javax-inject.github.io/javax-inject/).

## Features

Neuron DI provides the following **features**:

- dependency injection into abstract methods without parameters, called _synapse methods_
- synapse methods can be members of **any** class or interface, called _neuron classes_ or _neuron interfaces_
- dependency injection into third-party code like `java.util.function.Supplier.get()`
- lazy resolution of dependencies
- caching of dependencies by applying a not-thread-safe, thread-safe or thread-local strategy
- looking up dependencies in any object by delegation
- dependency injection at compile-time (Scala only)
- peaceful coexistence with any other DI framework or library

Neuron DI frees your code from the following **code smells**:

- copy constructors for constructor injection
- mutable classes for method injection
- bad testability because of (private) field injection
- qualifier annotations like @Named
- scope annotations like @Singleton
- specific application contexts or containers
- tight coupling with a DI framework or library

## Benefits

Neuron DI is a hybrid - it supports both runtime and compile-time DI:

With Java, your code is constrained to runtime DI, but Neuron DI frees it from the shortcomings and the ballast of 
JSR 330 - see [next section](#walk-through).
It also adds caching like you can do with a `lazy val` definition in Scala, but with more options like not-thread-safe 
caching or even thread-local caching, so you can say goodbye to the `ThreadLocal` class.
With synapse methods and caching you can effectively inject dependencies into interfaces, which means you can compose
your application mostly of mix-in interfaces like you can do in Scala. 

With Scala, you can use all the features for Java plus compile-time DI:
With compile-time DI, you can avoid the (albeit minimal) runtime overhead plus you get a compiler error if any 
dependency is missing or has the wrong type.

All in all, Neuron DI is designed to close some gaps between Java and Scala, allowing you to more easily mix these 
languages in your projects.
For example, you can write your main code in Java and your test code in Scala.
Neuron DI then lets you take the same approach to DI with both languages.

## Walk-Through

The sample code discussed in this chapter is derived from another GitHub repository, named
[Neuron DI Examples For Java].
Please check out this repository for more options and all the glory details.

### About Neurons And Synapses

Consider the following sample code:

```java
import java.util.*;
import java.util.function.BiFunction;

public interface GreetingService extends BiFunction<List<Locale>, Optional<String>, String> {

    Locale defaultLocale();

    Map<Locale, List<String>> greetingMessages();

    default String apply(List<Locale> languageRanges, Optional<String> who) {
        // Some code calling `defaultLocale()` and `greetingMesssages()`:
        [...]
    }
}
```

`GreetingService` has two abstract methods without parameters: `defaultLocale()` and `greetingMessages()`.
These methods return dependencies of the code in the `apply([...])` method (not shown).
In Neuron DI, abstract methods without parameters are called _synapse methods_ and their enclosing types are called
_neuron classes_ or _neuron interfaces_.
This concept is entirely abstract:
There is no dependency of the `GreetingService` interface on the API of Neuron DI.

> ###### Tip
>
> The body of the `apply([...])` method and its dependencies `defaultLocale()` and `greetingMessages()` are actually
> implementation details!
> So, according to the interface segregation and dependency inversion principles these methods should be moved to 
> another class or interface (not shown). 

One of the benefits of this approach is that synapse methods not only give a dependency a location and a (return) type,
but also a (method) _name_.
This is why Neuron DI doesn't need any qualifier annotations such as `@Named`.    

Another benefit is that dependency resolution is inherently _lazy by design_, but can also be _eager by choice_:
When a neuron is constructed, you get to choose if you want to delegate each call to a synapse method to another method 
in another object (lazy dependency resolution) or if you simply want to return a pre-computed value (eager dependency 
resolution).

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

In this case, the body of the `get()` method depends on the synapse method `greetingService()`. 

### About Modules And The Incubator

In a large code base, you will have many neuron classes and interfaces and you will need to wire them into a dependency 
graph somewhere.
This is where the module pattern comes into play:
A module bundles a group of factory methods for application components which may depend on each other into a single 
object.

In design pattern parlance, the module pattern is a blend of the factory pattern and the mediator pattern because a 
module not only creates (and optionally caches) application components, but typically delegates back to itself whenever 
any of their dependencies need to get resolved.

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
Its body calls the static method `Incubator.wire([...])` in the package `global.namespace.neuron.di.java`, which 
provides the Java API of Neuron DI.
The term `wire(GreetingService.class).using(this)` specifically tells the incubator to make a `GreetingService` and look 
up any of its dependencies in `this` module object.
This technique is called _dependency delegation_.
There are other options to wire a neuron, but dependency delegation is easy to use, yet very versatile.

When looking up dependencies, the `using([...])` method accepts any method without parameters or any field with the same
name as the synapse method and an assignment compatible return value - regardless of any modifiers.
In this case, it finds the private, static fields `defaultLocale` and `greetingMessages`.
Such fields or methods are called _dependency provider fields_ or _dependency provider methods_.

A dependency provider method may be abstract, in which case it's also a synapse method and hence the module is a neuron 
class or interface.
You can either use regular inheritance and implement the synapse method in a subclass or sub-interface or you can use 
another `wire([...]).using([...])` term to wire the module by looking up its dependencies in some delegate object, 
ideally another module.
This kind of module stacking enables the modules to have different scopes:
For example, the delegate module may be application scoped while the delegating module may be request scoped.
This is why Neuron DI doesn't need any scope annotations such as `@Singleton`.

In this case, the module is application scoped and the `GreetingService` is immutable, so it's a good idea to cache it
for subsequent use.
The `@Caching` annotation makes sure that the body of the method `greetingService()` is called at most once.
For Scala developers, the effect is the same as a `lazy val` definition.
The `@Caching` annotation requires the `@Neuron` annotation on the class or interface.
So although this module class does not have any synapse methods, it's a neuron class by declaration.

Last, but not least, a module does not need to extend or implement a specific class or interface.
Modules classes or interfaces are implementations of the module pattern, not of a particular type.

### About Booting Applications

The `Module` class is abstract to prevent you from accidentally calling `new Module()` - this would ignore the
`@Caching` annotation.
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

The main class extends our module class, so it inherits the `@Neuron` and `@Caching` annotations and hence it should be 
abstract again to prevent you from accidentally calling `new Main()`.
To create our application instance, the main method calls the `breed([...])` method in the `Incubator` class.
This method is designed for creating a neuron object which does not have any synapse methods.
Obviously, a main class should never have any synapse methods, that is, it should never have any unsatisfied 
dependencies, so it's a perfect match. 

The remaining method calls use the [domain-specific language] (DSL) inherited from the `HttpServer` interface to 
configure the routing of HTTP calls and start a web server.
The DSL instructs the web server to create an instance of the `GreetingController` interface and call its `get()` method
whenever it receives a `GET` request for the URI path `/greeting`.

The actual creation of the controller instance is happening in the web framework and looks similar to this:

```java
package example.web.framework;

import com.sun.net.httpserver.HttpExchange;

import java.util.Map;

import static global.namespace.neuron.di.java.Incubator.wire;

interface HttpHandler<C extends HttpController> {

    Class<C> controller();

    HttpServer server();

    default void apply(HttpExchange exchange) throws Exception {
        try (OutputStream responseBody = [...]) {
            C controller = wire(controller())
                    .bind(HttpController::exchange).to(exchange)
                    .bind(HttpController::responseBody).to(responseBody)
                    .using(server())
            [...]
        }
    }
}
```

The `apply([...])` method calls the `wire([...])` method again, but this time the term is a bit more complex:

+ The controller class to instantiate is not provided as a class literal, but returned by the `controller()` method.
+ The synapse method `HttpController.exchange()` is bound to the method parameter `exchange`.
+ The synapse method `HttpController.responseBody` is bound to the local variable `responseBody`.
+ Any other synapse methods of the controller class or interface will be bound to dependency provider methods or fields 
  in the server object, which is an instance of the application class `Main`, thereby effectively delegating any 
  dependencies of controller classes or interfaces to the application class.

Note that any controller instances are request scoped, so they may even be mutable, while the main class (with its 
module superclass) is application scoped, so it should be immutable. 

### About Unit Testing

The Scala API of Neuron DI provides an exclusive feature: 
Compile-time dependency injection using the `wire` macro.
Consider the following [ScalaTest] stub for the `GreetingService` interface:

```scala
import java.util.Locale._

import global.namespace.neuron.di.scala._
import org.scalatest.WordSpec

import scala.jdk.CollectionConverters._

class GreetingServiceSpec extends WordSpec {

  "A GreetingService" should {
    "compute the expected message" in {
      // Some test code calling `greetingService.apply([...])`:
      [...]
    }
  }

  private lazy val greetingService = wire[GreetingService]

  private lazy val greetingMessages = Map(
    ENGLISH -> List("Hello, %s!", "world"),
    GERMAN -> List("Hallo, %s!", "Welt"),
  ).view.mapValues(_.asJava).toMap.asJava

  private lazy val defaultLocale = ENGLISH
}
```

In this unit test, `greetingService` is initialized using the `wire` macro in the package 
`global.namespace.neuron.di.scala`, which provides the Scala API of Neuron DI.
The macro figures that the `GreetingService` interface has two synapse methods, `defaultLocale` and `greetingMessages`,
and looks them up in the current scope.
If no such methods or fields are available or if their (return) types are not assignment-compatible, then the macro
emits an error message. 

The Scala API also has its own variant of the `Incubator` class with some syntax sugar added, so you could write this 
instead:

    private lazy val greetingService = Incubator.wire[GreetingService] using this 

However, the `Incubator` class strictly uses runtime DI, no matter if you use its variant in the Scala API or the Java 
API, so the `wire` macro is generally preferable. 

### About Application Performance

Neuron DI uses reflection to analyze neuron and dependency provider classes or interfaces at runtime.
It saves its findings in class-loader sensitive caches to speed up subsequent calls.
For proxy class generation, it uses ASM.
The actual dispatching of synapse methods to dependency provider methods or fields is done using method handles.
Tests have shown that the per-call overhead of synapse methods in comparison to hand-written implementations is below 
the level of noise typically induced by the standard garbage collection. 

### About Illegal Reflective Access

Illegal reflective access is avoided wherever possible:

+ No illegal reflective access whatsoever is required for dynamic loading of generated proxy classes.
+ If a dependency provider method in a non-public subclass or sub-interface overrides or implements a method in a public 
  superclass or super-interface, then the public superclass or super-interface is used.

## More Documentation

For more documentation, please consult the following resources:

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

[Domain-Specific Language]: https://en.wikipedia.org/wiki/Domain-specific_language
[Features and Benefits]: ../../wiki/Features
[JSR 330]: https://www.jcp.org/en/jsr/detail?id=330
[Neuron DI Examples For Java]: https://github.com/christian-schlichtherle/neuron-di-examples
[ScalaTest]: http://www.scalatest.org
[Wiki]: ../../wiki
