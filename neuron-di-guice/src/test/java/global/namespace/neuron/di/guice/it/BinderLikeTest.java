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
package global.namespace.neuron.di.guice.it;

import com.google.inject.Binder;
import com.google.inject.Injector;
import com.google.inject.binder.AnnotatedBindingBuilder;
import com.google.inject.binder.AnnotatedConstantBindingBuilder;
import com.google.inject.binder.ConstantBindingBuilder;
import com.google.inject.binder.ScopedBindingBuilder;
import global.namespace.neuron.di.api.Incubator;
import global.namespace.neuron.di.guice.BinderLike;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import javax.inject.Provider;

import static com.google.inject.name.Names.named;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.mockito.Mockito.*;

public class BinderLikeTest {

    private final Binder binder = mock(Binder.class);

    private final BinderLike binderLike = Incubator
            .stub(BinderLike.class)
            .bind(BinderLike::binder).to(binder)
            .breed();

    @Test
    public void testBindConstantNamed() {
        final AnnotatedConstantBindingBuilder builder1 =
                mock(AnnotatedConstantBindingBuilder.class);
        final ConstantBindingBuilder builder2 =
                mock(ConstantBindingBuilder.class);

        when(binder.bindConstant()).thenReturn(builder1);
        when(builder1.annotatedWith(named("foo"))).thenReturn(builder2);

        assertThat(binderLike.bindConstantNamed("foo"), is(sameInstance(builder2)));
    }

    @Test
    public void testBindNeuron() {
        final AnnotatedBindingBuilder builder1 =
                mock(AnnotatedBindingBuilder.class);
        final ScopedBindingBuilder builder2 =
                mock(ScopedBindingBuilder.class);
        final com.google.inject.Provider injectorProvider =
                mock(com.google.inject.Provider.class);

        when(binder.bind(BinderLike.class)).thenReturn(builder1);
        when(builder1.toProvider(any(Provider.class))).thenReturn(builder2);
        when(binder.getProvider(Injector.class)).thenReturn(injectorProvider);

        assertThat(binderLike.bindNeuron(BinderLike.class), is(sameInstance(builder2)));

        verify(binder).getProvider(Injector.class);

        final ArgumentCaptor<Provider> binderLikeProvider = ArgumentCaptor.forClass(Provider.class);
        verify(builder1).toProvider(binderLikeProvider.capture());
        assertThat(binderLikeProvider.getValue().get(), is(instanceOf(BinderLike.class)));

        verify(injectorProvider).get();
    }
}
