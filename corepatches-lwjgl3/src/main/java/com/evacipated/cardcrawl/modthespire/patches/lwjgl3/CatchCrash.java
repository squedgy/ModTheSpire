package com.evacipated.cardcrawl.modthespire.patches.lwjgl3;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.backends.lwjgl3.*;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.patches.HandleCrash;
import javassist.CannotCompileException;
import javassist.NotFoundException;
import javassist.expr.ExprEditor;
import javassist.expr.Instanceof;

@SpirePatch(
    clz=Lwjgl3Application.class,
    method=SpirePatch.CONSTRUCTOR,
    paramtypez = {ApplicationListener.class, Lwjgl3ApplicationConfiguration.class}
)
public class CatchCrash
{
    public static ExprEditor Instrument()
    {
        return new ExprEditor()
        {
            @Override
            public void edit(Instanceof i) throws CannotCompileException
            {
                try {
                    if (i.getType().getName().equals(RuntimeException.class.getName())) {
                        i.replace(
                                HandleCrash.class.getName() + ".crash = $1;" +
                                "$_ = $proceed($$);"
                        );
                    }
                } catch (NotFoundException e) {
                    throw new CannotCompileException(e);
                }
            }
        };
    }
}
