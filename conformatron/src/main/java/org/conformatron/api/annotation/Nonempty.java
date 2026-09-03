package org.conformatron.api.annotation;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;

/**
 * Defines that Strings or Collections might not be empty.
 * 
 * @author Philip Helger
 */
@Documented
@Retention(RUNTIME)
public @interface Nonempty {
}
