package org.kosit.validator.cmd;

import static org.apache.commons.lang3.StringUtils.defaultIfBlank;

import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.kosit.validator.cmd.CommandLineOptions.AbstractDefinition;
import org.kosit.validator.cmd.CommandLineOptions.RepositoryDefinition;
import org.kosit.validator.cmd.CommandLineOptions.ScenarioDefinition;
import org.kosit.validator.impl.ScenarioRepository;

import picocli.CommandLine.ITypeConverter;

/**
 * Custom type converters for dealing with command line input.
 * 
 * @author Andreas Penski
 */
class TypeConverter {

    private TypeConverter() {
    }

    /**
     * Type converter for a repository definition specification e.g. '-r somelocation.xml OR -r myid=somelocation.xml'
     *
     * @author Andreas Penski
     */
    public static class RepositoryConverter implements ITypeConverter<RepositoryDefinition> {

        @Override
        public RepositoryDefinition convert(final String value) throws Exception {
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
        public ScenarioDefinition convert(final String value) throws Exception {
            return TypeConverter.convert(ScenarioDefinition.class, value);
        }
    }

    static final Map<Class<?>, AtomicInteger> counter = new HashMap<>();

    private static String getDefaultName(final Class<?> type) {
        final AtomicInteger current = counter.computeIfAbsent(type, a -> new AtomicInteger(1));
        return ScenarioRepository.DEFAULT + "_" + current.getAndIncrement();
    }

    private static <T extends AbstractDefinition> T convert(final Class<T> type, final String value) {
        final T def;
        final String[] splitted = defaultIfBlank(value, "").split("=");
        if (splitted.length == 1) {
            def = createNewInstance(type);
            def.setName(getDefaultName(type));
            def.setPath(Paths.get(splitted[0].trim()));
        } else if (splitted.length == 2) {
            def = createNewInstance(type);
            def.setName(splitted[0].trim());
            def.setPath(Paths.get(splitted[1].trim()));
        } else {
            throw new IllegalArgumentException("Not a valid repository specification " + value);
        }
        return def;
    }

    private static <T extends AbstractDefinition> T createNewInstance(final Class<T> type) {
        try {
            return type.getConstructor().newInstance();
        } catch (final ReflectiveOperationException e) {
            throw new IllegalStateException("Error creating instance of type " + type);
        }
    }
}
