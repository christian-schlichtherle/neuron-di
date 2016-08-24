package global.tranquillity.neuron.di.guice.it;

import javax.inject.Inject;
import javax.inject.Named;

public class RealFormatter implements Formatter {

    private final String format;

    @Inject
    public RealFormatter(final @Named("format") String format) {
        this.format = format;
    }

    @Override
    public String message(String... args) {
        return String.format(format, (Object[]) args);
    }
}
