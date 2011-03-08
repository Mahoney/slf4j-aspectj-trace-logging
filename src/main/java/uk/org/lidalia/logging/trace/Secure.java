package uk.org.lidalia.logging.trace;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Marks that a field, parameter or return value (if set on the method) should not be logged.
 *
 * @author relliot
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface Secure {
	/* Marker only */
}
