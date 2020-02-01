package com.example.producer.sender;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;


public class ActiveMQSender {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(ActiveMQSender.class);

    @Autowired
    private JmsTemplate jmsTemplate;

    public void send(String message) {
        LOGGER.info("sending message='{}'", message);
        jmsTemplate.convertAndSend("helloworld.q", message);
    }

}
