package com.evacipated.cardcrawl.modthespire.patching.enums;

import com.evacipated.cardcrawl.modthespire.*;
import com.evacipated.cardcrawl.modthespire.lib.SpireEnum;
import javassist.*;
import javassist.Modifier;
import javassist.bytecode.*;
import javassist.bytecode.annotation.*;
import org.scannotation.AnnotationDB;
import org.slf4j.*;

import java.io.IOException;
import java.lang.reflect.*;
import java.net.URL;
import java.util.*;

import static org.slf4j.LoggerFactory.*;

public class EnumPatcher {
    private static final Logger LOG = getLogger(EnumPatcher.class);
    private static Map<Class<?>, EnumBusterReflect> enumBusterMap = new HashMap<>();

    private static String findName(
        CtField on,
        SpireEnum anno
    ) {
        if(anno.name()
            .isEmpty()) {
            return on.getName();
        }

        return anno.name();
    }

    private static String findName(
        Field on,
        SpireEnum anno
    ) {
        if(anno.name()
            .isEmpty()) {
            return on.getName();
        }

        return anno.name();
    }

    private static Constructor findConstructor(
        Class clazz,
        Class<?>... types
    ) {
        try {
            return clazz.getDeclaredConstructor(types);
        } catch(NoSuchMethodException e) {
            return null;
        }
    }

