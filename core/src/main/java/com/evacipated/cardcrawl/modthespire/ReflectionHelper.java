package com.evacipated.cardcrawl.modthespire;

import jdk.internal.reflect.FieldAccessor;
import org.slf4j.*;

import java.lang.reflect.*;
import java.util.*;

public class ReflectionHelper {
    private static final Logger LOG = LoggerFactory.getLogger(ReflectionHelper.class);

    private static final Method getFieldAccessor, acquireFieldAccessor;
    private static final Field trustedFinal, modifiers;

    static {
        boolean failed = false;
        boolean failedAll = true;

        try {
            Class.forName(Field.class.getName());
        } catch(ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        try {
            getFieldAccessor = Field.class.getDeclaredMethod("getFieldAccessor", boolean.class);
            getFieldAccessor.setAccessible(true);

            acquireFieldAccessor = Field.class.getDeclaredMethod("acquireFieldAccessor", boolean.class);
            acquireFieldAccessor.setAccessible(true);

            Field trusted = null;
            try {
                LOG.debug("Found trustedFinal");
                trusted = Field.class.getDeclaredField("trustedFinal");
                trusted.setAccessible(true);
                failedAll = false;
            } catch(NoSuchFieldException e) {
                LOG.debug("Failed to find trusted final");
                failed = true;
            }
            trustedFinal = trusted;

            Field modies = null;
            try {
                LOG.debug("Found modifiers");
                modies = Field.class.getDeclaredField("modifiers");
                modies.setAccessible(true);
                failedAll = false;
            } catch(NoSuchFieldException e) {
                LOG.debug("Failed to find modifiers");
                failed = true;
            }
            modifiers = modies;
        } catch(NoSuchMethodException e) {
            throw new RuntimeException(e);
        }

        if(failed) {
            LOG.debug("Java version: {}", System.getProperty("java.vm.version"));
            LOG.debug("Java name: {}", System.getProperty("java.vm.name"));
            LOG.debug("Java vendor: {}", System.getProperty("java.vm.vendor"));
            LOG.debug("Failed all? {}", failedAll);

            if(failedAll) {
                Field[] fields = Field.class.getDeclaredFields();
                LOG.debug("Field has {} declared fields", fields.length);
                if(fields.length == 0) {
                    List<Field> allFields = new LinkedList<>();
                    Class<?> parent = Field.class.getSuperclass();

                    while(parent != null) {
                        Collections.addAll(allFields, parent.getDeclaredFields());
                        parent = parent.getSuperclass();
                    }
                    LOG.debug("ALL Fields: {}", allFields.size());
                    fields = allFields.toArray(new Field[0]);
                }

                for(Field f : fields) {
                    LOG.debug("Has field: {} {}", f.getType().getName(), f.getName());
                }
            }
        }
    }

    private static void enforceNotReadOnly(FieldAccessor accessor)
        throws NoSuchFieldException, IllegalAccessException {
        Class currentTarget = accessor.getClass();

        Field target = null;
        while(target == null && currentTarget != Object.class) {
            String targetName = currentTarget.getSimpleName();
            for(Field f : currentTarget.getDeclaredFields()) {
                LOG.trace("Checking {}#{} AKA {}", targetName, f.toString(), f.getName());
                int mods = f.getModifiers();
                boolean notStatic = (mods & Modifier.STATIC) == 0;
                if(notStatic && Objects.equals(f.getName(), "isReadOnly")) {
                    target = f;
                    break;
                }
            }

            currentTarget = currentTarget.getSuperclass();
        }

        if(target == null) throw new NoSuchFieldException("Failed to locate isReadOnly for " + accessor.getClass().getName());

        target.setAccessible(true);
        target.set(accessor, false);
    }

    public static void setStaticFinalField(
        Field field,
        Object value
    ) throws NoSuchFieldException, IllegalAccessException, InvocationTargetException {
        field.setAccessible(true);

        FieldAccessor accessor = (FieldAccessor) getFieldAccessor.invoke(field, true);
        if(accessor == null) accessor = (FieldAccessor) acquireFieldAccessor.invoke(field, true);

        enforceNotReadOnly(accessor);

        if(trustedFinal != null) {
            LOG.trace("trusted final to false");
            trustedFinal.set(field, false);
        }

        if(modifiers != null) {
            LOG.trace("disabling final");
            modifiers.setInt(field, modifiers.getInt(field) & ~Modifier.FINAL);
        }

        accessor.set(null, value);
    }
}
