package com.example.producer.service;

import com.example.producer.sender.ActiveMQSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class SenderService {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(SenderService.class);

    public SenderService(@Autowired ActiveMQSender sender) {
        long iterationSender = 0;
        while (true) {
            try {
                sender.send(new StringBuilder("Message ").append(iterationSender++).append(" @ ").append(LocalDateTime.now()).toString());
            } catch (Exception e) {
                LOGGER.error(e.getMessage());
                continue;
            }
        }
    }
}
