package com.leandronazareth.mensagens_rabbitmq.repository;

import org.springframework.stereotype.Repository;

import com.leandronazareth.mensagens_rabbitmq.model.Sms;

@Repository
public class SmsRepository {

    private static final java.util.Random RANDOM = new java.util.Random();
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(SmsRepository.class);

    public Sms enviaSms(String mensagem) {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("Thread interrompida durante espera de recebimento de mensagem.");
            return null;
        }
    // In production this method should call the external SMS gateway (e.g. SOAP/HTTP)
    // and return the provider-assigned id. Here we simulate it by generating a random id
    // and returning the same message text back as confirmation.
    Sms msg = new Sms();
    // random gerar STRING aleatoriA COM 10 CARACTERES
    String randomString = java.util.UUID.randomUUID().toString().substring(0, 10);
    msg.setId(randomString);
    msg.setMensagem(mensagem);
    return msg;
    }

    public Sms consultarRecebimentoSms(String idMensagem) {
        // aqui em produção faço uma chamada em um sistema externo com protocolo soap
        // sperar 2 segundos
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("Thread interrompida durante espera de recebimento de mensagem.");
            return null;
        }
        // Simulate occasional responses. In a real implementation you would query
        // the gateway for any delivered/received replies for the given id.
        Sms msg = new Sms();
        msg.setId(idMensagem);
        if (RANDOM.nextBoolean()) {
            msg.setMensagem(idMensagem + " - Resposta");
            logger.info("Mensagem recebida para consulta: {}", idMensagem);
            return msg;
        }
        logger.warn("Mensagem não respondida para o ID: {}", idMensagem);
        return null;
    }

}
