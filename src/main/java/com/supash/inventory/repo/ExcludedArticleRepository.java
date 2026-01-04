package com.supash.inventory.repo;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.supash.inventory.model.ExcludedArticle;

public interface ExcludedArticleRepository
        extends MongoRepository<ExcludedArticle, String> {

    boolean existsByArticleNo(String articleNo);

    List<ExcludedArticle> findAllByArticleNoIn(List<String> articleNos);
}
