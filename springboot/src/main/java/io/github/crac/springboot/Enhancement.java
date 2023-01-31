package io.github.crac.springboot;

import net.bytebuddy.agent.builder.AgentBuilder;

public interface Enhancement {
    AgentBuilder register(AgentBuilder agentBuilder);
}
