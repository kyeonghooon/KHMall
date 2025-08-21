package com.khmall.validation;

import jakarta.validation.valueextraction.ExtractedValue;
import jakarta.validation.valueextraction.UnwrapByDefault;
import jakarta.validation.valueextraction.ValueExtractor;
import org.openapitools.jackson.nullable.JsonNullable;

@UnwrapByDefault
public class JsonNullableValueExtractor
    implements ValueExtractor<JsonNullable<@ExtractedValue ?>> {

  @Override
  public void extractValues(JsonNullable<?> original, ValueReceiver receiver) {
    // 요청 바디에 해당 필드가 "없음"이면 검증 자체를 건너뜀
    if (original == null || !original.isPresent()) {
      return;
    }
    // 명시적으로 null이면 null로 전달 -> @NotNull 등 위반 발생
    receiver.value(null, original.orElse(null));
  }
}
