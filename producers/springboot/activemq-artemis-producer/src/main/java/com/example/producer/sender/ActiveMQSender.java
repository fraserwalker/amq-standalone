package com.example.producer.sender;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;


public class ActiveMQSender1 {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(ActiveMQSender1.class);

    private JmsTemplate jmsTemplate;

    public ActiveMQSender1(JmsTemplate jmsTemplate1) {
        this.jmsTemplate = jmsTemplate1;
    }

    public void send(String message) {
        LOGGER.info("sending message='{}'", message);
        jmsTemplate.convertAndSend("helloworld.q", message);
    }

}
