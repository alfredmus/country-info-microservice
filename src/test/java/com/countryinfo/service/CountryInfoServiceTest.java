package com.countryinfo.service;

import com.countryinfo.dto.SoapCountryInfo;
import com.countryinfo.integration.CountrySoapClient;
import com.countryinfo.model.CountryInfo;
import com.countryinfo.repository.CountryInfoRepository;
import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class CountryInfoServiceTest
{
    @Test
    void importsCountryUsingTwoStepSoapFlowAndSentenceCase() throws XPathExpressionException, IOException, ParserConfigurationException, SAXException {
        var repository = mock(CountryInfoRepository.class);
        var soap = mock(CountrySoapClient.class);
        var service = new CountryInfoService(repository, soap);
        List<SoapCountryInfo.LanguageResponse> languageResponse = new ArrayList<>();
        languageResponse.add(new SoapCountryInfo.LanguageResponse("sw", "Swahili"));
        when(soap.countryIsoCode("Kenya")).thenReturn("KE");
        when(soap.fullCountryInfo("KE")).thenReturn(new SoapCountryInfo(
                "KE", "Kenya", "Nairobi", "254", "AF", "KES",
                "http://example/kenya.jpg", languageResponse));
        when(repository.findByIsoCodeIgnoreCase("KE")).thenReturn(Optional.empty());
        when(repository.save(any(CountryInfo.class))).thenAnswer(inv -> inv.getArgument(0));

        var result = service.importCountry("kENYA");

        assertThat(result.getIsoCode()).isEqualTo("KE");
        assertThat(result.getName()).isEqualTo("Kenya");
        assertThat(result.getLanguages()).hasSize(1);
        verify(soap).countryIsoCode("Kenya");
        verify(soap).fullCountryInfo("KE");
    }
}