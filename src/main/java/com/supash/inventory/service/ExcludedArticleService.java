package com.supash.inventory.service;

import java.time.Instant;
import java.util.List;

import org.springframework.stereotype.Service;

import com.supash.inventory.model.ExcludedArticle;
import com.supash.inventory.repo.ExcludedArticleRepository;

@Service
public class ExcludedArticleService {

    private final ExcludedArticleRepository repo;

    public ExcludedArticleService(ExcludedArticleRepository repo) {
        this.repo = repo;
    }

    // 🔹 ADD SINGLE
    public ExcludedArticle add(String articleNo, String admin) {
        articleNo = articleNo.trim();

        if (repo.existsByArticleNo(articleNo)) {
            throw new RuntimeException("Article already excluded");
        }

        ExcludedArticle e = new ExcludedArticle();
        e.setArticleNo(articleNo);
        e.setCreatedBy(admin);
        e.setCreatedAt(Instant.now());

        return repo.save(e);
    }

    // 🔹 ADD MULTIPLE (BULK)
    public List<ExcludedArticle> addBulk(
            List<String> articleNos,
            String admin
    ) {
        return articleNos.stream()
                .map(String::trim)
                .filter(a -> !a.isEmpty())
                .filter(a -> !repo.existsByArticleNo(a))
                .map(a -> {
                    ExcludedArticle e = new ExcludedArticle();
                    e.setArticleNo(a);
                    e.setCreatedBy(admin);
                    e.setCreatedAt(Instant.now());
                    return e;
                })
                .map(repo::save)
                .toList();
    }

    // 🔹 GET ALL (ADMIN)
    public List<ExcludedArticle> getAll() {
        return repo.findAll();
    }

    // 🔹 DELETE
    public void delete(String id) {
        repo.deleteById(id);
    }

    // 🔹 USED INTERNALLY FOR STOCK FILTER
    public List<String> getExcludedArticleNos() {
        return repo.findAll()
                   .stream()
                   .map(ExcludedArticle::getArticleNo)
                   .toList();
    }
}
