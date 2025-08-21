package com.khmall.domain.cart;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

  Optional<CartItem> findByCart_IdAndProduct_Id(Long cartId, Long productId);

  List<CartItem> findByCart_Id(Long cartId);

  long deleteByCart_IdAndProduct_Id(Long cartId, Long productId);
  long deleteByCart_IdAndIdIn(Long cartId, Collection<Long> ids);
}