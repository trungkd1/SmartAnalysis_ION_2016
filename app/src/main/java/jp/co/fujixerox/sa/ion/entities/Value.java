package jp.co.fujixerox.sa.ion.entities;

import java.util.List;

/**
 * Json value of Item
 * @see Item
 */
public class Value {
    private String value;
    private String text;
    private List<Item> items;

    public Value() {

    }

    public Value(String value, String text) {
        this.value = value;
        this.text = text;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public List<Item> getItems() {
        return items;
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }

    public String toString() {
        return text;
    }

}
