/*
 * Copyright © 2016 - 2019 Schlichtherle IT Services
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
package global.namespace.neuron.di.java;

import java.lang.annotation.*;

/**
 * Indicates that the annotated class or interface is a <i>neuron type</i>.
 * A neuron type is supposed to have <i>synapse methods</i> to make contact with its dependencies.
 * All abstract, parameterless methods in a neuron type are synapse methods - there is no need to declare them.
 * These methods get automatically implemented so that they return their respective dependencies.
 * Thus, all dependencies of a neuron are resolved lazily plus they have a name and a type.
 * <p>
 * If this annotation is present on a class, it gets inherited by any subclass.
 * In compliance with the Java Language Specification, this does not apply to interfaces.
 * <p>
 * This annotation can also be applied to custom annotation types, in which case any class or interface annotated with
 * these is treated as if it's directly annotated with this annotation.
 * For example, the following annotation caches all synapse values using a thread-safe strategy:
 * <pre><code>
 *     import global.namespace.neuron.di.java.*;
 *     import java.lang.annotation.*;
 *
 *     &#64;Neuron(cachingStrategy = CachingStrategy.THREAD_SAFE)
 *     &#64;Inherited
 *     &#64;Documented
 *     &#64;Retention(RetentionPolicy.RUNTIME)
 *     &#64;Target(ElementType.TYPE)
 *     public &#64;interface ThreadSafeCachingNeuron {
 *     }
 * </code></pre>
 *
 * @see <a href="https://en.wikipedia.org/wiki/Neuron">Neuron on the English Wikipedia</a>
 */
@Inherited
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Neuron {

    /**
     * Configures the strategy for caching the return value of synapse methods which are not annotated with
     * {@link Caching}.
     */
    CachingStrategy cachingStrategy() default CachingStrategy.DISABLED;
}
