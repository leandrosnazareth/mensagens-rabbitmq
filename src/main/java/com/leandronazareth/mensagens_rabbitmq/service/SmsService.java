package com.leandronazareth.mensagens_rabbitmq.service;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import org.springframework.stereotype.Service;

import com.leandronazareth.mensagens_rabbitmq.model.Sms;
import com.leandronazareth.mensagens_rabbitmq.repository.SmsRepository;

@Service
public class SmsService {

    @Autowired
    SmsRepository smsRepository;

    @Autowired(required = false)
    RabbitTemplate rabbitTemplate;

    @Value("${app.use.rabbit:true}")
    private boolean useRabbit;

    @Value("${timeout.delay.ms:60000}")
    private long timeoutMs;

    // Pending responses keyed by message id
    private static final Map<String, CompletableFuture<Sms>> pendingResponses = new ConcurrentHashMap<>();

    // Expose accessor for controller to complete pending responses
    public static CompletableFuture<Sms> getPendingResponse(String id) {
        return pendingResponses.get(id);
    }

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(SmsService.class);

    public Sms processarSms(String mensagem) {
        // criar objeto SMS com id temporario
        Sms smsRequest = new Sms();
        smsRequest.setId(java.util.UUID.randomUUID().toString().substring(0, 10));
        smsRequest.setMensagem(mensagem);

        Sms smsEnviado = null;

        // Se RabbitTemplate estiver disponível, envie via fila e aguarde resposta via endpoint
        if (rabbitTemplate != null && useRabbit) {
            // Register a future that will be completed by the callback endpoint when
            // the provider calls /callback/sms with the same id.
            CompletableFuture<Sms> future = new CompletableFuture<>();
            pendingResponses.put(smsRequest.getId(), future);
            try {
                logger.info("Enviando mensagem via RabbitMQ, aguardando callback na API");
                rabbitTemplate.convertAndSend(com.leandronazareth.mensagens_rabbitmq.config.RabbitConfig.SMS_QUEUE, smsRequest);

                // aguardar a resposta enviada pelo callback (endpoint)
                Sms resp = null;
                try {
                    resp = future.get(timeoutMs, TimeUnit.MILLISECONDS);
                } catch (java.util.concurrent.TimeoutException te) {
                    // If timeout happens, the caller will get 202 Accepted and the
                    // pending entry will be removed; the provider can still call later
                    // but this process won't be waiting anymore.
                    logger.warn("Timeout aguardando resposta para ID={}", smsRequest.getId());
                }

                if (resp != null) {
                    smsEnviado = resp;
                    logger.info("Resposta recebida via callback: {}", smsEnviado.getMensagem());
                }
            } catch (Exception e) {
                logger.error("Erro ao enviar via RabbitMQ: {}", e.getMessage());
            } finally {
                pendingResponses.remove(smsRequest.getId());
            }
        }

        // fallback para envio direto (simulado) caso rabbit não esteja configurado ou sem resposta
        if (smsEnviado == null) {
            smsEnviado = enviarSms(mensagem);
            if (smsEnviado != null) {
                logger.info("Mensagem enviada com sucesso (fallback): {}", smsEnviado.getMensagem());
                this.saveEnvioMensagem(smsEnviado);
            } else {
                logger.error("N7o foi possível enviar a mensagem (fallback).");
                return null;
            }
            // tentar consultar resposta via repositório
            Sms smsRecebido = consultarRespostaDoSms(smsEnviado.getId());
            if (smsRecebido != null) {
                logger.info("Mensagem recebida: {}", smsRecebido.getMensagem());
                return smsRecebido;
            } else {
                logger.warn("Nenhuma mensagem recebida para o ID: {}", smsEnviado.getId());
                return null;
            }
        }

        return smsEnviado;
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
        // aguardar at 1 minuto para receber a mensagem (consultar a cada 5 segundos)
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
