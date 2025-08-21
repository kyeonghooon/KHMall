package com.khmall.domain.cart;

import com.khmall.domain.user.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import static lombok.AccessLevel.PROTECTED;

@EntityListeners(AuditingEntityListener.class)
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = PROTECTED)
@Entity
@Table(name = "cart")
public class Cart {

  @Id
  @Column(name = "cart_id")
  private Long id;

  @NotNull
  @OneToOne(fetch = FetchType.LAZY, optional = false)
  @MapsId
  @JoinColumn(name = "cart_id")
  private User user;

  // 장바구니 항목들
  @Builder.Default
  @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<CartItem> items = new ArrayList<>();

  @LastModifiedDate
  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;

  void addItem(CartItem item) {
    items.add(item);
    item.setCart(this);
    markAsUpdated();
  }

  void removeItem(CartItem item) {
    items.remove(item);
    item.setCart(null);
    markAsUpdated();
  }

  public void markAsUpdated() {
    this.updatedAt = LocalDateTime.now();
  }
}
