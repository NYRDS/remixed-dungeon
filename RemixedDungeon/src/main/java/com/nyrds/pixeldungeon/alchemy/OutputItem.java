package com.nyrds.pixeldungeon.alchemy;

/**
 * Data class representing an output item with its produced quantity
 */
public class OutputItem {
    private final String name;
    private final int count;

    public OutputItem(String name, int count) {
        this.name = name;
        this.count = count;
    }

    public String getName() {
        return name;
    }

    public int getCount() {
        return count;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        OutputItem outputItem = (OutputItem) obj;

        if (count != outputItem.count) return false;
        return name != null ? name.equals(outputItem.name) : outputItem.name == null;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + count;
        return result;
    }

    @Override
    public String toString() {
        return "OutputItem{" +
                "name='" + name + '\'' +
                ", count=" + count +
                '}';
    }
}