package com.smpt;

import java.util.List;


public class WikiResult {
    private String extract;
    private List<String> imageLinks;

    public WikiResult(String extract, List<String> imageLinks) {
        this.extract = extract;
        this.imageLinks = imageLinks;
    }

    // Gettery
    public String getExtract() {
        return extract;
    }

    public List<String> getImageLinks() {
        return imageLinks;
    }
}