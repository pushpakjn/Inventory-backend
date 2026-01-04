package com.supash.inventory.model;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

@Document(collection = "stock")
@Data
public class Stock {

    @Id
    private String id;

    private String articleNo;
    private String variant; // color OR size
    private int noOfBundles;
    private int bundleSize;

    private String createdBy;
    private String modifiedBy;

    private Instant createdAt;
    private Instant updatedAt;

    private boolean isActive = true;

    // 🔴 derived field (NOT stored)
    public int getTotalQuantity() {
        return noOfBundles * bundleSize;
    }

    // getters & setters
}
