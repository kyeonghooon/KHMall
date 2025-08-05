package com.khmall.common;

import java.util.NoSuchElementException;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.openapitools.jackson.nullable.JsonNullable;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
class JsonNullableTest {

  @Test
  void 필드값_존재_테스트() {
    JsonNullable<String> jsonNullable = JsonNullable.of("test");
    jsonNullable.ifPresent(value -> {
      assertEquals("test", value);
    });

    assertTrue(jsonNullable.isPresent());
    assertEquals("test", jsonNullable.get());
  }

  @Test
  void 필드값_null_테스트() {
    JsonNullable<String> jsonNullable = JsonNullable.of(null);
    jsonNullable.ifPresent(Assertions::assertNull);

    assertTrue(jsonNullable.isPresent());
    assertNull(jsonNullable.get());
  }

  @Test
  void Optional_필드값_null_비교용_테스트() {
    Optional<String> optional = Optional.ofNullable(null);
    optional.ifPresent(value -> {
      fail("This should not be called, as the value is null.");
    });
    assertFalse(optional.isPresent());
  }

  @Test
  void 필드값_생략_테스트() {
    JsonNullable<String> jsonNullable = JsonNullable.undefined();
    jsonNullable.ifPresent(value -> {
      fail("This should not be called, as the value is undefined.");
    });

    assertFalse(jsonNullable.isPresent());
    assertThrows(NoSuchElementException.class, jsonNullable::get);
  }

}
