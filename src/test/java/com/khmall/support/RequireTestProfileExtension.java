package com.khmall.support;

import java.util.Arrays;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;

public class RequireTestProfileExtension implements BeforeAllCallback {
  @Override
  public void beforeAll(ExtensionContext context) {
    ApplicationContext appContext = SpringExtension.getApplicationContext(context);
    String[] profiles = appContext.getEnvironment().getActiveProfiles();
    boolean isTest = Arrays.asList(profiles).contains("test");
    if (!isTest) {
      throw new IllegalStateException("테스트는 반드시 test 프로필로 실행해야 합니다! (현재: " + Arrays.toString(profiles) + ")");
    }
  }
}
