package com.khmall.domain.payment;

import com.khmall.domain.order.Order;
import com.khmall.domain.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@Table(name = "pseudo_payment",
    uniqueConstraints = {@UniqueConstraint(name = "uk_payment_order", columnNames = "order_id")})
public class Payment {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "payment_id")
  private Long id;

  @OneToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "order_id", nullable = false)
  private Order order;

  @Enumerated(EnumType.STRING)
  @Column(name = "method", nullable = false, length = 20)
  @Builder.Default
  private PaymentMethod method = PaymentMethod.CARD;

  @Column(name = "amount", nullable = false)
  private long amount;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 20)
  @Builder.Default
  private PaymentStatus status = PaymentStatus.READY;

  @Column(name = "approved_at")
  private LocalDateTime approvedAt;

  @Column(name = "canceled_at")
  private LocalDateTime canceledAt;

  // 관리자가 취소한 경우만 값 존재
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "canceled_by")
  private User canceledBy;

  // 상태 전이 편의 메서드
  public void approve() {
    this.status = PaymentStatus.PAID;
    this.approvedAt = LocalDateTime.now();
  }

  public void cancel(User admin) {
    this.status = PaymentStatus.CANCEL;
    this.canceledAt = LocalDateTime.now();
    this.canceledBy = admin;
  }
}
