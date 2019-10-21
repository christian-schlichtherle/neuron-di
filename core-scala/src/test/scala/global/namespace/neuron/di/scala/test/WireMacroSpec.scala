/*
 * Copyright © 2016 Schlichtherle IT Services
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package global.namespace.neuron.di.scala.test

import java.util.function.Supplier

import global.namespace.neuron.di.scala.CachingStrategy._
import global.namespace.neuron.di.scala.test.WireMacroSpec._
import global.namespace.neuron.di.scala.{Caching, Neuron, wire}
import org.scalatest.FeatureSpec
import org.scalatest.Matchers._

class WireMacroSpec extends FeatureSpec {

  feature("Synapse methods in neuron and non-neuron types can be auto-wired using the `wire` macro") {

    scenario("A[Int]") {
      val foo = 1
      val a = wire[A[Int]]
      a.foo shouldBe foo
    }

    scenario("A[String]") {
      def foo = new String("foo")
      val a = wire[A[String]]
      a.foo shouldBe foo
      a.foo shouldNot be theSameInstanceAs a.foo
    }

    scenario("A1") {
      intercept[IllegalAccessError] {
        def foo = new String("foo")
        val a1 = wire[A1]
        a1.foo shouldBe foo
        a1.foo should be theSameInstanceAs a1.foo
      }
    }

    scenario("A2") {
      def foo = new String("foo")
      val a2 = wire[A2]
      a2.foo shouldBe foo
      a2.foo should be theSameInstanceAs a2.foo
    }

    scenario("ABC[Int]") {
      val foo = 1
      val bar = 2
      def baz(abc: ABC[Int]) = abc.foo + abc.bar
      val abc = wire[ABC[Int]]
      abc.foo shouldBe foo
      abc.bar shouldBe bar
      abc.baz shouldBe baz(abc)
    }

    scenario("ABC[String]") {
      def foo = new String("foo")
      def bar = new String("bar")
      def baz(abc: ABC[String]) = abc.foo + abc.bar
      val abc = wire[ABC[String]]
      abc.foo shouldBe foo
      abc.foo shouldNot be theSameInstanceAs abc.foo
      abc.bar shouldBe bar
      abc.bar shouldNot be theSameInstanceAs abc.bar
      abc.baz shouldBe baz(abc)
      abc.baz shouldNot be theSameInstanceAs abc.baz
    }

    scenario("ABC1[String]") {
      intercept[IllegalAccessError] {
        def foo = new String("foo")
        def bar = new String("bar")
        val baz = (abc: ABC1[String]) => abc.foo + abc.bar
        val abc1 = wire[ABC1[String]]
        abc1.foo shouldBe foo
        abc1.foo should be theSameInstanceAs abc1.foo
        abc1.bar shouldBe bar
        abc1.bar should be theSameInstanceAs abc1.bar
        abc1.baz shouldBe baz(abc1)
        abc1.baz should be theSameInstanceAs abc1.baz
      }
    }

    scenario("ABC2[String]") {
      def foo = new String("foo")
      def bar = new String("bar")
      val baz = (abc: ABC2[String]) => abc.foo + abc.bar
      val abc2 = wire[ABC2[String]]
      abc2.foo shouldBe foo
      abc2.foo should be theSameInstanceAs abc2.foo
      abc2.bar shouldBe bar
      abc2.bar should be theSameInstanceAs abc2.bar
      abc2.baz shouldBe baz(abc2)
      abc2.baz should be theSameInstanceAs abc2.baz
    }

    scenario("Supplier[String]") {
      def get = "Hello world!"
      val supplier = wire[Supplier[String]]
      supplier.get shouldBe "Hello world!"
    }
  }
}

private object WireMacroSpec {

  trait A[T] {

    def foo: T
  }

  trait A1 extends A[String] {

    override val foo: String
  }

  @Neuron
  trait A2 extends A[String] {

    @Caching
    override def foo: String
  }

  trait B[T] {

    def bar: T
  }

  trait C[T] {

    def baz: T
  }

  trait ABC[T] extends A[T] with B[T] with C[T]

  trait ABC1[T] extends ABC[T] {

    override val foo: T
    override val bar: T
    override val baz: T
  }

  @Neuron
  trait ABC2[T] extends ABC[T] {

    @Caching(NOT_THREAD_SAFE)
    override def foo: T

    @Caching(THREAD_LOCAL)
    override def bar: T

    @Caching(THREAD_SAFE)
    override def baz: T
  }
}
