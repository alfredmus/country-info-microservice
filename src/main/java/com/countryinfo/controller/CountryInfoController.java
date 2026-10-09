package com.countryinfo.controller;

import com.countryinfo.dto.CountryName;
import com.countryinfo.dto.CountryResponse;
import com.countryinfo.service.CountryInfoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.xml.sax.SAXException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.net.URI;
import java.util.List;

@RestController
@Validated
@RequestMapping(path = "/api/v1/countries")
public class CountryInfoController
{
    private final CountryInfoService countryInfoService;
    public CountryInfoController(CountryInfoService countryInfoService)
    {
        this.countryInfoService = countryInfoService;
    }

    @PostMapping("/import")
    public ResponseEntity<CountryResponse> importCountry(@RequestBody @Valid CountryName countryName) throws IOException, XPathExpressionException, ParserConfigurationException, SAXException
    {
        CountryResponse countryResponse = countryInfoService.importCountry(countryName.getCountryname());
        return ResponseEntity.created(URI.create("/api/v1/countries/" + countryResponse.getId())).body(countryResponse);
    }

    @GetMapping
    public List<CountryResponse> findAllCountries()
    {
        return countryInfoService.findAllCountries();
    }

    @GetMapping("/{id}")
    public CountryResponse findCountryById(@PathVariable Long id)
    {
        return countryInfoService.findCountryById(id);
    }

    @PutMapping("/{id}")
    public CountryResponse updateCountry(@PathVariable Long id, @Valid @RequestBody CountryResponse countryResponse)
    {
        return countryInfoService.updateCountry(id, countryResponse);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCountry(@PathVariable Long id)
    {
        countryInfoService.deleteCountry(id);
        return ResponseEntity.noContent().build();
    }
}