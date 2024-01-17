package com.developer.linkshortener.model;



import java.util.Optional;

@lombok.Data
public class Data {
    private String url;
    private Optional<String> title;
}
