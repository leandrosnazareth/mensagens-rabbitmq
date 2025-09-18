package com.leandronazareth.mensagens_rabbitmq;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class SmsServiceTest {
    @Autowired
    SmsRepository smsRepository;
    @Autowired
    SmsService smsService;

    @Test
    void testProcessarMensagem_Sucesso() {
        Sms resultado = smsService.processarSms("Teste");
        assertNotNull(resultado);
        assertEquals(resultado.getId() + " - Resposta", resultado.getMensagem());
    }

    @Test
    void testProcessarMensagem_FalhaEnvio() {
        Sms resultado = smsService.processarSms("Teste");
        assertNull(resultado);
    }

    @Test
    void testProcessarMensagem_FalhaRecebimento() {
        Sms enviada = new Sms();
        enviada.setId("1234567890");
        enviada.setMensagem("Teste");
        Mockito.when(smsRepository.enviaSms(Mockito.anyString())).thenReturn(enviada);
        Mockito.when(smsRepository.consultarRecebimentoSms(Mockito.anyString())).thenReturn(null);
        Sms resultado = smsService.processarSms("Teste");
        assertNull(resultado);
    }
}
