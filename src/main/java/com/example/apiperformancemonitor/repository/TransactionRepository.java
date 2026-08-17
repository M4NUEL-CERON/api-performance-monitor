package com.example.apiperformancemonitor.repository;

import com.example.apiperformancemonitor.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    long countByStatus(String status);

    @Query("SELECT t.status, COUNT(t) FROM Transaction t GROUP BY t.status")
    List<Object[]> countGroupedByStatus();

    @Query("SELECT t.currency, COUNT(t) FROM Transaction t GROUP BY t.currency")
    List<Object[]> countGroupedByCurrency();

    @Query("SELECT t.type, COUNT(t) FROM Transaction t GROUP BY t.type")
    List<Object[]> countGroupedByType();

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.status = 'COMPLETED'")
    Double getTotalCompletedVolume();
}
