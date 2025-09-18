package com.leandronazareth.mensagens_rabbitmq;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MensagemController {
    private final MensagemService mensagemService;

    public MensagemController(MensagemService mensagemService) {
        this.mensagemService = mensagemService;
    }

    @PostMapping("/mensagem")
    public Mensagem enviarMensagem(@RequestParam String  mensagem) {
        return mensagemService.processarMensagem(mensagem);
    }

}
