package com.example.producer.configuration;

import com.example.producer.sender.ActiveMQSender;
import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.connection.CachingConnectionFactory;
import org.springframework.jms.core.JmsTemplate;

@Configuration
public class ActiveMQConfiguration {

    @Value("${spring.activemq.broker-url}")
    private String brokerUrl;

    @Value("${spring.artemis.user:}")
    private String username;

    @Value("${spring.artemis.password:}")
    private String password;

    @Bean
    public ActiveMQConnectionFactory senderActiveMQConnectionFactory() {
        ActiveMQConnectionFactory connectionFactory;
        if (username == null || username.isEmpty() || password == null || password.isEmpty()) {
            connectionFactory = new ActiveMQConnectionFactory(brokerUrl);
        } else {
            connectionFactory = new ActiveMQConnectionFactory(brokerUrl, username, password);
        }
        connectionFactory.setConnectionLoadBalancingPolicyClassName("org.apache.activemq.artemis.api.core.client.loadbalance.RoundRobinConnectionLoadBalancingPolicy");
        connectionFactory.setUseTopologyForLoadBalancing(false);
        connectionFactory.setFailoverOnInitialConnection(true);
        return connectionFactory;
    }

    @Bean
    public CachingConnectionFactory cachingConnectionFactory() {
        CachingConnectionFactory cachingConnectionFactory = new CachingConnectionFactory(
                senderActiveMQConnectionFactory());
        cachingConnectionFactory.setSessionCacheSize(5);
        return cachingConnectionFactory;
    }

    @Bean
    public JmsTemplate jmsTemplate() {
        JmsTemplate jmsTemplate = new JmsTemplate(cachingConnectionFactory());
        return jmsTemplate;
    }

    @Bean
    public ActiveMQSender sender() {
        return new ActiveMQSender();
    }

}
