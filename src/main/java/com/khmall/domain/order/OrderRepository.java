package com.khmall.domain.order;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrderRepository extends JpaRepository<Order, Long> {

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("""
        select o from Order o
        join fetch o.user u
        left join fetch o.items i
        left join fetch i.product p
        where o.id = :id
      """)
  Optional<Order> findGraphForUpdate(@Param("id") Long id);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select o from Order o where o.id = :id")
  Optional<Order> findByIdForUpdate(@Param("id") Long id);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("""
    select o from Order o
    join fetch o.user u
    where o.id = :id
  """)
  Optional<Order> findByIdForUpdateWithUser(@Param("id") Long id);
}