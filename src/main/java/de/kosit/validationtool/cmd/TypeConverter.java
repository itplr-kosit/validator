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

package de.kosit.validationtool.cmd;

import de.kosit.validationtool.cmd.CommandLineOptions.Definition;
import de.kosit.validationtool.cmd.CommandLineOptions.RepositoryDefinition;
import de.kosit.validationtool.cmd.CommandLineOptions.ScenarioDefinition;
import de.kosit.validationtool.impl.ScenarioRepository;
import picocli.CommandLine.ITypeConverter;

import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.apache.commons.lang3.StringUtils.defaultIfBlank;

/**
 * Custom type converters for dealing with command line input.
 *
 * @author Andreas Penski
 */
class TypeConverter {

    final static Map<Class<?>, AtomicInteger> counter = new HashMap<>();

    private static String getDefaultName(final Class<?> type) {
        final AtomicInteger current = counter.computeIfAbsent(type, a -> new AtomicInteger(1));
        return ScenarioRepository.DEFAULT + "_" + current.getAndIncrement();
    }

    private static <T extends Definition> T convert(final Class<T> type, final String value) {
        final T def;
        final String[] items = defaultIfBlank(value, "").split("=");
        if (items.length == 1) {
            def = createNewInstance(type);
            def.setName(getDefaultName(type));
            def.setPath(Paths.get(items[0].trim()));
        } else if (items.length == 2) {
            def = createNewInstance(type);
            def.setName(items[0].trim());
            def.setPath(Paths.get(items[1].trim()));
        } else {
            throw new IllegalArgumentException("Not a valid repository specification " + value);
        }
        return def;
    }

    private static <T extends Definition> T createNewInstance(final Class<T> type) {
        try {
            return type.getConstructor().newInstance();
        } catch (final ReflectiveOperationException e) {
            throw new IllegalStateException("Error creating instance of type " + type);
        }
    }

    /**
     * Type converter for a repository definition specification e.g. '-r somelocation.xml OR -r myid=somelocation.xml'
     *
     * @author Andreas Penski
     */
    public static class RepositoryConverter implements ITypeConverter<RepositoryDefinition> {

        @Override
        public RepositoryDefinition convert(final String value) {
            return TypeConverter.convert(RepositoryDefinition.class, value);
        }
    }

    /**
     * Type converter for a scenario definition specification e.g. '-s somelocation.xml OR -s myid=somelocation.xml'
     *
     * @author Andreas Penski
     */
    public static class ScenarioConverter implements ITypeConverter<ScenarioDefinition> {

        @Override
        public ScenarioDefinition convert(final String value) {
            return TypeConverter.convert(ScenarioDefinition.class, value);
        }
    }
}
