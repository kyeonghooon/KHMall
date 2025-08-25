package com.khmall.domain.cart;

import jakarta.persistence.LockModeType;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

  Optional<CartItem> findByCart_IdAndProduct_Id(Long cartId, Long productId);

  List<CartItem> findByCart_Id(Long cartId);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("SELECT ci FROM CartItem ci WHERE ci.id IN :ids AND ci.cart.id = :cartId")
  List<CartItem> findAllByIdInAndCart_IdForUpdate(@Param("ids") Collection<Long> ids, @Param("cartId") Long cartId);

  long deleteByCart_IdAndIdIn(Long cartId, Collection<Long> ids);
}