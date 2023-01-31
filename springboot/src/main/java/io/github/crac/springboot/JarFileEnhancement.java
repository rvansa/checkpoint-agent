package io.github.crac.springboot;

import jdk.crac.Context;
import jdk.crac.Core;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.modifier.Visibility;
import net.bytebuddy.implementation.Implementation;
import net.bytebuddy.implementation.SuperMethodCall;
import net.bytebuddy.implementation.bytecode.ByteCodeAppender;
import net.bytebuddy.jar.asm.MethodVisitor;
import net.bytebuddy.jar.asm.Opcodes;
import net.bytebuddy.jar.asm.Type;
import org.kohsuke.MetaInfServices;

import java.io.File;
import java.util.zip.ZipFile;

import static net.bytebuddy.matcher.ElementMatchers.named;
import static net.bytebuddy.matcher.ElementMatchers.takesArgument;

@MetaInfServices
public class JarFileEnhancement implements Enhancement {
    public static final String SPRING_JARFILE = "org.springframework.boot.loader.jar.JarFile";
    public static final String CRAC_RESOURCE = "$crac_resource$";


    @Override
    public AgentBuilder register(AgentBuilder agentBuilder) {
        return agentBuilder.type(named(SPRING_JARFILE))
                .transform((builder, typeDescription, classLoader, module, protectionDomain) -> builder
                        .defineField(CRAC_RESOURCE, Resource.class, Visibility.PRIVATE)
                        .constructor(takesArgument(0, File.class))
                        .intercept(SuperMethodCall.INSTANCE
                                .andThen(new WithAppender(ResourceAppender::new)))
                );
    }

    public static class Resource implements jdk.crac.Resource {
        private final Object jarFile;

        public Resource(Object jarFile) {
            this.jarFile = jarFile;
            Core.getGlobalContext().register(this);
        }

        @Override
        public void beforeCheckpoint(Context<? extends jdk.crac.Resource> context) throws Exception {
            ((ZipFile) jarFile).close();
        }

        @Override
        public void afterRestore(Context<? extends jdk.crac.Resource> context) throws Exception {
            // the file reopens automatically when needed
        }
    }

    private static class ResourceAppender implements ByteCodeAppender {
        private final Implementation.Target target;

        public ResourceAppender(Implementation.Target target) {
            this.target = target;
        }

        @Override
        public Size apply(MethodVisitor methodVisitor, Implementation.Context implementationContext, MethodDescription instrumentedMethod) {
            String resourceType = Type.getType(Resource.class).getInternalName();
            methodVisitor.visitTypeInsn(Opcodes.NEW, resourceType);
            methodVisitor.visitInsn(Opcodes.DUP);
            methodVisitor.visitVarInsn(Opcodes.ALOAD, 0);
            methodVisitor.visitMethodInsn(Opcodes.INVOKESPECIAL, resourceType, "<init>", Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType(Object.class)), false);
            methodVisitor.visitVarInsn(Opcodes.ALOAD, 0);
            methodVisitor.visitInsn(Opcodes.SWAP);
            methodVisitor.visitFieldInsn(Opcodes.PUTFIELD, target.getInstrumentedType().getInternalName(), CRAC_RESOURCE, Type.getDescriptor(Resource.class));
            methodVisitor.visitInsn(Opcodes.RETURN);
            return new Size(2, 0);
        }
    }
}
