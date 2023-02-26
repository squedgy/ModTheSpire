package com.evacipated.cardcrawl.modthespire;

import java.lang.reflect.*;

public class ReflectionHelper {
    private static final String MODIFIERS_FIELD = "modifiers";

    private static void makeNonFinal(
        Field field
    ) throws IllegalAccessException, NoSuchFieldException {
        Field internalModifiers = Field.class.getDeclaredField(MODIFIERS_FIELD);
        internalModifiers.setAccessible(true);

        int targetModifiers = internalModifiers.getInt(field);

        int finalBitInverted = ~Modifier.FINAL;
        targetModifiers &= finalBitInverted;

        internalModifiers.set(field, targetModifiers);
    }

    private static void makeAccessible(Field field) {
        field.setAccessible(true);
    }

    public static void setStaticFinalField(
        Field field,
        Object value
    ) throws NoSuchFieldException, IllegalAccessException {
        makeAccessible(field);

        makeNonFinal(field);

        field.set(null, value);
    }
}
