package com.khmall.domain.cart;

import com.khmall.common.constants.CartConstants;
import com.khmall.common.constants.ProductConstants;
import com.khmall.config.S3Props;
import com.khmall.domain.cart.dto.CartAddRequest;
import com.khmall.domain.cart.dto.CartItemResponse;
import com.khmall.domain.cart.dto.CartRemoveByIdsRequest;
import com.khmall.domain.cart.dto.CartResponse;
import com.khmall.domain.cart.dto.CartSetQuantityRequest;
import com.khmall.domain.product.Product;
import com.khmall.domain.product.ProductRepository;
import com.khmall.domain.product.ProductStatus;
import com.khmall.domain.user.User;
import com.khmall.domain.user.UserRepository;
import com.khmall.exception.custom.BadRequestException;
import com.khmall.exception.custom.ConflictException;
import com.khmall.exception.custom.NotFoundException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class CartService {

  private final CartRepository cartRepository;
  private final CartItemRepository cartItemRepository;
  private final ProductRepository productRepository;
  private final UserRepository userRepository;

  private final S3Props s3Props;

  /**
   * 장바구니에 상품을 추가합니다.
   *
   * @param userId 사용자 ID
   * @param req    장바구니 추가 요청
   */
  @Transactional
  public void add(Long userId, CartAddRequest req) {
    Cart cart = getOrCreateCart(userId);

    CartItem item = cartItemRepository
        .findByCart_IdAndProduct_Id(cart.getId(), req.productId())
        .orElse(null);

    if (item != null) {
      if (item.getProduct().getStatus() != ProductStatus.ON_SALE) {
        cart.removeItem(item);
        cartItemRepository.delete(item);
        throw new BadRequestException(ProductConstants.NOT_SALE);
      }
      item.setQuantity(item.getQuantity() + req.quantity());
      return; // 수량만 업데이트하고 종료
    }
    Product product = productRepository.findById(req.productId())
        .orElseThrow(() -> new NotFoundException(ProductConstants.NOT_FOUND));
    if (product.getStatus() != ProductStatus.ON_SALE) {
      throw new ConflictException(ProductConstants.NOT_SALE);
    }

    CartItem newItem = CartMapper.toItemEntity(product, req.quantity());
    cart.addItem(newItem);
    try {
      cartItemRepository.save(newItem);
    } catch (DataIntegrityViolationException e) {
      // 동시성: 누군가 먼저 넣은 경우 → 재조회 후 합산
      CartItem merged = cartItemRepository
          .findByCart_IdAndProduct_Id(cart.getId(), req.productId())
          .orElseThrow(() -> e);
      merged.setQuantity(merged.getQuantity() + req.quantity());
    }
  }

  /**
   * 장바구니에 있는 상품의 수량을 설정합니다.
   *
   * @param userId  사용자 ID
   * @param request 장바구니 수량 설정 요청
   */
  @Transactional
  public void setQuantity(Long userId, CartSetQuantityRequest request) {
    Cart cart = cartRepository.findByUser_Id(userId)
        .orElseThrow(
            () -> new NotFoundException(CartConstants.NOT_FOUND));
    CartItem item = cartItemRepository.findByCart_IdAndProduct_Id(cart.getId(), request.productId())
        .orElseThrow(() -> new NotFoundException(CartConstants.ITEM_NOT_FOUND));
    if (item.getProduct().getStatus() == ProductStatus.ON_SALE && request.quantity() > 0) {
      item.setQuantity(request.quantity());
    } else {
      // 수량이 0 이하이거나 상품이 판매 중이 아닌 경우
      cart.removeItem(item);
      cartItemRepository.delete(item);
    }
  }

  /**
   * 장바구니에서 상품을 제거합니다.
   *
   * @param userId  사용자 ID
   * @param request 장바구니 항목 제거 요청
   */
  @Transactional
  public void remove(Long userId, CartRemoveByIdsRequest request) {
    Cart cart = cartRepository.findByUser_Id(userId)
        .orElseThrow(() -> new NotFoundException(CartConstants.ITEM_NOT_FOUND));
    long deleted = cartItemRepository.deleteByCart_IdAndIdIn(cart.getId(), request.cartItemIds());
    if (deleted > 0) {
      // 삭제된 항목이 있다면 장바구니 업데이트
      cart.markAsUpdated();
    }
  }

  /**
   * 장바구니를 비웁니다.
   *
   * @param userId 사용자 ID
   */
  @Transactional
  public void clear(Long userId) {
    Cart cart = cartRepository.findByUser_Id(userId)
        .orElseThrow(() -> new NotFoundException(CartConstants.ITEM_NOT_FOUND));
    cart.getItems().clear();
    cart.markAsUpdated();
  }

  /**
   * 사용자의 장바구니를 조회합니다.
   *
   * @param userId 사용자 ID
   * @return 장바구니 응답
   */
  @Transactional
  public CartResponse get(Long userId) {
    Cart cart = cartRepository.findWithItemsByUser_Id(userId)
        .orElseGet(() -> getOrCreateCart(userId));

    // 판매중이지 않은 상품 delete
    List<Long> toRemoveIds = cart.getItems().stream()
        .filter(ci -> ci.getProduct().getStatus() != ProductStatus.ON_SALE)
        .map(CartItem::getId)
        .toList();
    if (!toRemoveIds.isEmpty()) {
      cart.getItems().removeIf(ci -> toRemoveIds.contains(ci.getId()));
      cartItemRepository.deleteByCart_IdAndIdIn(cart.getId(), toRemoveIds);
      cart.markAsUpdated();
    }


    List<CartItemResponse> lines = cart.getItems().stream()
        .map(ci -> CartMapper.toItemResponse(ci, s3Props.getBaseUrl() + "/" + ci.getProduct().getImageKey()))
        .toList();

    return CartMapper.toResponse(cart, lines);
  }

  private Cart getOrCreateCart(Long userId) {
    return cartRepository.findByUser_Id(userId).orElseGet(() -> {
      User userRef = userRepository.getReferenceById(userId);
      try {
        return cartRepository.save(CartMapper.toEntity(userRef));
      } catch (DataIntegrityViolationException e) {
        // 동시 생성 경합 처리(유니크/PK 충돌 시 재조회)
        return cartRepository.findByUser_Id(userId).orElseThrow(() -> e);
      }
    });
  }


}
