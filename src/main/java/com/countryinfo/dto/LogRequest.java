package com.countryinfo.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
public class LogRequest
{
    String severity;
    String microservice;
    String process;
    String transactionId;
    String transaction;
    String targetSystem;
    String sourceSystem;
    String responseCode;
    String response;
    String processDuration;
    String identity;
    String requestPayload;
    String responsePayload;
    String requestHeaders;
}