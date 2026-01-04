package com.supash.inventory.dto;

import java.util.List;

import lombok.Data;

@Data
public class CreateQuotationRequest {

    private String customerName;
    private List<CreateQuotationItemRequest> items;
}
