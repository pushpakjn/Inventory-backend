package com.supash.inventory.dto;

import lombok.Data;

@Data
public class CreateQuotationItemRequest {

    private String stockId;
    private int bundlesSold;
    private double costPerPc;
}

