package com.leandronazareth.mensagens_rabbitmq;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class MensagemServiceTest {
    @Autowired
    MensagemRepository mensagemRepository;
    @Autowired
    MensagemService mensagemService;

    @Test
    void testProcessarMensagem_Sucesso() {
        Mensagem resultado = mensagemService.processarMensagem("Teste");
        assertNotNull(resultado);
        assertEquals(resultado.getId() + " - Resposta", resultado.getMensagem());
    }

    @Test
    void testProcessarMensagem_FalhaEnvio() {
        Mensagem resultado = mensagemService.processarMensagem("Teste");
        assertNull(resultado);
    }

    @Test
    void testProcessarMensagem_FalhaRecebimento() {
        Mensagem enviada = new Mensagem();
        enviada.setId("1234567890");
        enviada.setMensagem("Teste");
        Mockito.when(mensagemRepository.enviaMensagem(Mockito.anyString())).thenReturn(enviada);
        Mockito.when(mensagemRepository.consultarRecebimentoMensagem(Mockito.anyString())).thenReturn(null);
        Mensagem resultado = mensagemService.processarMensagem("Teste");
        assertNull(resultado);
    }
}
