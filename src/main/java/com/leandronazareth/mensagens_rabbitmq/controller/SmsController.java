package com.leandronazareth.mensagens_rabbitmq.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.leandronazareth.mensagens_rabbitmq.model.Sms;
import com.leandronazareth.mensagens_rabbitmq.service.SmsService;

@RestController
public class SmsController {
    private final SmsService smsService;

    public SmsController(SmsService mensagemService) {
        this.smsService = mensagemService;
    }

    @PostMapping("/mensagem")
    /**
     * Endpoint that accepts a message text, sends it to the SMS pipeline and
     * waits for a response (callback) up to the configured timeout.
     *
     * Behavior:
     * - 200 OK + body: if a response was received within timeout
     * - 202 Accepted: if the message was accepted but no response arrived in time
     */
    public ResponseEntity<?> enviarSms(@RequestParam String mensagem) {
        Sms resultado = smsService.processarSms(mensagem);
        if (resultado != null) {
            return ResponseEntity.ok(resultado);
        }
        // accepted - mensagem enviada/aceita mas sem resposta no tempo
        return ResponseEntity.accepted().build();
    }

}
