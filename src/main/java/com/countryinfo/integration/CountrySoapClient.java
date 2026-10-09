package com.countryinfo.integration;

import com.countryinfo.dto.SoapCountryInfo;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.w3c.dom.Document;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.xml.sax.SAXException;

@Component
public class CountrySoapClient
{
    RestTemplate restTemplate = new RestTemplate();
    ObjectMapper objectMapper = new ObjectMapper();
    Logging logging = new Logging();
    private static String appname = "country-info-microservice";
    private final String endpoint;

    public CountrySoapClient(@Value("${country.soap.endpoint}") String endpoint)
    {
        this.endpoint = endpoint;
    }

    public String countryIsoCode(String countryName) throws IOException, ParserConfigurationException, SAXException, XPathExpressionException
    {
        StringBuilder str = new StringBuilder();

        str.append("<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:web=\"http://www.oorsprong.org/websamples.countryinfo\"><soapenv:Header/><soapenv:Body><web:CountryISOCode>");
        str.append("<web:sCountryName>").append(countryName).append("</web:sCountryName></web:CountryISOCode>");
        str.append("</soapenv:Body></soapenv:Envelope>");

        String xmlResponse = callCountryService(str.toString());

        ///convert xml to string
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        Document document = factory.newDocumentBuilder().parse(new ByteArrayInputStream(xmlResponse.getBytes(StandardCharsets.UTF_8)));

        XPath xpath = XPathFactory.newInstance().newXPath();
        String countryCode = xpath.evaluate("//*[local-name()='CountryISOCodeResult']/text()", document);

        return countryCode;
    }

    public SoapCountryInfo fullCountryInfo(String isoCode) throws IOException, ParserConfigurationException, SAXException, XPathExpressionException
    {
        List<SoapCountryInfo.LanguageResponse> languageResponse = new ArrayList<>();

        StringBuilder str = new StringBuilder();
        str.append("<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:web=\"http://www.oorsprong.org/websamples.countryinfo\"><soapenv:Header/><soapenv:Body><web:FullCountryInfo>");
        str.append("<web:sCountryISOCode>").append(isoCode).append("</web:sCountryISOCode></web:FullCountryInfo>");
        str.append("</soapenv:Body></soapenv:Envelope>");

        String xmlResponse = callCountryService(str.toString());
        String responseJson = xmlToJson(xmlResponse);
        JSONObject responseObj = new JSONObject(responseJson);
        JSONObject responseBody = responseObj.getJSONObject("Body").getJSONObject("FullCountryInfoResponse").getJSONObject("FullCountryInfoResult");

        JSONObject jsonBody = responseBody.getJSONObject("Languages");
        Object jsonBodyArr = jsonBody.opt("tLanguage");
        if (jsonBodyArr instanceof JSONArray)
        {
            JSONArray dataArray = (JSONArray) jsonBodyArr;
            for(int i = 0; i < dataArray.length(); i++)
            {
                JSONObject object = dataArray.getJSONObject(i);
                languageResponse.add(new SoapCountryInfo.LanguageResponse(object.get("sISOCode").toString().replace("\"", "").trim(), object.get("sName").toString().replace("\"", "").trim()));
            }
        }
        else
        {
            JsonNode root = objectMapper.readTree(responseBody.opt("Languages").toString());
            languageResponse.add(new SoapCountryInfo.LanguageResponse(root.get("tLanguage").get("sISOCode").toString().replace("\"", "").trim(), root.get("tLanguage").get("sName").toString().replace("\"", "").trim()));
        }
        return new SoapCountryInfo(responseBody.get("sISOCode").toString(),responseBody.get("sName").toString(),responseBody.get("sCapitalCity").toString(),responseBody.get("sPhoneCode").toString(),responseBody.get("sContinentCode").toString(),responseBody.get("sCurrencyISOCode").toString(),responseBody.get("sCountryFlag").toString(), languageResponse);
    }

    public String callCountryService(String payload)
    {
        long startTime = System.currentTimeMillis();
        try
        {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.TEXT_XML);
            headers.setAccept(List.of(MediaType.TEXT_XML));
            headers.add("SOAPAction",
                    "\"http://www.oorsprong.org/websamples.countryinfo/FullCountryInfo\""
            );

            HttpEntity<String> httpEntity = new HttpEntity<>(payload, headers);
            ResponseEntity<String> responseEntity
                    = restTemplate.exchange(endpoint,
                    HttpMethod.POST,
                    httpEntity,
                    String.class
            );
            return responseEntity.getBody();
        }
        catch (Exception e)
        {
            logging.setProcess("calling country service.")
                    .setLogLevel("error")
                    .setSeverity("error".toUpperCase())
                    .setMicroservice(appname)
                    .setTransaction(String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR))
                    .setTargetSystem(appname)
                    .setSourceSystem(appname)
                    .setResponseCode(String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()))
                    .setResponse(e.toString())
                    .setProcessDuration(String.valueOf(System.currentTimeMillis() - startTime))
                    .setIdentity(String.valueOf(System.currentTimeMillis()))
                    .setRequestPayload("")
                    .setResponsePayload("")
                    .setRequestHeaders("")
                    .write();
            return null;
        }
    }

    public static String xmlToJson(String response) throws IOException
    {
        XmlMapper xmlMapper = new XmlMapper();
        JsonNode jsonNode = xmlMapper.readTree(response.getBytes()); //NOSONAR
        com.fasterxml.jackson.databind.ObjectMapper objMapper = new ObjectMapper();
        return objMapper.writeValueAsString(jsonNode);
    }
}