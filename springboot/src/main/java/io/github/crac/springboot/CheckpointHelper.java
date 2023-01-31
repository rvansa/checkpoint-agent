package io.github.crac.springboot;

import net.bytebuddy.agent.builder.AgentBuilder;

import java.lang.instrument.Instrumentation;
import java.util.ServiceLoader;

public class CheckpointHelper {


    public static void premain(String args, Instrumentation instrumentation) {
        AgentBuilder agentBuilder = new AgentBuilder.Default()
                .with(AgentBuilder.RedefinitionStrategy.RETRANSFORMATION);
        for (Enhancement e : ServiceLoader.load(Enhancement.class)) {
            agentBuilder = e.register(agentBuilder);
        }
        agentBuilder.installOn(instrumentation);
    }
}
