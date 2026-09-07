package org.kosit.base;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

public class ObjectHelperTest {

    @Test
    public void requireNonNullReturnsTheValue() {
        final String value = "content";
        assertThat(ObjectHelper.requireNonNull(value, "Value")).isSameAs(value);
    }

    @Test
    public void requireNonNullNamesTheParameter() {
        assertThatThrownBy(() -> ObjectHelper.requireNonNull(null, "Value")).isInstanceOf(NullPointerException.class)
                .hasMessage("Value must not be null");
    }

    @Test
    public void compareHandlesNull() {
        assertThat(ObjectHelper.compare(null, null)).isZero();
        assertThat(ObjectHelper.compare(null, "a")).isNegative();
        assertThat(ObjectHelper.compare("a", null)).isPositive();
    }

    @Test
    public void compareNonNullValues() {
        assertThat(ObjectHelper.compare("a", "a")).isZero();
        assertThat(ObjectHelper.compare("a", "b")).isNegative();
        assertThat(ObjectHelper.compare("b", "a")).isPositive();
        assertThat(ObjectHelper.compare(Integer.valueOf(1), Integer.valueOf(2))).isNegative();
    }

    @Test
    public void compareIsUsableForSorting() {
        final List<String> values = new ArrayList<>(Arrays.asList("b", null, "a"));
        values.sort(ObjectHelper::compare);
        assertThat(values).containsExactly(null, "a", "b");
    }

    @Test
    public void uncheckedCast() {
        final Object source = "content";
        final String target = ObjectHelper.uncheckedCast(source);
        assertThat(target).isSameAs(source);
        assertThat((String) ObjectHelper.uncheckedCast(null)).isNull();
    }

    @Test
    public void uncheckedCastFailsOnUse() {
        final Object source = "content";
        assertThatThrownBy(() -> {
            final Integer target = ObjectHelper.uncheckedCast(source);
            assertThat(target).isNotNull();
        }).isInstanceOf(ClassCastException.class);
    }
}
