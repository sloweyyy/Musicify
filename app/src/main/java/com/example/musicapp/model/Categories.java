package com.example.musicapp.model;

import java.util.List;

public class Categories {
    private String href;
    private String id;
    private Icons[] icons;
    private String name;

    private List<Categories> items;

    public List<Categories> getItems() {
        return items;
    }

    public void setItems(List<Categories> items) {
        this.items = items;
    }

    public String getHref() {
        return href;
    }

    public void setHref(String href) {
        this.href = href;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Icons[] getIcons() {
        return icons;
    }

    public void setIcons(Icons[] icons) {
        this.icons = icons;
    }

    public String getImageUrl() {
        if (icons != null && icons.length > 0) {
            return icons[0].url;
        } else {
            return null;
        }
    }

    public static class Icons {
        public String url;
        public int height;
        public int width;
    }
}