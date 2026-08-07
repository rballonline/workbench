package com.tiltedev.springreactive.dto.projection;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DestinationWithCountry {

  private Long id;
  private String cityName;
  private Double latitude;
  private Double longitude;
  private String addedBy;
  private LocalDateTime createdAt;
  private String countryCode;
  private String countryName;
  private String capital;
  private String region;
  private Long population;
  private String flagUrl;
}
