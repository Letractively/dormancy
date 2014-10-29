//$Id: CollectionOfElements.java 14736 2008-06-04 14:23:42Z hardy.ferentschik $
package org.hibernate.annotations;

import javax.persistence.FetchType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static javax.persistence.FetchType.LAZY;

/**
 * Annotation used to mark a collection as a collection of elements or
 * a collection of embedded objects
 *
 * @author Emmanuel Bernard
 */
@Target({METHOD, FIELD})
@Retention(RUNTIME)
public @interface CollectionOfElements {
	/**
	 * Represent the element class in the collection
	 * Only useful if the collection does not use generics
	 */
	Class targetElement() default void.class;

	FetchType fetch() default LAZY;
}
