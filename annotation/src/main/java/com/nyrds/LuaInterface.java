package com.nyrds;


import java.lang.annotation.ElementType;
import java.lang.annotation.Target;


@Target(value = {ElementType.METHOD, ElementType.TYPE, ElementType.CONSTRUCTOR, ElementType.FIELD})
public @interface LuaInterface {
}
