package com.tiltedev.springreactive.dto.response;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DestinationResponse {

  private Long id;
  private String cityName;
  private Double latitude;
  private Double longitude;
  private String addedBy;
  private LocalDateTime createdAt;
  private CountryResponse country;
}
