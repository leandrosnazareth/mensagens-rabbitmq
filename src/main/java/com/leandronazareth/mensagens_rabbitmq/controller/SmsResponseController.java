package com.leandronazareth.mensagens_rabbitmq.controller;

import com.leandronazareth.mensagens_rabbitmq.model.Sms;
import com.leandronazareth.mensagens_rabbitmq.service.SmsService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/callback")
public class SmsResponseController {

    private static final Logger logger = LoggerFactory.getLogger(SmsResponseController.class);

    @Value("${callback.token:}")
    private String callbackToken;

    @PostMapping("/sms")
    public ResponseEntity<String> receiveCallback(@RequestBody Sms sms,
                                                  @RequestHeader(value = "X-Callback-Token", required = false) String token) {
        logger.info("Callback recebido para ID={}", sms.getId());

        // If a token is configured, the callback must present the same token
        if (callbackToken != null && !callbackToken.isEmpty()) {
            if (token == null || !callbackToken.equals(token)) {
                logger.warn("Token de callback inválido para ID={}", sms.getId());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
        }

        // Lookup pending CompletableFuture and complete it so the thread waiting
        // in SmsService.processarSms() will receive the response without polling.
        CompletableFuture<Sms> future = SmsService.getPendingResponse(sms.getId());
        if (future != null) {
            future.complete(sms);
            return ResponseEntity.ok("OK");
        } else {
            // No pending request: the provider must handle retries or log for later
            logger.warn("Nenhuma requisição pendente encontrada para ID={}", sms.getId());
            return ResponseEntity.notFound().build();
        }
    }

}
