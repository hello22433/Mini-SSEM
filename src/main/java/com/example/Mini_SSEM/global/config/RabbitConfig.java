package com.example.Mini_SSEM.global.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class RabbitConfig {

    public static final String MAIN_QUEUE = "tax-queue";
    public static final String DLQ_QUEUE = "tax-dlq";
    public static final String DLQ_EXCHANGE = "dlx-exchange";
    public static final String DLQ_ROUTING_KEY= "tax.dead";
    public static final String EXCHANGE = "tax-exchange";
    public static final String ROUTING_KEY = "tax.calculate";

    // 1. 죽은 편지함 (DLQ) : 실패한 메시지가 모이는 곳
    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder.durable(DLQ_QUEUE).build();
    }

    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange(DLQ_EXCHANGE);
    }

    @Bean
    public Binding deadLetterBinding() {
        return BindingBuilder.bind(deadLetterQueue()).to(deadLetterExchange()).with(DLQ_ROUTING_KEY);
    }

    // 2. 메인 큐 (일하는 곳) : 실패 시 DLQ로 보내도록 설정
    @Bean
    public Queue mainQueue() {
        return QueueBuilder.durable(MAIN_QUEUE)
                .withArgument("x-dead-letter-exchange", DLQ_EXCHANGE) // 실패하면 여기로!
                .withArgument("x-dead-letter-routing-key", DLQ_ROUTING_KEY)
                .build();
    }

    // 3. 메인 Exchange & Binding
    @Bean
    public TopicExchange exchange() { return new TopicExchange(EXCHANGE); }
    @Bean
    public Binding binding() {
        return BindingBuilder.bind(mainQueue()).to(exchange()).with(ROUTING_KEY);
    }
}
