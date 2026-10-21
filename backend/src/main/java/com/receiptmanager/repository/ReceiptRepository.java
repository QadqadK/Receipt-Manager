package com.receiptmanager.repository;

import com.receiptmanager.entity.Receipt;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ReceiptRepository extends JpaRepository<Receipt, Long> {

    Page<Receipt> findByUserIdOrderByReceiptDateDesc(Long userId, Pageable pageable);

    Optional<Receipt> findByIdAndUserId(Long id, Long userId);

    @Query("select distinct r from Receipt r left join fetch r.items " +
            "where r.user.id = :userId and r.receiptDate between :from and :to " +
            "order by r.receiptDate asc, r.id asc")
    List<Receipt> findByUserIdAndDateRangeWithItems(
            @Param("userId") Long userId,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to);
}
