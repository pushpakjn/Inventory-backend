package com.supash.inventory.service;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import com.supash.inventory.dto.StockResponseDTO;
import com.supash.inventory.model.ExcludedArticle;
import com.supash.inventory.model.Stock;
import com.supash.inventory.repo.ExcludedArticleRepository;
import com.supash.inventory.repo.StockRepository;

@Service
public class StockService {

    private final StockRepository stockRepo;
    private final ExcludedArticleRepository excludedRepo;

    public StockService(
            StockRepository stockRepo,
            ExcludedArticleRepository excludedRepo
    ) {
        this.stockRepo = stockRepo;
        this.excludedRepo = excludedRepo;
    }

    // ✅ Create stock (ADMIN)
    public Stock createStock(Stock stock, String username) {
        stock.setCreatedBy(username);
        stock.setModifiedBy(username);
        stock.setCreatedAt(Instant.now());
        stock.setUpdatedAt(Instant.now());
        return stockRepo.save(stock);
    }

    // ✅ MAIN METHOD: used by stock page
    public List<StockResponseDTO> getAllStock(Authentication auth) {

        boolean isAdmin = auth.getAuthorities()
                .stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        List<Stock> stocks;

        if (isAdmin) {
            // 🔥 ADMIN → see everything
            stocks = stockRepo.findByIsActiveTrue();

        } else {
            // 🔥 USER → exclude article numbers
            List<String> excludedArticleNos = excludedRepo.findAll()
                    .stream()
                    .map(ExcludedArticle::getArticleNo)
                    .toList();

            if (excludedArticleNos.isEmpty()) {
                stocks = stockRepo.findByIsActiveTrue();
            } else {
                stocks = stockRepo.findByArticleNoNotInAndIsActiveTrue(
                        excludedArticleNos
                );
            }
        }

        return stocks.stream()
                .map(this::toDto)
                .toList();
    }

    
 // UPDATE STOCK
    public Stock updateStock(String id, Stock updated, String username) {

        Stock stock = stockRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Stock not found"));

        stock.setArticleNo(updated.getArticleNo());
        stock.setVariant(updated.getVariant());
        stock.setNoOfBundles(updated.getNoOfBundles());
        stock.setBundleSize(updated.getBundleSize());
        stock.setModifiedBy(username);
        stock.setUpdatedAt(Instant.now());

        return stockRepo.save(stock);
    }

    // SOFT DELETE (recommended)
    public void deleteStock(String id, String username) {

        Stock stock = stockRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Stock not found"));

        stock.setActive(false);
        stock.setModifiedBy(username);
        stock.setUpdatedAt(Instant.now());

        stockRepo.save(stock);
    }


    private StockResponseDTO toDto(Stock stock) {
        StockResponseDTO dto = new StockResponseDTO();
        dto.setId(stock.getId());
        dto.setArticleNo(stock.getArticleNo());
        dto.setVariant(stock.getVariant());
        dto.setNoOfBundles(stock.getNoOfBundles());
        dto.setBundleSize(stock.getBundleSize());
        dto.setTotalQuantity(stock.getTotalQuantity());
        dto.setCreatedBy(stock.getCreatedBy());
        dto.setModifiedBy(stock.getModifiedBy());
        return dto;
    }
}
