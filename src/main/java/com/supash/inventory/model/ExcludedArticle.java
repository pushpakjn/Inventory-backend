package com.supash.inventory.model;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

@Document(collection = "excluded_articles")
@Data
public class ExcludedArticle {

    @Id
    private String id;

    private String articleNo;

    private String createdBy;
    private Instant createdAt;

    // getters & setters
}
