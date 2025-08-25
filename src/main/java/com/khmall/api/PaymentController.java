package com.khmall.api;

import com.khmall.domain.payment.PaymentService;
import com.khmall.domain.payment.dto.PaymentApproveRequest;
import com.khmall.domain.payment.dto.PaymentApproveResponse;
import com.khmall.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/payments")
public class PaymentController {

  private final PaymentService paymentService;

  @PostMapping("/{paymentId}/approve")
  @PreAuthorize("hasAnyRole('USER','ADMIN')")
  public ResponseEntity<PaymentApproveResponse> approve(
      @PathVariable Long paymentId,
      @Valid @RequestBody PaymentApproveRequest request,
      @AuthenticationPrincipal CustomUserDetails principal) {
    boolean isAdmin = principal.getAuthorities().stream()
        .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    return ResponseEntity.ok(paymentService.approve(paymentId, request, principal.getUserId(), isAdmin));
  }

}
