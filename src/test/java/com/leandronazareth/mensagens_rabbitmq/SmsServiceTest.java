package com.leandronazareth.mensagens_rabbitmq;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;

import com.leandronazareth.mensagens_rabbitmq.model.Sms;
import com.leandronazareth.mensagens_rabbitmq.repository.SmsRepository;
import com.leandronazareth.mensagens_rabbitmq.service.SmsService;

@SpringBootTest
@TestPropertySource(properties = "app.use.rabbit=false")
public class SmsServiceTest {

    @MockBean
    SmsRepository smsRepository;

    @Autowired
    SmsService smsService;

    @Test
    void testProcessarMensagem_Sucesso() {
        Sms enviada = new Sms();
        enviada.setId("bf6e8659-f");
        enviada.setMensagem("Teste");
        Sms resposta = new Sms();
        resposta.setId(enviada.getId());
        resposta.setMensagem(enviada.getId() + " - Resposta");

        Mockito.when(smsRepository.enviaSms(Mockito.anyString())).thenReturn(enviada);
        Mockito.when(smsRepository.consultarRecebimentoSms(enviada.getId())).thenReturn(resposta);

        Sms resultado = smsService.processarSms("Teste");
        assertNotNull(resultado);
        assertEquals(enviada.getId() + " - Resposta", resultado.getMensagem());
    }

    @Test
    void testProcessarMensagem_FalhaEnvio() {
        Mockito.when(smsRepository.enviaSms(Mockito.anyString())).thenReturn(null);
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