    private static <T> T construct(
        Constructor<T> constr,
        Object... args
    ) {
        try {
            return constr.newInstance(args);
        } catch(InvocationTargetException | InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private static SpireEnum.ArgProvider makeProvider(
        String enumName,
        Class<? extends SpireEnum.ArgProvider> clazz
    ) {
        Constructor<? extends SpireEnum.ArgProvider> constr = findConstructor(clazz, String.class);

        if(constr != null) return construct(constr, enumName);

        constr = findConstructor(clazz);

        if(constr == null) {
            throw new RuntimeException(
                "Failed to find zero or one argument constructor for ArgProvider: " +
                clazz.getCanonicalName());
        }

        return construct(constr);
    }

    private static Object[] getExtraArgs(
        String decidedName,
        SpireEnum anno
    ) {
        Class<? extends SpireEnum.ArgProvider>[] args = anno.args();
        if(args.length == 0) return new Object[0];

        Class<? extends SpireEnum.ArgProvider> provider = args[0];
        SpireEnum.ArgProvider provider1 = makeProvider(decidedName, provider);

        return provider1.getArgs();
    }

    public static void patchEnums(
        ClassPool pool,
        ModInfo[] modInfos
    ) throws IOException, ClassNotFoundException, NotFoundException, CannotCompileException {
        URL[] urls = new URL[modInfos.length];
        for(int i = 0; i < modInfos.length; i++) {
            urls[i] = modInfos[i].jarURL;
        }
        patchEnums(pool, urls);
    }

    private static boolean addEnumField(
        ClassPool pool,
        CtField field,
        String enumName,
        boolean hasPrintedWarning
    ) throws CannotCompileException, NotFoundException {
        // Patch new field onto the enum
        try {
            CtClass ctClass = pool.get(field.getType()
                .getName());
            CtField f = new CtField(ctClass, enumName, ctClass);
            f.setModifiers(Modifier.PUBLIC | Modifier.STATIC | Modifier.FINAL | Modifier.ENUM);
            ConstPool constPool = ctClass.getClassFile()
                .getConstPool();
            AnnotationsAttribute attr = new AnnotationsAttribute(
                constPool,
                AnnotationsAttribute.visibleTag
            );
            for(Object a : field.getAvailableAnnotations()) {
                if(Proxy.getInvocationHandler(a) instanceof AnnotationImpl) {
                    AnnotationImpl impl = (AnnotationImpl) Proxy.getInvocationHandler(a);
                    if(impl.getTypeName()
                        .equals(SpireEnum.class.getName())) {
                        continue;
                    }
                    Annotation annotation = new Annotation(impl.getTypeName(), constPool);
                    if(impl.getAnnotation()
                           .getMemberNames() != null) {
                        for(String memberName : impl.getAnnotation()
                            .getMemberNames()) {
                            annotation.addMemberValue(
                                memberName,
                                impl.getAnnotation()
                                    .getMemberValue(memberName)
                            );
                        }
                    }
                    attr.addAnnotation(annotation);
                }
            }
            f.getFieldInfo()
                .addAttribute(attr);
            ctClass.addField(f);
        } catch(DuplicateMemberException ignore) {
            // Field already exists
            if(!com.evacipated.cardcrawl.modthespire.Loader.DEBUG && !hasPrintedWarning) {
                System.out.println();
                return true;
            }
            System.out.printf(
                "Warning: @SpireEnum %s %s is already defined.%n",
                field.getType()
                    .getName(),
                enumName
            );
        }

        return hasPrintedWarning;
    }

    public static void patchEnums(
        ClassPool pool,
        URL... urls
    ) throws IOException, ClassNotFoundException, NotFoundException, CannotCompileException {
        AnnotationDB db = new AnnotationDB();
        db.setScanClassAnnotations(false);
        db.setScanMethodAnnotations(false);
        db.scanArchives(urls);

        Set<String> annotations = db.getAnnotationIndex()
            .get(SpireEnum.class.getName());
        if(annotations == null) {
            return;
        }

        boolean hasPrintedWarning = false;

        for(String s : annotations) {
            CtClass cls = pool.get(s);
            for(CtField field : cls.getDeclaredFields()) {
                SpireEnum spireEnum = (SpireEnum) field.getAnnotation(SpireEnum.class);
                if(spireEnum != null) {
                    String enumName = findName(field, spireEnum);

                    hasPrintedWarning = addEnumField(pool, field, enumName, hasPrintedWarning);
                }
            }
        }
    }

    public static void bustEnums(
        ClassLoader loader,
        ModInfo[] modInfos
    ) throws IOException, ClassNotFoundException, NoSuchFieldException, IllegalAccessException {
        URL[] urls = new URL[modInfos.length];
        for(int i = 0; i < modInfos.length; i++) {
            urls[i] = modInfos[i].jarURL;
        }
        bustEnums(loader, urls);
    }

    private static EnumBusterReflect getEnumBuster(
        ClassLoader loader,
        Class<?> enumType
    ) throws NoSuchFieldException, ClassNotFoundException {
        if(enumBusterMap.containsKey(enumType)) {
            return enumBusterMap.get(enumType);
        }

        EnumBusterReflect buster = new EnumBusterReflect(loader, enumType);
        enumBusterMap.put(enumType, buster);
        return buster;
    }

    public static void bustEnums(
        ClassLoader loader,
        URL... urls
    ) throws IOException, ClassNotFoundException, NoSuchFieldException, IllegalAccessException {
        AnnotationDB db = new AnnotationDB();
        db.setScanClassAnnotations(false);
        db.setScanMethodAnnotations(false);
        db.scanArchives(urls);

        Set<String> annotations = db.getAnnotationIndex()
            .get(SpireEnum.class.getName());
        if(annotations == null) {
            return;
        }

        for(String s : annotations) {
            Class<?> cls = loader.loadClass(s);
            for(Field field : cls.getDeclaredFields()) {
                SpireEnum spireEnum = field.getDeclaredAnnotation(SpireEnum.class);
                if(spireEnum != null) {
                    String enumName = findName(field, spireEnum);
                    Class[] constructorTypes = spireEnum.args();
                    Object[] constructorArgs = getExtraArgs(enumName, spireEnum);

                    EnumBusterReflect buster = getEnumBuster(loader, field.getType());

                    Enum<?> enumValue = buster.make(enumName, 0, constructorTypes, constructorArgs);
                    buster.addByValue(enumValue);
                    try {
                        Field constantField = field.getType()
                            .getField(enumName);
                        ReflectionHelper.setStaticFinalField(constantField, enumValue);
                    } catch(NoSuchFieldException ignored) {
                    } catch(InvocationTargetException e) {
                        throw new RuntimeException(e);
                    }

                    field.setAccessible(true);
                    field.set(null, enumValue);
                }
            }
        }
    }
}
