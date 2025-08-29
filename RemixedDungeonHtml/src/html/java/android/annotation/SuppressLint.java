package android.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Stub SuppressLint annotation for HTML version
 */
@Retention(RetentionPolicy.CLASS)
public @interface SuppressLint {
    String[] value();
}