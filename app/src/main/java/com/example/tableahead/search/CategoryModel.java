package com.example.tableahead.search;
/**
 * Model class to store data related to the categories from the database
 * @author Ethan Rody
 */
public class CategoryModel {
    final String name;
    final String description;
    final String id;

    public CategoryModel(String name, String description, String id) {
        this.name = name;
        this.description = description;
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getId() {
        return id;
    }


}
