/*
 * Copyright 2017-2022  Koordinierungsstelle für IT-Standards (KoSIT)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * Generic, reusable JAXB read/write helper.
 *
 * <p>
 * {@link org.kosit.jaxb.JaxbConversionService} marshals and unmarshals JAXB-annotated objects to and from a wide
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
