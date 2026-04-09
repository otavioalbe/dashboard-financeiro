package com.example.demo.repository;

import com.example.demo.document.TransactionRecord;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface TransactionRecordRepository extends MongoRepository<TransactionRecord, String> {

    List<TransactionRecord> findByUserIdAndTypeOrderByAmountDesc(String userId, String type, Pageable pageable);
}
