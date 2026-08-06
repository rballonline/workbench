package com.tiltedev.spring_reactive.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("countries")
public class Country {

    @Id
    private String code;
    private String name;
    private String capital;
    private String region;
    private Long population;
    private String flagUrl;
}
