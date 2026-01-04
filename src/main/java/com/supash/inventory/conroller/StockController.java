package com.supash.inventory.conroller;
import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.supash.inventory.dto.StockResponseDTO;
import com.supash.inventory.model.Stock;
import com.supash.inventory.service.StockService;

@RestController
@RequestMapping("/stock")
public class StockController {

    private final StockService service;

    public StockController(StockService service) {
        this.service = service;
    }

    // ✅ GET STOCK (role-based visibility)
    @GetMapping
    public List<StockResponseDTO> getAllStock(Authentication authentication) {
        return service.getAllStock(authentication);
    }

    // ✅ ADD STOCK (admin + user)
    @PostMapping
    public Stock addStock(
            @RequestBody Stock stock,
            Authentication authentication
    ) {
        return service.createStock(stock, authentication.getName());
    }

    // ✅ UPDATE STOCK (admin + user)
    @PutMapping("/{id}")
    public Stock updateStock(
            @PathVariable String id,
            @RequestBody Stock stock,
            Authentication authentication
    ) {
        return service.updateStock(id, stock, authentication.getName());
    }

    // ✅ DELETE STOCK (soft delete)
    @DeleteMapping("/{id}")
    public void deleteStock(
            @PathVariable String id,
            Authentication authentication
    ) {
        service.deleteStock(id, authentication.getName());
    }
}

