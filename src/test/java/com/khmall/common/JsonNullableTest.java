package com.khmall.common;

import java.util.NoSuchElementException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.openapitools.jackson.nullable.JsonNullable;

import static org.junit.jupiter.api.Assertions.*;

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
  void 필드값_생략_테스트() {
    JsonNullable<String> jsonNullable = JsonNullable.undefined();
    jsonNullable.ifPresent(value -> {
      fail("This should not be called, as the value is undefined.");
    });

    assertFalse(jsonNullable.isPresent());
    assertThrows(NoSuchElementException.class, jsonNullable::get);
  }
}
