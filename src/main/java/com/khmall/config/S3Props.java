package com.khmall.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "storage.s3")
@Getter
@Setter
public class S3Props {
  private String bucket;
  private String region;
  private String baseUrl;
  private String prefix;
}
