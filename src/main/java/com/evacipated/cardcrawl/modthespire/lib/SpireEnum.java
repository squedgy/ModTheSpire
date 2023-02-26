package com.evacipated.cardcrawl.modthespire.lib;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface SpireEnum {
    String name() default "";
    /** Which constructor are we looking for */
    Class[] constructor() default {};
    /**
     * If used should be a 1-member array that provides an array of arguments matching those defined
     * in the related {@link #constructor()} member.
     */
    Class<? extends ArgProvider>[] args() default {};

    /**
     * This class should have a either a zero or one-argument constructor intended for retrieving arguments to be passed
     * to it's related enum. If it has an argument it should be a String, and it is the name of the "enum" being created.
     */
    public static abstract class ArgProvider {
        public abstract Object[] getArgs();
    }
}
