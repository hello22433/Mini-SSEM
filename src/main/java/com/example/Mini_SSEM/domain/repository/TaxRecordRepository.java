package com.example.Mini_SSEM.domain.repository;

import com.example.Mini_SSEM.domain.model.TaxRecord;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaxRecordRepository extends JpaRepository<TaxRecord, String> {
}
