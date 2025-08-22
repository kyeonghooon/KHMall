package com.khmall.domain.order;

import static com.khmall.domain.order.QOrder.order;

import com.khmall.domain.order.dto.OrderListView;
import com.khmall.domain.order.dto.OrderSearchCond;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class OrderQueryRepository {

  private final JPAQueryFactory query;

  public Page<OrderListView> search(OrderSearchCond cond, Pageable pageable) {
    BooleanExpression owner = cond.userId() != null ? order.user.id.eq(cond.userId()) : null;
    BooleanExpression st = cond.status() != null ? order.status.eq(cond.status()) : null;

    LocalDateTime from = cond.from() != null ? cond.from().atStartOfDay() : null;
    LocalDateTime to = cond.to() != null ? cond.to().atTime(23, 59, 59) : null;
    BooleanExpression range =
        from != null && to != null ? order.createdAt.between(from, to)
            : from != null ? order.createdAt.goe(from)
                : to != null ? order.createdAt.loe(to) : null;

    List<OrderListView> content = query
        .select(Projections.constructor(OrderListView.class,
            order.id, order.status, order.totalPrice, order.createdAt))
        .from(order)
        .where(owner, st, range)
        .orderBy(order.id.desc())
        .offset(pageable.getOffset())
        .limit(pageable.getPageSize())
        .fetch();

    Long total = query.select(order.count())
        .from(order)
        .where(owner, st, range)
        .fetchOne();

    return new PageImpl<>(content, pageable, total == null ? 0 : total);
  }
}
