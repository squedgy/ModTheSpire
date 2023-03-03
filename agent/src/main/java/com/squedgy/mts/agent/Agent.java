package com.squedgy.mts.agent;

import com.vdurmont.semver4j.Semver;

import java.lang.instrument.UnmodifiableClassException;
import java.lang.reflect.*;
import java.util.*;

public class Agent {
    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger("Squedgy-Agent");

    public static void premain(String agentArgs, java.lang.instrument.Instrumentation inst) {
        LOG.info("squedgy agent loaded");
        inst.addTransformer(new FieldTransform(), true);

        // LOG.info("Loaded classes:");
        // for(Class c : inst.getAllLoadedClasses()) {
        //     LOG.info("  {}", c.getName());
        // }
        try {
            Class c = Class.forName("jdk.internal.reflect.Reflection");
            LOG.info("Found: {}", c.getName());
            inst.retransformClasses(c);

            Method register = c.getDeclaredMethod("registerFieldsToFilter", Class.class, Set.class);
            register.invoke(null, Agent.class, Collections.emptySet());
        } catch(
            UnmodifiableClassException |
            ClassNotFoundException |
            NoSuchMethodException |
            IllegalAccessException |
            InvocationTargetException e
        ) {
            LOG.info("Failed to re-transform Field");
            Semver jvm = new Semver(System.getProperty("java.vm.version"), Semver.SemverType.LOOSE);
            Semver target = new Semver("15.0.0", Semver.SemverType.LOOSE);

            if(jvm.isGreaterThanOrEqualTo(target)) {
                throw new RuntimeException(e);
            } else {
                LOG.info("We're before java 15, assuming that it's due to stuff not being implemented, and ignoring the issue");
            }
        }

    }
}
