package com.countryinfo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import java.util.List;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class CountryResponse
{
    @JsonProperty("id")
    private Long id;
    @JsonProperty("isoCode")
    private String isoCode;
    @JsonProperty("name")
    private String name;
    @JsonProperty("capitalCity")
    private String capitalCity;
    @JsonProperty("phoneCode")
    private String phoneCode;
    @JsonProperty("continentCode")
    private String continentCode;
    @JsonProperty("currencyIsoCode")
    private String currencyIsoCode;
    @JsonProperty("countryFlag")
    private String countryFlag;
    @JsonProperty("languages")
    private List<LanguageResponse> languages;

    @Getter
    @Setter
    @ToString
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    @Data
    public static class LanguageResponse
    {
        @JsonProperty("isoCode")
        public String isoCode;
        @JsonProperty("name")
        public String name;
    }
}