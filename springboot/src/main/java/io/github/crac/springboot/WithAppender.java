package io.github.crac.springboot;

import net.bytebuddy.dynamic.scaffold.InstrumentedType;
import net.bytebuddy.implementation.Implementation;
import net.bytebuddy.implementation.bytecode.ByteCodeAppender;

import java.util.function.Function;

class WithAppender implements Implementation {
    private final Function<Target, ByteCodeAppender> appenderProvider;

    public WithAppender(Function<Target, ByteCodeAppender> appenderProvider) {
        this.appenderProvider = appenderProvider;
    }

    @Override
    public ByteCodeAppender appender(Target target) {
        return appenderProvider.apply(target);
    }

    @Override
    public InstrumentedType prepare(InstrumentedType instrumentedType) {
        return instrumentedType;
    }
}
