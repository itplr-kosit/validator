/**
 * The version independent ("generic") scenario data model. It covers the union of the requirements of the scenario
 * configuration versions 2 ({@code http://www.xoev.de/de/validator/framework/2/scenarios}) and 3
 * ({@code urn:kosit:validator:scenario:3}) and can be serialized into both of them.
 * <p>
 * The entry point is {@link org.kosit.validator.scenario.generic.ScenarioConfiguration}. Conversion from and to the
 * JAXB models is done by {@code org.kosit.validator.scenario.v2.Scenario2Mapper} and
 * {@code org.kosit.validator.scenario.v3.Scenario3Mapper}.
 * <p>
 * All types in this package follow the {@link org.jspecify.annotations.NullMarked} contract: members are non-null
 * unless explicitly annotated {@link org.jspecify.annotations.Nullable}.
 */
@NullMarked
package org.kosit.validator.scenario.generic;

import org.jspecify.annotations.NullMarked;
