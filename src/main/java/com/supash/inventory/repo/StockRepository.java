package com.supash.inventory.repo;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.supash.inventory.model.Stock;

public interface StockRepository extends MongoRepository<Stock, String> {

    // ADMIN: get everything
    List<Stock> findByIsActiveTrue();

    // USER: exclude article numbers
    List<Stock> findByArticleNoNotInAndIsActiveTrue(List<String> articleNos);
}
