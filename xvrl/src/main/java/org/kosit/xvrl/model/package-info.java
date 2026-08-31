/**
 * The serialization independent XVRL data model.
 *
 * <p>
 * All types in this package are immutable and are created through the {@code builder()} methods of the respective type.
 * An existing instance can be turned back into a builder via {@code toBuilder()} to derive a modified copy. The model
 * carries no JAXB dependency at all; conversion from and to the JAXB representation is done by
 * {@link org.kosit.xvrl.jaxb.XvrlJaxbCreator} and {@link org.kosit.xvrl.jaxb.XvrlJaxbReader}.
 *
 * <p>
 * All types in this package follow the {@link org.jspecify.annotations.NullMarked} contract: members are non-null
 * unless explicitly annotated {@link org.jspecify.annotations.Nullable}.
 */
@NullMarked
package org.kosit.xvrl.model;

import org.jspecify.annotations.NullMarked;
