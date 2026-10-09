package com.countryinfo.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "country_info", indexes = @Index(name = "idx_country_iso_code", columnList = "iso_code", unique = true))
public class CountryInfo
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "iso_code", nullable = false, unique = true, length = 10)
    private String isoCode;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(length = 150)
    private String capitalCity;

    @Column(length = 30)
    private String phoneCode;

    @Column(length = 10)
    private String continentCode;

    @Column(length = 10)
    private String currencyIsoCode;

    @Column(length = 500)
    private String countryFlag;

    @OneToMany(mappedBy = "country", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Language> languages = new ArrayList<>();

    public CountryInfo() {}

    public CountryInfo(String isoCode, String name, String capitalCity, String phoneCode,
                       String continentCode, String currencyIsoCode, String countryFlag)
    {
        this.isoCode = isoCode;
        this.name = name;
        this.capitalCity = capitalCity;
        this.phoneCode = phoneCode;
        this.continentCode = continentCode;
        this.currencyIsoCode = currencyIsoCode;
        this.countryFlag = countryFlag;
    }

    public void addLanguage(Language language)
    {
        languages.add(language);
        language.setCountry(this);
    }

    public Long getId() { return id; }
    public String getIsoCode() { return isoCode; }
    public void setIsoCode(String isoCode) { this.isoCode = isoCode; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCapitalCity() { return capitalCity; }
    public void setCapitalCity(String capitalCity) { this.capitalCity = capitalCity; }
    public String getPhoneCode() { return phoneCode; }
    public void setPhoneCode(String phoneCode) { this.phoneCode = phoneCode; }
    public String getContinentCode() { return continentCode; }
    public void setContinentCode(String continentCode) { this.continentCode = continentCode; }
    public String getCurrencyIsoCode() { return currencyIsoCode; }
    public void setCurrencyIsoCode(String currencyIsoCode) { this.currencyIsoCode = currencyIsoCode; }
    public String getCountryFlag() { return countryFlag; }
    public void setCountryFlag(String countryFlag) { this.countryFlag = countryFlag; }
    public List<Language> getLanguages() { return languages; }
    public void setLanguages(List<Language> languages) { this.languages = languages; }
}
