package com.khmall.common;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@Getter
public abstract class BaseAuditEntity extends BaseTimeEntity {

  @CreatedBy
  @Column(name = "created_by", updatable = false, nullable = false)
  private Long createdBy;

  @LastModifiedBy
  @Column(name = "updated_by", nullable = false)
  private Long updatedBy;
}
