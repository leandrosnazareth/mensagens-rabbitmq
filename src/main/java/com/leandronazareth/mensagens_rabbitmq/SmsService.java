package com.leandronazareth.mensagens_rabbitmq;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SmsService {

    @Autowired
    SmsRepository smsRepository;
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(SmsService.class);

    public Sms processarSms(String mensagem) {
        // enviar mensagem com retentativas e aguarda a resposta
        Sms smsEnviado = enviarSms(mensagem);
        if (smsEnviado != null) {
            logger.info("Mensagem enviada com sucesso: {}", smsEnviado.getMensagem());
            this.saveEnvioMensagem(smsEnviado);
        } else {
            logger.error("Não foi possível enviar a mensagem.");
            return null;
        }

        // cosultar resposta do sms
        Sms smsRecebido = consultarRespostaDoSms(smsEnviado.getId());
        if (smsRecebido != null) {
            logger.info("Mensagem recebida: {}", smsRecebido.getMensagem());
            return smsRecebido;
        } else {
            logger.warn("Nenhuma mensagem recebida para o ID: {}", smsEnviado.getId());
            return null;
        }
    }

    // enviarMensagemComRetentativas
    private Sms enviarSms(String mensagem) {
        try {
            Sms smsEnviado = smsRepository.enviaSms(mensagem);
            if (smsEnviado != null) {
                logger.info("Mensagem enviada com sucesso!");
                return smsEnviado;
            } else {
                logger.warn("Falha ao enviar mensagem.");
            }
        } catch (Exception e) {
            logger.error("Erro ao enviar mensagem: {}", e.getMessage());
        }
        return null;
    }

    private void saveEnvioMensagem(Sms sms) {
        // Simula o salvamento da mensagem enviada
        logger.info("Salvando mensagem enviada: ID={}, Mensagem={}", sms.getId(), sms.getMensagem());
    }

    private Sms consultarRespostaDoSms(String idMensagem) {
        // aguardar até 1 minuto para receber a mensagem (consultar a cada 5 segundos)
        for (int i = 0; i < 12; i++) {
            Sms sms = smsRepository.consultarRecebimentoSms(idMensagem);
            if (sms != null) {
                return sms;
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
