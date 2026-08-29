package org.kosit.validator.scenario.generic;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.conformatron.api.annotation.Nonempty;
import org.jspecify.annotations.NonNull;
import org.kosit.base.ObjectHelper;

/**
 * The description of a {@link ScenarioConfiguration} or of a single {@link Scenario}. It is an ordered list of
 * {@link ScenarioDescriptionBlock} objects.
 *
 * @author Philip Helger
 */
public class ScenarioDescription {

    private final List<ScenarioDescriptionBlock> blocks = new ArrayList<>();

    /**
     * @return the modifiable list of all contained blocks. Never <code>null</code>.
     */
    public @NonNull List<ScenarioDescriptionBlock> getBlocks() {
        return this.blocks;
    }

    /**
     * @return <code>true</code> if at least one block is contained, <code>false</code> if not.
     */
    public boolean isNotEmpty() {
        return !this.blocks.isEmpty();
    }

    /**
     * Add an arbitrary block.
     *
     * @param block the block to add. May not be <code>null</code>.
     * @return this for chaining
     */
    public @NonNull ScenarioDescription add(@NonNull final ScenarioDescriptionBlock block) {
        ObjectHelper.requireNonNull(block, "Block");
        this.blocks.add(block);
        return this;
    }

    /**
     * Add a block of free text without any markup.
     *
     * @param text the text to add. May neither be <code>null</code> nor empty.
     * @return this for chaining
     */
    public @NonNull ScenarioDescription addText(@NonNull @Nonempty final String text) {
        return add(ScenarioDescriptionBlock.text(text));
    }

    /**
     * Add a paragraph block.
     *
     * @param text the text to add. May neither be <code>null</code> nor empty.
     * @return this for chaining
     */
    public @NonNull ScenarioDescription addParagraph(@NonNull @Nonempty final String text) {
        return add(ScenarioDescriptionBlock.paragraph(text));
    }

    /**
     * Add an ordered list block.
     *
     * @param items the list items to add. May neither be <code>null</code> nor empty.
     * @return this for chaining
     */
    public @NonNull ScenarioDescription addOrderedList(@NonNull @Nonempty final List<String> items) {
        return add(ScenarioDescriptionBlock.orderedList(items));
    }

    /**
     * Add an unordered list block.
     *
     * @param items the list items to add. May neither be <code>null</code> nor empty.
     * @return this for chaining
     */
    public @NonNull ScenarioDescription addUnorderedList(@NonNull @Nonempty final List<String> items) {
        return add(ScenarioDescriptionBlock.unorderedList(items));
    }

    /**
     * @return the content of all blocks as plain text, separated by a newline. Never <code>null</code>.
     */
    public @NonNull String getAsPlainText() {
        return this.blocks.stream().map(ScenarioDescriptionBlock::getAsPlainText).collect(Collectors.joining("\n"));
    }

    @Override
    public String toString() {
        return "ScenarioDescription[blocks=" + this.blocks + "]";
    }

    /**
     * Convenience factory creating a description consisting of a single free text block.
     *
     * @param text the text to use. May neither be <code>null</code> nor empty.
     * @return never <code>null</code>.
     */
    public static @NonNull ScenarioDescription ofText(@NonNull @Nonempty final String text) {
        return new ScenarioDescription().addText(text);
    }

    /**
     * Convenience factory creating a description consisting of one paragraph per provided text.
     *
     * @param texts the paragraph texts to use. May not be <code>null</code>.
     * @return never <code>null</code>.
     */
    public static @NonNull ScenarioDescription ofParagraphs(@NonNull final List<String> texts) {
        ObjectHelper.requireNonNull(texts, "Texts");
        final ScenarioDescription ret = new ScenarioDescription();
        texts.forEach(ret::addParagraph);
        return ret;
    }
}
