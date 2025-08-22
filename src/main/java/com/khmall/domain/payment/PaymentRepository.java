package com.khmall.domain.payment;

import jakarta.persistence.LockModeType;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("""
        select p from Payment p
        join fetch p.order o
        join fetch o.user u
        where p.id = :id
      """)
  Optional<Payment> findByIdForUpdate(@Param("id") Long id);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("""
    select p from Payment p
    where p.order.id = :orderId and p.status in :statuses
  """)
  Optional<Payment> findForUpdateByOrderIdAndStatusIn(@Param("orderId") Long orderId,
      @Param("statuses") Collection<PaymentStatus> statuses);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("""
    select p from Payment p
    where p.order.id = :orderId and p.status = 'PAID'
  """)
  Optional<Payment> findPaidForUpdateByOrderId(@Param("orderId") Long orderId);
}