package com.khmall.domain.product;

import com.khmall.domain.product.dto.AdminProductDetailResponse;
import com.khmall.domain.product.dto.AdminProductListView;
import com.khmall.domain.product.dto.CustomerProductDetailResponse;
import com.khmall.domain.product.dto.CustomerProductListView;
import java.util.Collection;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductRepository extends JpaRepository<Product, Long> {

  // 특정 카테고리의 상품 존재 확인
  boolean existsByCategory_Id(Long categoryId);

  /**
   * 관리자 상품 상세 조회
   *
   * @param id 상품 ID
   * @return 상품 상세 정보 (관리자용) imageUrl과 categoryPath는 서비스 계층에서 주입한다.
   */
  @Query("""
      select new com.khmall.domain.product.dto.AdminProductDetailResponse(
        p.id,
        p.name,
        p.description,
        p.imageKey,
        null,
        p.price,
        p.status,
        c.id,
        c.name,
        null,
        (select i.quantity from Inventory i where i.product.id = p.id),
        p.createdAt,
        p.updatedAt,
        p.createdBy,
        p.updatedBy
      )
      from Product p
      left join p.category c
      where p.id = :id
      """)
  Optional<AdminProductDetailResponse> findAdminProductDetailById(@Param("id") Long id);

  /**
   * 관리자 상품 목록 조회
   *
   * @param keyword          검색어 (상품명)
   * @param status           상품 상태 (null이면 전체)
   * @param noCategoryFilter 카테고리 필터링 여부 (true면 모든 카테고리)
   * @param categoryIds      카테고리 ID 목록 (noCategoryFilter가 false일 때만 사용)
   * @param pageable         페이징 정보
   * @return 상품 목록 페이지 CategoryPath는 서비스 계층에서 주입한다.
   */
  @Query("""
      select new com.khmall.domain.product.dto.AdminProductListView(
        p.id,
        p.name,
        p.price,
        p.status,
        c.id,
        c.name,
        null,
        (select i.quantity from Inventory i where i.product.id = p.id),
        p.createdAt
      )
      from Product p
      left join p.category c
      where (:keyword is null or :keyword = '' or p.name like concat('%', :keyword, '%'))
        and (:status is null or p.status = :status)
        and (:noCategoryFilter = true or c.id in :categoryIds)
      """)
  Page<AdminProductListView> findAdminProductList(
      @Param("keyword") String keyword,
      @Param("status") ProductStatus status,
      @Param("noCategoryFilter") boolean noCategoryFilter,
      @Param("categoryIds") Collection<Long> categoryIds,
      Pageable pageable
  );

  /**
   * 고객용 상품 상세 조회
   *
   * @param id 상품 ID
   * @return 상품 상세 정보 (고객용) imageUrl, categoryPath, soldOut은 서비스 계층에서 주입한다.
   */
  @Query("""
      select new com.khmall.domain.product.dto.CustomerProductDetailResponse(
        p.id,
        p.name,
        p.description,
        p.price,
        p.imageKey,
        null,
        (select i.quantity from Inventory i where i.product.id = p.id),
        false,
        c.id,
        null
      )
      from Product p
      join p.category c
      where p.id = :id
        and p.status = com.khmall.domain.product.ProductStatus.ON_SALE
      """)
  Optional<CustomerProductDetailResponse> findCustomerProductDetailById(@Param("id") Long id);

  /**
   * 고객용 상품 목록 조회
   *
   * @param keyword          검색어 (상품명)
   * @param noCategoryFilter 카테고리 필터링 여부 (true면 모든 카테고리)
   * @param categoryIds      카테고리 ID 목록 (noCategoryFilter가 false일 때만 사용)
   * @param pageable         페이징 정보
   * @return 상품 목록 페이지 imageUrl과 categoryPath는 서비스 계층에서 주입한다.
   */
  @Query("""
      select new com.khmall.domain.product.dto.CustomerProductListView(
        p.id,
        p.name,
        p.price,
        p.imageKey,
        null,
        (select i.quantity from Inventory i where i.product.id = p.id),
        false,
        c.id,
        null,
        p.createdAt
      )
      from Product p
      join p.category c
      where p.status = com.khmall.domain.product.ProductStatus.ON_SALE
        and (:keyword is null or :keyword = '' or p.name like concat('%', :keyword, '%'))
        and (:noCategoryFilter = true or c.id in :categoryIds)
      """)
  Page<CustomerProductListView> findCustomerProductList(
      @Param("keyword") String keyword,
      @Param("noCategoryFilter") boolean noCategoryFilter,
      @Param("categoryIds") Collection<Long> categoryIds,
      Pageable pageable
  );
}