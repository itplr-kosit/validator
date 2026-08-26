package org.kosit.base.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that a method returns an immutable object (in case the returned type itself is not immutable). This is
 * especially useful for returned containers that are not modifiable.
 *
 * @author Philip Helger
 */
@Retention(RetentionPolicy.CLASS)
@Target({ ElementType.METHOD })
@Documented
public @interface ReturnsImmutableObject {

    String value() default "";
}
