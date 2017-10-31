package com.nyrds;

import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;

/**
 * Created by mike on 28.07.2017.
 * This file is part of Remixed Pixel Dungeon.
 */

@Target(value = FIELD)
public @interface Packable {
	String defaultValue() default "";
}

