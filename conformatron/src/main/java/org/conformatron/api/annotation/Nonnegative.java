package org.conformatron.api.annotation;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;

/**
 * Indicate that values are not allowed to be negative. Makes no statement whether zero is allowed or not
 * 
 * @author Philip Helger
 */
@Documented
@Retention(RUNTIME)
public @interface Nonnegative {
}
