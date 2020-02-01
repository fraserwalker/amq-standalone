package com.redhat;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.language.SpELExpression;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Configuration
public class Routes extends RouteBuilder {
    @Override
    public void configure() throws Exception {
        from("timer://timedo?fixedRate=true&period=600").id("idOfQueueHere").setBody(new SpELExpression("Hi"))
                .to("amqp:queue:camelExample");
    }
}
