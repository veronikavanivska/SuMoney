package org.example.sumoney.repositories;

import io.lettuce.core.dynamic.annotation.Param;
import org.example.sumoney.entities.Expense;
import org.example.sumoney.repositories.projections.CategoryStatsProjection;
import org.example.sumoney.repositories.projections.CurrencyStatsProjection;
import org.example.sumoney.repositories.projections.MonthlyCategoryStatsProjection;
import org.example.sumoney.repositories.projections.MonthlyCurrencyStatsProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long>,
        JpaSpecificationExecutor<Expense> {

    Optional<Expense> findByIdAndDelegation_IdAndDelegation_User_Id(
            Long expenseId,
            Long delegationId,
            Long userId
    );
    List<Expense> findByDelegation_IdAndDelegation_User_IdOrderByExpenseDateAsc(
            Long delegationId,
            Long userId
    );

    @Query("""
        select
            e.currency as currency,
            sum(e.amount) as totalAmount,
            count(e.id) as expensesCount
        from Expense e
        where e.delegation.id = :delegationId
          and e.delegation.user.id = :userId
        group by e.currency
        order by e.currency
    """)
    List<CurrencyStatsProjection> getCurrencyStatsByDelegation(
            @Param("userId") Long userId,
            @Param("delegationId") Long delegationId
    );

    @Query("""
        select
            e.category as category,
            e.currency as currency,
            sum(e.amount) as totalAmount,
            count(e.id) as expensesCount
        from Expense e
        where e.delegation.id = :delegationId
          and e.delegation.user.id = :userId
        group by e.category, e.currency
        order by e.category, e.currency
    """)
    List<CategoryStatsProjection> getCategoryStatsByDelegation(
            @Param("userId") Long userId,
            @Param("delegationId") Long delegationId
    );

    @Query("""
        select
            year(e.expenseDate) as expenseYear,
            month(e.expenseDate) as expenseMonth,
            e.currency as currency,
            sum(e.amount) as totalAmount,
            count(e.id) as expensesCount
        from Expense e
        where e.delegation.user.id = :userId
          and e.expenseDate is not null
          and year(e.expenseDate) = :year
        group by year(e.expenseDate), month(e.expenseDate), e.currency
        order by year(e.expenseDate), month(e.expenseDate), e.currency
    """)
    List<MonthlyCurrencyStatsProjection> getMonthlyStats(
            @Param("userId") Long userId,
            @Param("year") Integer year
    );


    @Query("""
    select
        year(e.expenseDate) as expenseYear,
        month(e.expenseDate) as expenseMonth,
        e.currency as currency,
        sum(e.amount) as totalAmount,
        count(e.id) as expensesCount
    from Expense e
    where e.delegation.user.id = :userId
      and e.expenseDate is not null
      and year(e.expenseDate) = :year
    group by year(e.expenseDate), month(e.expenseDate), e.currency
    order by year(e.expenseDate), month(e.expenseDate), e.currency
""")
    List<MonthlyCurrencyStatsProjection> getMonthlyCurrencyStats(
            @Param("userId") Long userId,
            @Param("year") Integer year
    );

    @Query("""
    select
        year(e.expenseDate) as expenseYear,
        month(e.expenseDate) as expenseMonth,
        e.category as category,
        e.currency as currency,
        sum(e.amount) as totalAmount,
        count(e.id) as expensesCount
    from Expense e
    where e.delegation.user.id = :userId
      and e.expenseDate is not null
      and year(e.expenseDate) = :year
    group by year(e.expenseDate), month(e.expenseDate), e.category, e.currency
    order by year(e.expenseDate), month(e.expenseDate), e.category, e.currency
""")
    List<MonthlyCategoryStatsProjection> getMonthlyCategoryStats(
            @Param("userId") Long userId,
            @Param("year") Integer year
    );

}
