package com.nyrds.pixeldungeon.alchemy;

import java.util.List;
import java.util.Objects;

import lombok.Getter;

/**
 * Pod class representing a single alchemy recipe
 * Contains input ingredients and output products with their respective counts
 */
@Getter
public class AlchemyRecipe {
    private final List<InputItem> input;
    private final List<OutputItem> output;

    public AlchemyRecipe(List<InputItem> input, List<OutputItem> output) {
        this.input = input;
        this.output = output;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        AlchemyRecipe that = (AlchemyRecipe) obj;

        if (!Objects.equals(input, that.input)) return false;
        return Objects.equals(output, that.output);
    }

    @Override
    public int hashCode() {
        int result = input != null ? input.hashCode() : 0;
        result = 31 * result + (output != null ? output.hashCode() : 0);
        return result;
    }
}