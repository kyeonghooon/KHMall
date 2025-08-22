package com.khmall.domain.order;

import com.khmall.domain.inventory.InventoryLogRepository;
import com.khmall.domain.inventory.InventoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class OrderReversalService {

  private final InventoryRepository inventoryRepository;
  private final InventoryLogRepository inventoryLogRepository;

  public enum ReversalType { CANCEL, REFUND }
}
