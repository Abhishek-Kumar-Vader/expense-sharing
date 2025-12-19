package com.splitwise.repository;

import com.splitwise.entity.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    @Query("SELECT e FROM Expense e WHERE e.group.id = :groupId")
    List<Expense> findByGroupId(@Param("groupId") Long groupId);

    @Query("SELECT DISTINCT e FROM Expense e LEFT JOIN e.splits s " +
            "WHERE e.paidBy.id = :userId OR s.user.id = :userId")
    List<Expense> findByUserId(@Param("userId") Long userId);
}
