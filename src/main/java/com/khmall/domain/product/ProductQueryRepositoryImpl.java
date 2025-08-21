package com.khmall.domain.product;

import static com.khmall.domain.product.QProduct.product;
import static com.khmall.domain.category.QCategory.category;
import static com.khmall.domain.inventory.QInventory.inventory;

import com.khmall.domain.product.dto.AdminProductDetailResponse;
import com.khmall.domain.product.dto.AdminProductListView;
import com.khmall.domain.product.dto.CustomerProductDetailResponse;
import com.khmall.domain.product.dto.CustomerProductListView;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.ComparableExpressionBase;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import jakarta.persistence.EntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

public class ProductQueryRepositoryImpl implements ProductQueryRepository {

  private final JPAQueryFactory query;

  public ProductQueryRepositoryImpl(EntityManager em) {
    this.query = new JPAQueryFactory(em);
  }

  private static Expression<String> NULL_STR() { return Expressions.nullExpression(String.class); }
  private static Expression<Boolean> FALSE() { return Expressions.constant(Boolean.FALSE); }

  private static BooleanBuilder whereAdmin(String keyword, ProductStatus status,
      boolean noCategoryFilter, Collection<Long> categoryIds) {
    BooleanBuilder w = new BooleanBuilder();

    if (keyword != null && !keyword.isBlank()) {
      w.and(product.name.contains(keyword));
    }
    if (status != null) {
      w.and(product.status.eq(status));
    }
    if (!noCategoryFilter) {
      // categoryIds가 비었으면 결과가 0이어야 하므로 불가능식 추가
      if (categoryIds == null || categoryIds.isEmpty()) {
        w.and(Expressions.FALSE.isTrue()); // always false
      } else {
        w.and(category.id.in(categoryIds));
      }
    }
    return w;
  }

  private static BooleanBuilder whereCustomer(String keyword,
      boolean noCategoryFilter, Collection<Long> categoryIds) {
    BooleanBuilder w = new BooleanBuilder();
    w.and(product.status.eq(ProductStatus.ON_SALE));

    if (keyword != null && !keyword.isBlank()) {
      w.and(product.name.contains(keyword));
    }
    if (!noCategoryFilter) {
      if (categoryIds == null || categoryIds.isEmpty()) {
        w.and(Expressions.FALSE.isTrue());
      } else {
        w.and(category.id.in(categoryIds));
      }
    }
    return w;
  }

  private static OrderSpecifier<?>[] orderBy(Pageable pageable) {
    if (pageable == null || pageable.getSort().isEmpty()) {
      return new com.querydsl.core.types.OrderSpecifier<?>[]{ product.createdAt.desc() };
    }
    // 필요한 필드만 매핑
    Map<String, ComparableExpressionBase<?>> map = Map.of(
        "createdAt", product.createdAt,
        "price",     product.price,
        "name",      product.name
    );
    return pageable.getSort().stream()
        .map(o -> {
          var expr = map.getOrDefault(o.getProperty(), product.createdAt);
          return o.isAscending() ? expr.asc() : expr.desc();
        })
        .toArray(OrderSpecifier[]::new);
  }

  // 관리자 상세
  @Override
  public Optional<AdminProductDetailResponse> findAdminProductDetailById(Long id) {
    AdminProductDetailResponse dto = query
        .select(Projections.constructor(AdminProductDetailResponse.class,
            product.id,
            product.name,
            product.description,
            product.imageKey,
            NULL_STR(),              // imageUrl (서비스에서 주입)
            product.price,
            product.status,
            category.id,
            category.name,
            NULL_STR(),              // categoryPath (서비스에서 주입)
            inventory.quantity,
            product.createdAt,
            product.updatedAt,
            product.createdBy,
            product.updatedBy
        ))
        .from(product)
        .leftJoin(product.category, category)
        .leftJoin(inventory).on(inventory.product.id.eq(product.id))
        .where(product.id.eq(id))
        .fetchOne();

    return Optional.ofNullable(dto);
  }

  // 관리자 목록
  @Override
  public Page<AdminProductListView> findAdminProductList(
      String keyword, ProductStatus status, boolean noCategoryFilter,
      Collection<Long> categoryIds, Pageable pageable) {

    BooleanBuilder where = whereAdmin(keyword, status, noCategoryFilter, categoryIds);

    List<AdminProductListView> content = query
        .select(Projections.constructor(AdminProductListView.class,
            product.id,
            product.name,
            product.price,
            product.status,
            category.id,
            category.name,
            NULL_STR(),          // categoryPath
            inventory.quantity,
            product.createdAt
        ))
        .from(product)
        .leftJoin(product.category, category)
        .leftJoin(inventory).on(inventory.product.id.eq(product.id))
        .where(where)
        .orderBy(orderBy(pageable))
        .offset(pageable.getOffset())
        .limit(pageable.getPageSize())
        .fetch();

    Long total = query
        .select(product.id.count())
        .from(product)
        .leftJoin(product.category, category)
        .where(where)
        .fetchOne();

    return new PageImpl<>(content, pageable, total == null ? 0 : total);
  }

  // 고객 상세
  @Override
  public Optional<CustomerProductDetailResponse> findCustomerProductDetailById(Long id) {
    var dto = query
        .select(Projections.constructor(CustomerProductDetailResponse.class,
            product.id,
            product.name,
            product.description,
            product.price,
            product.imageKey,
            NULL_STR(),          // imageUrl
            inventory.quantity,
            FALSE(),             // soldOut (서비스에서 재계산)
            category.id,
            NULL_STR()           // categoryPath
        ))
        .from(product)
        .join(product.category, category)
        .leftJoin(inventory).on(inventory.product.id.eq(product.id))
        .where(product.id.eq(id), product.status.eq(ProductStatus.ON_SALE))
        .fetchOne();

    return Optional.ofNullable(dto);
  }

  // 고객 목록
  @Override
  public Page<CustomerProductListView> findCustomerProductList(
      String keyword, boolean noCategoryFilter, Collection<Long> categoryIds, Pageable pageable) {

    var where = whereCustomer(keyword, noCategoryFilter, categoryIds);

    List<CustomerProductListView> content = query
        .select(Projections.constructor(CustomerProductListView.class,
            product.id,
            product.name,
            product.price,
            product.imageKey,
            NULL_STR(),      // imageUrl
            inventory.quantity,
            FALSE(),         // soldOut
            category.id,
            NULL_STR(),      // categoryPath
            product.createdAt
        ))
        .from(product)
        .join(product.category, category)
        .leftJoin(inventory).on(inventory.product.id.eq(product.id))
        .where(where)
        .orderBy(orderBy(pageable))
        .offset(pageable.getOffset())
        .limit(pageable.getPageSize())
        .fetch();

    Long total = query
        .select(product.id.count())
        .from(product)
        .join(product.category, category)
        .where(where)
        .fetchOne();

    return new PageImpl<>(content, pageable, total == null ? 0 : total);
  }
}
