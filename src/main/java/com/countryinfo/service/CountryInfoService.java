package com.countryinfo.service;

import com.countryinfo.dto.CountryResponse;
import com.countryinfo.dto.SoapCountryInfo;
import com.countryinfo.exception.CountryNotFoundException;
import com.countryinfo.integration.CountrySoapClient;
import com.countryinfo.integration.Logging;
import com.countryinfo.model.CountryInfo;
import com.countryinfo.model.Language;
import com.countryinfo.repository.CountryInfoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.xml.sax.SAXException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.util.List;

@Service
public class CountryInfoService
{
    private final CountryInfoRepository countryInfoRepository;
    private final CountrySoapClient countrySoapClient;
    Logging logging = new Logging();
    private static String appname = "country-info-microservice";

    public CountryInfoService(CountryInfoRepository countryInfoRepository, CountrySoapClient countrySoapClient)
    {
        this.countryInfoRepository = countryInfoRepository;
        this.countrySoapClient = countrySoapClient;
    }

    @Transactional
    public CountryResponse importCountry(String countryName) throws IOException, XPathExpressionException, ParserConfigurationException, SAXException
    {
        CountryResponse countryResponse = new CountryResponse();
        long startTime = System.currentTimeMillis();
        try
        {
            String name = sentenceCase(countryName);
            String iso = countrySoapClient.countryIsoCode(name);
            if (iso == null || iso.isBlank())
            {
                logging.setProcess("calling country service.")
                        .setLogLevel("error")
                        .setSeverity("error".toUpperCase())
                        .setMicroservice(appname)
                        .setTransaction(String.valueOf(HttpStatus.NOT_FOUND.value()))
                        .setTargetSystem(appname)
                        .setSourceSystem(appname)
                        .setResponseCode(String.valueOf(HttpStatus.NOT_FOUND.value()))
                        .setResponse("Country not found: " + name)
                        .setProcessDuration(String.valueOf(System.currentTimeMillis() - startTime))
                        .setIdentity(String.valueOf(System.currentTimeMillis()))
                        .setRequestPayload("")
                        .setResponsePayload("")
                        .setRequestHeaders("")
                        .write();
            }

            SoapCountryInfo soapCountryInfo = countrySoapClient.fullCountryInfo(iso.trim());
            CountryInfo country = countryInfoRepository.findByIsoCodeIgnoreCase(soapCountryInfo.getIsoCode())
                    .orElseGet(CountryInfo::new);

            country.setIsoCode(soapCountryInfo.getIsoCode());
            country.setName(soapCountryInfo.getName());
            country.setCapitalCity(soapCountryInfo.getCapitalCity());
            country.setPhoneCode(soapCountryInfo.getPhoneCode());
            country.setContinentCode(soapCountryInfo.getContinentCode());
            country.setCurrencyIsoCode(soapCountryInfo.getCurrencyIsoCode());
            country.setCountryFlag(soapCountryInfo.getCountryFlag());
            country.getLanguages().clear();
            soapCountryInfo.getLanguages().forEach(l -> country.addLanguage(new Language(l.getIsoCode(), l.getName())));

            countryResponse = customResponse(countryInfoRepository.save(country));

            logging.setProcess("success saving country record to database.")
                    .setLogLevel("success")
                    .setSeverity("success".toUpperCase())
                    .setMicroservice(appname)
                    .setTransaction(String.valueOf(HttpStatus.CREATED.value()))
                    .setTargetSystem(appname)
                    .setSourceSystem(appname)
                    .setResponseCode(String.valueOf(HttpStatus.CREATED.value()))
                    .setResponse("")
                    .setProcessDuration(String.valueOf(System.currentTimeMillis() - startTime))
                    .setIdentity(String.valueOf(System.currentTimeMillis()))
                    .setRequestPayload("")
                    .setResponsePayload("")
                    .setRequestHeaders("")
                    .write();
        }
        catch (Exception e)
        {
            logging.setProcess("error saving country record to database.")
                    .setLogLevel("error")
                    .setSeverity("error".toUpperCase())
                    .setMicroservice(appname)
                    .setTransaction(String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR))
                    .setTargetSystem(appname)
                    .setSourceSystem(appname)
                    .setResponseCode(String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()))
                    .setResponse("")
                    .setProcessDuration(String.valueOf(System.currentTimeMillis() - startTime))
                    .setIdentity(String.valueOf(System.currentTimeMillis()))
                    .setRequestPayload("")
                    .setResponsePayload("")
                    .setRequestHeaders("")
                    .write();
        }
        return  countryResponse;
    }

    @Transactional(readOnly = true)
    public List<CountryResponse> findAllCountries()
    {
        return countryInfoRepository.findAll().stream().map(this::customResponse).toList();
    }

    @Transactional(readOnly = true)
    public CountryResponse findCountryById(Long id)
    {
        return customResponse(countryInfoRepository.findById(id).orElseThrow(() -> new CountryNotFoundException("Country not found with id=" + id)));
    }

    @Transactional
    public CountryResponse updateCountry(Long id, CountryResponse countryResponse)
    {
        CountryResponse countryresponse = new CountryResponse();
        long startTime = System.currentTimeMillis();
        try
        {
            CountryInfo country = countryInfoRepository.findById(id)
                    .orElseThrow(() -> new CountryNotFoundException("Country not found with id=" + id));
            country.setIsoCode(countryResponse.getIsoCode());
            country.setName(countryResponse.getName());
            country.setCapitalCity(countryResponse.getCapitalCity());
            country.setPhoneCode(countryResponse.getPhoneCode());
            country.setContinentCode(countryResponse.getContinentCode());
            country.setCurrencyIsoCode(countryResponse.getCurrencyIsoCode());
            country.setCountryFlag(countryResponse.getCountryFlag());
            country.getLanguages().clear();
            if (countryResponse.getLanguages() != null)
            {
                countryResponse.getLanguages().forEach(l -> country.addLanguage(new Language(l.getIsoCode(), l.getName())));
            }

            countryresponse = customResponse(countryInfoRepository.save(country));

            logging.setProcess("success updating country record to database.")
                    .setLogLevel("success")
                    .setSeverity("success".toUpperCase())
                    .setMicroservice(appname)
                    .setTransaction(String.valueOf(HttpStatus.CREATED.value()))
                    .setTargetSystem(appname)
                    .setSourceSystem(appname)
                    .setResponseCode(String.valueOf(HttpStatus.CREATED.value()))
                    .setResponse("")
                    .setProcessDuration(String.valueOf(System.currentTimeMillis() - startTime))
                    .setIdentity(String.valueOf(System.currentTimeMillis()))
                    .setRequestPayload("")
                    .setResponsePayload("")
                    .setRequestHeaders("")
                    .write();
        }
        catch (CountryNotFoundException e)
        {
            logging.setProcess("error updating country record to database.")
                    .setLogLevel("error")
                    .setSeverity("error".toUpperCase())
                    .setMicroservice(appname)
                    .setTransaction(String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR))
                    .setTargetSystem(appname)
                    .setSourceSystem(appname)
                    .setResponseCode(String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()))
                    .setResponse("")
                    .setProcessDuration(String.valueOf(System.currentTimeMillis() - startTime))
                    .setIdentity(String.valueOf(System.currentTimeMillis()))
                    .setRequestPayload("")
                    .setResponsePayload("")
                    .setRequestHeaders("")
                    .write();
        }
        return countryresponse;
    }

    @Transactional
    public void deleteCountry(Long id)
    {
        long startTime = System.currentTimeMillis();
        if (!countryInfoRepository.existsById(id))
        {
            logging.setProcess("country not found with id=" + id)
                    .setLogLevel("error")
                    .setSeverity("error".toUpperCase())
                    .setMicroservice(appname)
                    .setTransaction(String.valueOf(HttpStatus.NOT_FOUND.value()))
                    .setTargetSystem(appname)
                    .setSourceSystem(appname)
                    .setResponseCode(String.valueOf(HttpStatus.NOT_FOUND.value()))
                    .setResponse("Country not found: " + id)
                    .setProcessDuration(String.valueOf(System.currentTimeMillis() - startTime))
                    .setIdentity(String.valueOf(System.currentTimeMillis()))
                    .setRequestPayload("")
                    .setResponsePayload("")
                    .setRequestHeaders("")
                    .write();
        }
        else
        {
            countryInfoRepository.deleteById(id);

            logging.setProcess("success deleting country record to database.")
                    .setLogLevel("success")
                    .setSeverity("success".toUpperCase())
                    .setMicroservice(appname)
                    .setTransaction(String.valueOf(HttpStatus.ACCEPTED.value()))
                    .setTargetSystem(appname)
                    .setSourceSystem(appname)
                    .setResponseCode(String.valueOf(HttpStatus.ACCEPTED.value()))
                    .setResponse("")
                    .setProcessDuration(String.valueOf(System.currentTimeMillis() - startTime))
                    .setIdentity(String.valueOf(System.currentTimeMillis()))
                    .setRequestPayload("")
                    .setResponsePayload("")
                    .setRequestHeaders("")
                    .write();
        }
    }

    private CountryResponse customResponse(CountryInfo c)
    {
        return new CountryResponse(c.getId(), c.getIsoCode(), c.getName(), c.getCapitalCity(),
                c.getPhoneCode(), c.getContinentCode(), c.getCurrencyIsoCode(), c.getCountryFlag(),
                c.getLanguages().stream().map(l -> new CountryResponse.LanguageResponse(l.getIsoCode(), l.getName())).toList());
    }

    private static String sentenceCase(String input)
    {
        String value = input.trim().toLowerCase();
        return value.isEmpty() ? value : Character.toUpperCase(value.charAt(0)) + value.substring(1);
    }
}