package com.leandronazareth.mensagens_rabbitmq;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SmsController {
    private final SmsService smsService;

    public SmsController(SmsService mensagemService) {
        this.smsService = mensagemService;
    }

    @PostMapping("/mensagem")
    public Sms enviarSms(@RequestParam String  mensagem) {
        return smsService.processarSms(mensagem);
    }

}
