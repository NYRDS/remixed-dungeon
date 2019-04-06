package com.nyrds;


import java.lang.annotation.ElementType;
import java.lang.annotation.Target;


@Target(value = {ElementType.CONSTRUCTOR})
public @interface ForRestoreFromBundle {
}
