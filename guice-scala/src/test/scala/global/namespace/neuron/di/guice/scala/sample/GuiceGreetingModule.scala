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
package global.namespace.neuron.di.guice.scala.sample

import com.google.inject.AbstractModule
import com.google.inject.name.Names
import javax.inject.{Inject, Named, Singleton}

class GuiceGreeting @Inject() (formatter: Formatter) extends Greeting {

  def message(entity: String): String = formatter format entity
}

class GuiceFormatter @Inject() (@Named("format") format: String) extends Formatter {

  def format(args: AnyRef*): String = String.format(format, args: _*)
}

class GuiceGreetingModule extends AbstractModule {

  override def configure(): Unit = {
    bind(classOf[Greeting]).to(classOf[GuiceGreeting]).in(classOf[Singleton])
    bind(classOf[Formatter]).to(classOf[GuiceFormatter])
    bindConstant.annotatedWith(Names.named("format")).to("Hello %s!")
  }
}
