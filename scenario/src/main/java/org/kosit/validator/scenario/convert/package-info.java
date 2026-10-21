/**
 * Converting a scenario configuration from one version to another. The mapping itself belongs to the version packages;
 * what lives here is what a conversion needs beyond mapping, namely the
 * {@link org.kosit.validator.scenario.convert.CoordinateProposal}s for everything that version 3 requires a coordinate
 * for and version 2 was allowed to leave off.
 *
 * <p>
 * All types in this package follow the {@link org.jspecify.annotations.NullMarked} contract: members are non-null
 * unless explicitly annotated {@link org.jspecify.annotations.Nullable}.
 */
@NullMarked
package org.kosit.validator.scenario.convert;

import org.jspecify.annotations.NullMarked;
