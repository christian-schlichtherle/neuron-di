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
package global.namespace.neuron.di.spi;

import net.sf.cglib.core.DefaultNamingPolicy;
import net.sf.cglib.core.Predicate;

final class NeuronDINamingPolicy extends DefaultNamingPolicy {

    static final NeuronDINamingPolicy SINGLETON = new NeuronDINamingPolicy();

    private NeuronDINamingPolicy() { }

    @Override
    public String getClassName(String prefix, String source, Object key, Predicate names) {
        return super.getClassName(prefix, "Enhancer", key, names);
    }

    @Override
    protected String getTag() { return "ByNeuronDI"; }
}
