package com.khmall.domain.order;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrderRepository extends JpaRepository<Order, Long> {

  @Query("""
      select o from Order o
      left join fetch o.items oi
      left join fetch oi.product p
      where o.id = :id
      """)
  Optional<Order> findWithItems(@Param("id") Long id);
}