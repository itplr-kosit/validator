package org.kosit.validator.scenario.generic;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.conformatron.api.annotation.Nonempty;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.kosit.base.ObjectHelper;
import org.kosit.base.string.StringHelper;

/**
 * A single block of a {@link ScenarioDescription}. Depending on the {@link EScenarioDescriptionBlockKind} it either
 * carries a single text ({@link EScenarioDescriptionBlockKind#TEXT} and
 * {@link EScenarioDescriptionBlockKind#PARAGRAPH}) or a list of items
 * ({@link EScenarioDescriptionBlockKind#ORDERED_LIST} and {@link EScenarioDescriptionBlockKind#UNORDERED_LIST}). This
 * class is immutable.
 *
 * @author Philip Helger
 */
public final class ScenarioDescriptionBlock {

    private final EScenarioDescriptionBlockKind kind;

    private final @Nullable String text;

    private final List<String> items;

    private ScenarioDescriptionBlock(@NonNull final EScenarioDescriptionBlockKind kind, final @Nullable String text,
            @NonNull final List<String> items) {
        this.kind = kind;
        this.text = text;
        this.items = List.copyOf(items);
    }

    /**
     * @return the kind of this block. Never <code>null</code>.
     */
    public @NonNull EScenarioDescriptionBlockKind getKind() {
        return this.kind;
    }

    /**
     * @return the text of this block. Never <code>null</code> if {@link #getKind()} is not a list kind,
     *         <code>null</code> otherwise.
     */
    public @Nullable String getText() {
        return this.text;
    }

    /**
     * @return all items of this block. Never <code>null</code>, but empty if {@link #getKind()} is not a list kind.
     */
    public @NonNull List<String> getItems() {
        return this.items;
    }

    /**
     * @return the content of this block as plain text. List items are separated by a newline. Never <code>null</code>.
     */
    public @NonNull String getAsPlainText() {
        if (this.kind.isList()) {
            return String.join("\n", this.items);
        }
        return this.text == null ? "" : this.text;
    }

    @Override
    public boolean equals(final Object o) {
        if (o == this) {
            return true;
        }
        if (o == null || !getClass().equals(o.getClass())) {
            return false;
        }
        final ScenarioDescriptionBlock rhs = (ScenarioDescriptionBlock) o;
        return this.kind == rhs.kind && Objects.equals(this.text, rhs.text) && this.items.equals(rhs.items);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.kind, this.text, this.items);
    }

    @Override
    public String toString() {
        return "ScenarioDescriptionBlock[kind=" + this.kind + (this.kind.isList() ? "; items=" + this.items : "; text=" + this.text) + "]";
    }

    /**
     * Create a block of free text without any markup.
     *
     * @param text the text to use. May neither be <code>null</code> nor empty.
     * @return never <code>null</code>.
     */
    public static @NonNull ScenarioDescriptionBlock text(@NonNull @Nonempty final String text) {
        if (StringHelper.isEmpty(text)) {
            throw new IllegalArgumentException("Text must not be empty");
        }
        return new ScenarioDescriptionBlock(EScenarioDescriptionBlockKind.TEXT, text, List.of());
    }

    /**
     * Create a paragraph block - the XML element "p".
     *
     * @param text the text to use. May neither be <code>null</code> nor empty.
     * @return never <code>null</code>.
     */
    public static @NonNull ScenarioDescriptionBlock paragraph(@NonNull @Nonempty final String text) {
        if (StringHelper.isEmpty(text)) {
            throw new IllegalArgumentException("Text must not be empty");
        }
        return new ScenarioDescriptionBlock(EScenarioDescriptionBlockKind.PARAGRAPH, text, List.of());
    }

    /**
     * Create an ordered list block - the XML element "ol".
     *
     * @param items the list items to use. May neither be <code>null</code> nor empty.
     * @return never <code>null</code>.
     */
    public static @NonNull ScenarioDescriptionBlock orderedList(@NonNull @Nonempty final List<String> items) {
        return list(EScenarioDescriptionBlockKind.ORDERED_LIST, items);
    }

    /**
     * Create an unordered list block - the XML element "ul".
     *
     * @param items the list items to use. May neither be <code>null</code> nor empty.
     * @return never <code>null</code>.
     */
    public static @NonNull ScenarioDescriptionBlock unorderedList(@NonNull @Nonempty final List<String> items) {
        return list(EScenarioDescriptionBlockKind.UNORDERED_LIST, items);
    }

    /**
     * Create a list block of the provided kind.
     *
     * @param kind the kind to use. May not be <code>null</code> and {@link EScenarioDescriptionBlockKind#isList()} must
     *            be <code>true</code>.
     * @param items the list items to use. May neither be <code>null</code> nor empty.
     * @return never <code>null</code>.
     */
    public static @NonNull ScenarioDescriptionBlock list(@NonNull final EScenarioDescriptionBlockKind kind,
            @NonNull @Nonempty final List<String> items) {
        ObjectHelper.requireNonNull(kind, "Kind");
        if (!kind.isList()) {
            throw new IllegalArgumentException("Kind " + kind + " is no list kind");
        }
        ObjectHelper.requireNonNull(items, "Items");
        if (items.isEmpty()) {
            throw new IllegalArgumentException("Items must not be empty");
        }
        return new ScenarioDescriptionBlock(kind, null, new ArrayList<>(items));
    }
}
