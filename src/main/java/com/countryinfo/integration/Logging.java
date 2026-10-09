package com.countryinfo.integration;

import com.countryinfo.dto.LogRequest;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@Setter()
@Getter
@Accessors(chain = true)
public class Logging
{
    Object transactionId = "";
    String severity = "";
    String microservice = "";
    String transaction = "";
    String process = "";
    String processDuration = "";
    String identity = "";
    String sourceSystem = "";
    Object targetSystem = "";
    String responseCode = "";
    String response = "";
    Object errorDescription = "";
    Object requestPayload = "";
    Object responsePayload = "";
    Object requestHeaders = "";
    String logLevel = "";

    static final Logger logger = LoggerFactory.getLogger(Logging.class);
    private static final String TITTLES = "Severity={} | MicroService={} | TransactionID={} | Transaction={} | Process={} | ProcessDuration={} | Identity={} | SourceSystem={} | TargetSystem={} | ResponseCode={} | Response={} | ErrorDescription={} | RequestPayload={} | ResponsePayload={} | RequestHeaders={}";
    @Async
    public void write()
    {
        LogRequest logRequest = new LogRequest();

        try {
            switch (logLevel) {
                case "warn": {
                    logger.warn(TITTLES,
                            severity,microservice,transactionId,transaction,process, processDuration, identity, sourceSystem, targetSystem, responseCode, response, errorDescription, requestPayload, responsePayload, requestHeaders
                    );
                    break;
                }
                case "error": {
                    logger.error(TITTLES,
                            severity,microservice,transactionId,transaction,process, processDuration, identity, sourceSystem, targetSystem, responseCode, response, errorDescription, requestPayload, responsePayload, requestHeaders
                    );
                    break;
                }
                default: {
                    logger.info(TITTLES,
                            severity,microservice,transactionId,transaction,process, processDuration, identity, sourceSystem, targetSystem, responseCode, response, errorDescription, requestPayload, responsePayload, requestHeaders
                    );
                    break;
                }
            }

            logRequest.setRequestPayload(response)
                    .setSeverity(logRequest.getSeverity())
                    .setMicroservice(logRequest.getMicroservice())
                    .setTransactionId(logRequest.getTransactionId())
                    .setIdentity(logRequest.getIdentity())
                    .setProcess(logRequest.getProcess())
                    .setResponse(logRequest.getResponse())
                    .setResponseCode(logRequest.getResponseCode())
                    .setProcessDuration(logRequest.getProcessDuration())
                    .setRequestPayload(logRequest.getRequestPayload())
                    .setTransaction(logRequest.getTransaction())
                    .setRequestHeaders(logRequest.getRequestHeaders())
                    .setResponsePayload(logRequest.getResponsePayload());
        }
        catch(Exception e)
        {
            log.error("{}", e.getMessage());
        }
    }
}