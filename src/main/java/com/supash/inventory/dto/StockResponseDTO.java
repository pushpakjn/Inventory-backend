package com.supash.inventory.dto;

import lombok.Data;

@Data
public class StockResponseDTO {

    private String id;
    private String articleNo;
    private String variant;
    private int noOfBundles;
    private int bundleSize;
    private int totalQuantity;
    private String createdBy;
    private String modifiedBy;

    // getters & setters
}
