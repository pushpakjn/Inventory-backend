package com.supash.inventory.conroller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.supash.inventory.model.ExcludedArticle;
import com.supash.inventory.service.ExcludedArticleService;

@RestController
@RequestMapping("/admin/excluded-articles")
public class AdminExcludedArticleController {

    private final ExcludedArticleService service;

    public AdminExcludedArticleController(ExcludedArticleService service) {
        this.service = service;
    }

    // 🔹 ADD ONE
    @PostMapping
    public ResponseEntity<ExcludedArticle> add(
            @RequestBody String articleNo,
            Authentication auth
    ) {
        return ResponseEntity.ok(
                service.add(articleNo, auth.getName())
        );
    }

    // 🔹 ADD MANY
    @PostMapping("/bulk")
    public ResponseEntity<List<ExcludedArticle>> addBulk(
            @RequestBody List<String> articleNos,
            Authentication auth
    ) {
        return ResponseEntity.ok(
                service.addBulk(articleNos, auth.getName())
        );
    }

    // 🔹 LIST
    @GetMapping
    public List<ExcludedArticle> list() {
        return service.getAll();
    }

    // 🔹 DELETE
    @DeleteMapping("/{id}")
    public void delete(@PathVariable String id) {
        service.delete(id);
    }
}
