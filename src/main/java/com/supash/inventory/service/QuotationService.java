package com.supash.inventory.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.supash.inventory.dto.CreateQuotationItemRequest;
import com.supash.inventory.dto.CreateQuotationRequest;
import com.supash.inventory.model.Quotation;
import com.supash.inventory.model.QuotationItem;
import com.supash.inventory.model.Stock;
import com.supash.inventory.repo.QuotationRepository;
import com.supash.inventory.repo.StockRepository;

@Service
public class QuotationService {

    private final QuotationRepository quotationRepo;
    private final StockRepository stockRepo;

    public QuotationService(
            QuotationRepository quotationRepo,
            StockRepository stockRepo
    ) {
        this.quotationRepo = quotationRepo;
        this.stockRepo = stockRepo;
    }

    public Quotation createQuotation(
            CreateQuotationRequest request,
            String username
    ) {
        List<QuotationItem> items = new ArrayList<>();
        double grandTotal = 0;

        for (CreateQuotationItemRequest i : request.getItems()) {

            Stock stock = stockRepo.findById(i.getStockId())
                    .orElseThrow(() -> new RuntimeException("Stock not found"));

            int availableBundles = stock.getNoOfBundles();
            if (i.getBundlesSold() > availableBundles) {
                throw new RuntimeException("Not enough stock");
            }

            int qtySold = i.getBundlesSold() * stock.getBundleSize();
            double itemTotal = qtySold * i.getCostPerPc();

            // Deduct stock
            stock.setNoOfBundles(availableBundles - i.getBundlesSold());
            stockRepo.save(stock);

            QuotationItem qi = new QuotationItem();
            qi.setStockId(stock.getId());
            qi.setArticleNo(stock.getArticleNo());
            qi.setVariant(stock.getVariant());
            qi.setBundlesSold(i.getBundlesSold());
            qi.setQuantitySold(qtySold);
            qi.setCostPerPc(i.getCostPerPc());
            qi.setItemTotal(itemTotal);

            items.add(qi);
            grandTotal += itemTotal;
        }

        Quotation q = new Quotation();
        q.setQuotationNo(generateQuotationNo());
        q.setCustomerName(request.getCustomerName());
        q.setItems(items);
        q.setTotalAmount(grandTotal);
        q.setCreatedBy(username);
        q.setCreatedAt(Instant.now());
        q.setModifiedBy(null);
        q.setModifiedAt(null);

        return quotationRepo.save(q);
    }

    public List<Quotation> getQuotations(Authentication auth) {
        boolean isAdmin = auth.getAuthorities()
                .stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (isAdmin) {
            return quotationRepo.findAllByOrderByCreatedAtDesc();
        }
        return quotationRepo.findByCreatedByOrderByCreatedAtDesc(auth.getName());
    }
    
    public Quotation getQuotation(String id, Authentication auth) {

        Quotation quotation = quotationRepo.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Quotation not found"
                        )
                );

        boolean isAdmin = auth.getAuthorities()
                .stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin && !quotation.getCreatedBy().equals(auth.getName())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You are not allowed to view this quotation"
            );
        }

        return quotation;
    }
    
    @Transactional
    public Quotation updateQuotation(
            String quotationId,
            CreateQuotationRequest request,
            Authentication auth
    ) {

        Quotation existing = quotationRepo.findById(quotationId)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Quotation not found")
                );

        boolean isAdmin = auth.getAuthorities()
                .stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin && !existing.getCreatedBy().equals(auth.getName())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Forbidden");
        }

        /* ================= 1. RESTORE OLD STOCK ================= */
        for (QuotationItem oldItem : existing.getItems()) {

            Stock stock = stockRepo.findById(oldItem.getStockId())
                    .orElseThrow(() ->
                            new ResponseStatusException(HttpStatus.NOT_FOUND, "Stock not found")
                    );

            int bundlesToRestore =
                    oldItem.getQuantitySold() / stock.getBundleSize();

            stock.setNoOfBundles(
                    stock.getNoOfBundles() + bundlesToRestore
            );

            stockRepo.save(stock);
        }

        /* ================= 2. APPLY NEW ITEMS ================= */
        List<QuotationItem> newItems = new ArrayList<>();
        double grandTotal = 0;

        for (CreateQuotationItemRequest i : request.getItems()) {

            Stock stock = stockRepo.findById(i.getStockId())
                    .orElseThrow(() ->
                            new ResponseStatusException(HttpStatus.NOT_FOUND, "Stock not found")
                    );

            if (i.getBundlesSold() > stock.getNoOfBundles()) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Insufficient stock for article " + stock.getArticleNo()
                );
            }

            int qtySold = i.getBundlesSold() * stock.getBundleSize();
            double itemTotal = qtySold * i.getCostPerPc();

            // 🔻 Deduct stock again
            stock.setNoOfBundles(
                    stock.getNoOfBundles() - i.getBundlesSold()
            );
            stockRepo.save(stock);

            QuotationItem qi = new QuotationItem();
            qi.setStockId(stock.getId());
            qi.setArticleNo(stock.getArticleNo());
            qi.setVariant(stock.getVariant());
            qi.setBundlesSold(i.getBundlesSold());
            qi.setQuantitySold(qtySold);
            qi.setCostPerPc(i.getCostPerPc());
            qi.setItemTotal(itemTotal);

            newItems.add(qi);
            grandTotal += itemTotal;
        }

        /* ================= 3. UPDATE QUOTATION ================= */
        existing.setCustomerName(request.getCustomerName());
        existing.setItems(newItems);
        existing.setTotalAmount(grandTotal);

        existing.setModifiedBy(auth.getName());
        existing.setModifiedAt(Instant.now());

        return quotationRepo.save(existing);
    }

    @Transactional
    public void deleteQuotation(String id, Authentication auth) {

        Quotation q = quotationRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Quotation not found"));

        boolean isAdmin = auth.getAuthorities()
                .stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        // 🔐 Permission check
        if (!isAdmin && !q.getCreatedBy().equals(auth.getName())) {
            throw new RuntimeException("Forbidden");
        }

        // 🔄 RESTORE STOCK
        for (QuotationItem item : q.getItems()) {

            Stock stock = stockRepo.findById(item.getStockId())
                    .orElseThrow(() ->
                            new RuntimeException("Stock not found: " + item.getStockId())
                    );

            stock.setNoOfBundles(
                    stock.getNoOfBundles() + item.getBundlesSold()
            );

//            stock.setTotalQuantity(
//                    stock.getTotalQuantity() + item.getQuantitySold()
//            );

            stock.setModifiedBy(auth.getName());
            stock.setUpdatedAt(Instant.now());

            stockRepo.save(stock);
        }

        // 🗑️ DELETE QUOTATION
        quotationRepo.delete(q);
    }


    public String generateQuotationNo() {

        Optional<Quotation> last = quotationRepo.findTopByOrderByCreatedAtDesc();

        if (last.isEmpty()) {
            return "QT/PI/00001";
        }

        String lastNo = last.get().getQuotationNo(); // QT/PI/00023
        String numericPart = lastNo.replace("QT/PI/", ""); // 00023

        int next = Integer.parseInt(numericPart) + 1;

        return String.format("QT/PI/%05d", next);
    }


}
