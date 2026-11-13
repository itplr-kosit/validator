/**
 * DVR coordinates ({@code groupId:artifactId:version[:classifier]}), their versions and pseudo versions.
 * <p>
 * This package and {@link org.kosit.base.coord.version} are a port of the API module of ph-diver
 * (<a href="https://github.com/phax/ph-diver">github.com/phax/ph-diver</a>, {@code ph-diver-api}) by Philip Helger,
 * licensed under the Apache License 2.0, and are maintained here instead of depending on it. The port differs from the
 * original in two ways:
 * </p>
 * <ul>
 * <li>It depends on {@code org.kosit.base} and JSpecify instead of ph-commons, which the validator does not use.</li>
 * <li>Static versions are ordered with pre-release qualifiers
 * ({@link org.kosit.base.coord.version.EDVRPreReleaseQualifier}), so {@code 1.0.0-rc1} is older than {@code 1.0.0}. The
 * ordering of ph-diver up to v4.2.1, in which every qualifier but {@code SNAPSHOT} sorted after the release, remains
 * available as
 * {@link org.kosit.base.coord.version.DVRVersion#compareToClassic(org.kosit.base.coord.version.DVRVersion)}.</li>
 * </ul>
 * <p>
 * Only the API is ported. Retrieving an artifact by its coordinate - the repository side of ph-diver, which is what
 * gives a pseudo version such as {@code latest} a meaning - is not part of it; in the validator a coordinate identifies
 * an artifact, and the artifact is retrieved by its location.
 * </p>
 */
package org.kosit.base.coord;
