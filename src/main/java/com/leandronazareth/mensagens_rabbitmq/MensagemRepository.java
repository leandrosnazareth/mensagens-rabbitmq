package com.leandronazareth.mensagens_rabbitmq;

import org.springframework.stereotype.Repository;

@Repository
public class MensagemRepository {

    private static final java.util.Random RANDOM = new java.util.Random();
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(MensagemRepository.class);

    public Mensagem enviaMensagem(String mensagem) {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("Thread interrompida durante espera de recebimento de mensagem.");
            return null;
        }
        // aqui em produção faço uma chamada em um sistema externo com protocolo soap
        Mensagem msg = new Mensagem();
        // random gerar STRING aleatoriA COM 10 CARACTERES
        String randomString = java.util.UUID.randomUUID().toString().substring(0, 10);
        msg.setId(randomString);
        msg.setMensagem(mensagem);
        return msg;
    }

    public Mensagem consultarRecebimentoMensagem(String idMensagem) {
        // aqui em produção faço uma chamada em um sistema externo com protocolo soap
        // sperar 2 segundos
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("Thread interrompida durante espera de recebimento de mensagem.");
            return null;
        }
        Mensagem msg = new Mensagem();
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
