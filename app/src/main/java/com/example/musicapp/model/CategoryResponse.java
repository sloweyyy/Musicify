package com.example.musicapp.model;

import com.example.musicapp.fragment.ExploreFragment;

public class CategoryResponse {
    private Categories categories;

    public Categories getCategories() {
        return categories;
    }

    public void setCategories(Categories categories) {
        this.categories = categories;
    }

    @Override
    public String toString() {
        return "CategoryResponse{" + "categories=" + categories + '}';
    }
}