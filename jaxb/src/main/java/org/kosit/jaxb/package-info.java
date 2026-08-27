/**
 * Generic, reusable JAXB read/write helper.
 *
 * <p>
 * {@link org.kosit.jaxb.AbstractJaxbConversionService} marshals and unmarshals JAXB-annotated objects to and from a wide
 * variety of source/output types. Stream-based reads are hardened against XXE attacks; an explicit
 * {@link javax.xml.transform.Source} overload is provided as an escape hatch for callers that need full control over
 * parser configuration.
 *
 * <p>
 * All types in this package follow the {@link org.jspecify.annotations.NullMarked} contract: members are non-null
 * unless explicitly annotated {@link org.jspecify.annotations.Nullable}.
 */
@NullMarked
package org.kosit.jaxb;

import org.jspecify.annotations.NullMarked;
