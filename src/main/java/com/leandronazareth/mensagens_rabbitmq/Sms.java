package com.leandronazareth.mensagens_rabbitmq;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Sms {

    private String id;
    private String mensagem;

}
