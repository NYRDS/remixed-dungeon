package com.nyrds;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * Created by mike on 28.07.2017.
 * This file is part of Remixed Pixel Dungeon.
 */

@Target(value = {ElementType.FIELD, ElementType.TYPE})
public @interface Packable {
	String defaultValue() default "";
}

