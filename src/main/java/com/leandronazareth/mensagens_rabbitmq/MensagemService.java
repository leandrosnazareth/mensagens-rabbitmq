package com.leandronazareth.mensagens_rabbitmq;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MensagemService {

    @Autowired
    MensagemRepository mensagemRepository;
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(MensagemService.class);

    public MensagemService(MensagemRepository mensagemRepository) {
        this.mensagemRepository = mensagemRepository;
    }

    public Mensagem processarMensagem(String mensagem) {
        // enviar mensagem com retentativas e aguarda a resposta
        Mensagem mensagemEnviada = enviarMensagem(mensagem);
        if (mensagemEnviada != null) {
            logger.info("Mensagem enviada com sucesso: {}", mensagemEnviada.getMensagem());
            this.saveEnvioMensagem(mensagemEnviada);
        } else {
            logger.error("Não foi possível enviar a mensagem.");
            return null;
        }

        // cosultar resposta da mensagem
        Mensagem mensagemRecebida = consultarRespostaDaMensagem(mensagemEnviada.getId());
        if (mensagemRecebida != null) {
            logger.info("Mensagem recebida: {}", mensagemRecebida.getMensagem());
            return mensagemRecebida;
        } else {
            logger.warn("Nenhuma mensagem recebida para o ID: {}", mensagemEnviada.getId());
            return null;
        }
    }

    // enviarMensagemComRetentativas
    private Mensagem enviarMensagem(String mensagem) {
        try {
            Mensagem mensagemEnviada = mensagemRepository.enviaMensagem(mensagem);
            if (mensagemEnviada != null) {
                logger.info("Mensagem enviada com sucesso!");
                return mensagemEnviada;
            } else {
                logger.warn("Falha ao enviar mensagem.");
            }
        } catch (Exception e) {
            logger.error("Erro ao enviar mensagem: {}", e.getMessage());
        }
        return null;
    }

    private void saveEnvioMensagem(Mensagem mensagem) {
        // Simula o salvamento da mensagem enviada
        logger.info("Salvando mensagem enviada: ID={}, Mensagem={}", mensagem.getId(), mensagem.getMensagem());
    }

    private Mensagem consultarRespostaDaMensagem(String idMensagem) {
        // aguardar até 1 minuto para receber a mensagem (consultar a cada 5 segundos)
        for (int i = 0; i < 12; i++) {
            Mensagem mensagem = mensagemRepository.consultarRecebimentoMensagem(idMensagem);
            if (mensagem != null) {
                return mensagem;
            }
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.error("Thread interrompida durante espera: {}", e.getMessage());
            }
        }
        return null;
    }

}
