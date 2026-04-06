package com.example.demo.repository;

import com.example.demo.entity.Transaction;
import com.example.demo.entity.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, String> {
    List<Transaction> findByUserIdOrderByCreatedAtDesc(String userId);
    List<Transaction> findByAccountNumberOrderByCreatedAtDesc(String accountNumber);
    List<Transaction> findByUserIdAndTypeOrderByCreatedAtDesc(String userId, TransactionType type);
}
