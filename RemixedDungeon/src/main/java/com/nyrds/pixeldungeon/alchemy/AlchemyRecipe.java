package com.nyrds.pixeldungeon.alchemy;

import java.util.List;

/**
 * Pod class representing a single alchemy recipe
 * Contains input ingredients and output products with their respective counts
 */
public class AlchemyRecipe {
    private final List<InputItem> input;
    private final List<OutputItem> output;

    public AlchemyRecipe(List<InputItem> input, List<OutputItem> output) {
        this.input = input;
        this.output = output;
    }

    public List<InputItem> getInput() {
        return input;
    }

    public List<OutputItem> getOutput() {
        return output;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        AlchemyRecipe that = (AlchemyRecipe) obj;

        if (input != null ? !input.equals(that.input) : that.input != null) return false;
        return output != null ? output.equals(that.output) : that.output == null;
    }

    @Override
    public int hashCode() {
        int result = input != null ? input.hashCode() : 0;
        result = 31 * result + (output != null ? output.hashCode() : 0);
        return result;
    }
}