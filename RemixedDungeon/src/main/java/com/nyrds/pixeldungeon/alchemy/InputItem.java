package com.nyrds.pixeldungeon.alchemy;

/**
 * Data class representing an input item with its required quantity
 */
public class InputItem {
    private final String name;
    private final int count;

    public InputItem(String name) {
        this(name, 1);
    }

    public InputItem(String name, int count) {
        this.name = name;
        this.count = count <= 0 ? 1 : count; // Ensure count is at least 1
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

        InputItem inputItem = (InputItem) obj;

        if (count != inputItem.count) return false;
        return name != null ? name.equals(inputItem.name) : inputItem.name == null;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + count;
        return result;
    }

    @Override
    public String toString() {
        return "InputItem{" +
                "name='" + name + '\'' +
                ", count=" + count +
                '}';
    }
}