package com.supash.inventory.model;

import java.time.Instant;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

@Document(collection = "quotations")
@Data
public class Quotation {

    @Id
    private String id;

    private String quotationNo;
    private String customerName;

    private double totalAmount;

    private String createdBy;
    private Instant createdAt;
    
    private String modifiedBy;
    private Instant modifiedAt;

    private List<QuotationItem> items;
}
