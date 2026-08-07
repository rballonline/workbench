package com.tiltedev.springreactive.dto.result;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GeocodingResult {

  private List<GeocodingEntry> results;

  @Data
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class GeocodingEntry {
    private String name;
    private double latitude;
    private double longitude;
    private String country;
    private String country_code;
  }
}
