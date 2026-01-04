package com.supash.inventory.model;

import lombok.Data;

@Data
public class QuotationItem {

    private String stockId;
    private String articleNo;
    private String variant;

    private int bundlesSold;
    private int quantitySold;

    private double costPerPc;
    private double itemTotal;
}

