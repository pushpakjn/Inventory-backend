package com.supash.inventory.repo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.supash.inventory.model.Quotation;

public interface QuotationRepository extends MongoRepository<Quotation, String> {
	
	List<Quotation> findByCreatedByOrderByCreatedAtDesc(String createdBy);

	List<Quotation> findAllByOrderByCreatedAtDesc();
	
	Optional<Quotation> findTopByOrderByCreatedAtDesc();
}

