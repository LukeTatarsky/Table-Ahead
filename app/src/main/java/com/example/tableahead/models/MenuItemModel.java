package com.example.tableahead.models;

import java.io.Serializable;

public class MenuItemModel implements Cloneable, Serializable {
    final String itemName;
    final String itemDescription;
    final String itemPrice;

    public MenuItemModel(String itemName, String itemDescription, String itemPrice) {
        this.itemName = itemName;
        this.itemDescription = itemDescription;
        this.itemPrice = itemPrice;
    }


    public String getItemName() {
        return itemName;
    }

    public String getItemDescription() {
        return itemDescription;
    }

    public String getItemPrice() {
        return itemPrice;
    }

    /** @noinspection MethodDoesntCallSuperMethod, NullableProblems */
    @Override
    public MenuItemModel clone() {
        return new MenuItemModel(getItemName(), getItemDescription(), getItemPrice());
    }

    /** @noinspection NullableProblems*/
    @Override
    public String toString() {
        return "MenuItemModel{ " +
                "name='" + itemName + '\'' +
                ", price='" + itemPrice + '\'' +
                '}';
    }

}
