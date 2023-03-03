package com.squedgy.mts.agent;

import javassist.*;
import org.slf4j.Logger;

import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;
import java.util.Objects;

import static org.slf4j.LoggerFactory.getLogger;

public class FieldTransform implements ClassFileTransformer {
    private static final Logger LOG = getLogger(FieldTransform.class);

    private ClassPath classPath;
    private ClassPool pool = ClassPool.getDefault();

    @Override public byte[] transform(
        ClassLoader loader,
        String className,
        Class<?> classBeingRedefined,
        ProtectionDomain protectionDomain,
        byte[] classfileBuffer
    ) {
        if(classPath == null) {
            classPath = new LoaderClassPath(loader);
        }
        if(Objects.equals(className, "jdk/internal/reflect/Reflection")) {
            LOG.info("transforming Field: {}", classfileBuffer.length);
            try {
                CtClass clazz = pool.get("jdk.internal.reflect.Reflection");
                CtMethod filter = clazz.getMethod(
                    "registerFieldsToFilter",
                    "(Ljava/lang/Class;Ljava/util/Set;)V"
                );

                filter.insertBefore(" { fieldFilterMap.clear(); }");

                return clazz.toBytecode();

                // return writer.toByteArray();
            } catch(Throwable e) {
                LOG.error("Failed to rewrite", e);
            }
        }

        return classfileBuffer;
    }

    // private static class MethodTransform extends MethodVisitor {
    //     protected MethodTransform(MethodVisitor visitor) {
    //         super(Opcodes.ASM9, visitor);
    //     }
    //
    //     @Override public void visitCode() {
    //         super.visitCode();
    //
    //         try {
    //             mv.visitFieldInsn(
    //                 Opcodes.GETSTATIC,
    //                 Type.getInternalName(Class.forName("jdk.internal.reflect.Reflection")),
    //                 "fieldFilterMap",
    //                 Type.getInternalName(Map.class)
    //             );
    //         } catch(ClassNotFoundException e) {
    //             throw new RuntimeException(e);
    //         }
    //
    //         mv.visitMethodInsn(
    //             Opcodes.INVOKEINTERFACE,
    //             Type.getInternalName(Map.class),
    //             "clear",
    //             Type.getMethodDescriptor(Type.VOID_TYPE),
    //             true
    //         );
    //         LOG.info("Clearing fieldFilterMap on invocation of Reflection#registerFieldsToFilter");
    //     }
    // }
    //
    // public static class TransformModifiers extends ClassVisitor {
    //     protected TransformModifiers(ClassWriter parent) {
    //         super(Opcodes.ASM9, parent);
    //     }
    //
    //     @Override public FieldVisitor visitField(
    //         int access,
    //         String name,
    //         String descriptor,
    //         String signature,
    //         Object value
    //     ) {
    //         LOG.info("Field {}", name);
    //         return super.visitField(access, name, descriptor, signature, value);
    //     }
    //
    //     @Override public MethodVisitor visitMethod(
    //         int access,
    //         String name,
    //         String descriptor,
    //         String signature,
    //         String[] exceptions
    //     ) {
    //         MethodVisitor visitor = getDelegate().visitMethod(access, name, descriptor, signature, exceptions);
    //         if(Objects.equals(name, "registerFieldsToFilter")) {
    //             LOG.info("modifying method visitor for registerFieldsToFilter: {}", visitor);
    //             Printer p = new Textifier(Opcodes.ASM9) {
    //                 @Override public void visitMethodEnd() {
    //                     LOG.info("Visit method end");
    //                     print(new PrintWriter(System.out));
    //                 }
    //             };
    //             visitor = new MethodTransform(new TraceMethodVisitor(visitor, p));
    //         }
    //         return visitor;
    //     }
    // }
}
