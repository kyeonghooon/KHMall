package com.khmall.domain.cart;

import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartRepository extends JpaRepository<Cart, Long> {

  Optional<Cart> findByUser_Id(Long userId);

  @EntityGraph(attributePaths = {"items", "items.product"})
  Optional<Cart> findWithItemsByUser_Id(Long userId);
}