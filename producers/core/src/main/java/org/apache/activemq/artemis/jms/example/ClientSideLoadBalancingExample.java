/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.activemq.artemis.jms.example;

import org.apache.activemq.artemis.core.client.impl.ClientSessionInternal;

import javax.jms.*;
import javax.naming.InitialContext;

/**
 * This example demonstrates how sessions created from a single connection can be load
 * balanced across the different nodes of the cluster.
 * <p>
 * In this example there are three nodes and we use a round-robin client side load-balancing
 * policy.
 */
public class ClientSideLoadBalancingExample {

    public static void main(final String[] args) throws Exception {
        InitialContext initialContext = null;

        Connection connectionA = null;
        Connection connectionB = null;
        Connection connectionC = null;

        try {
            // Step 1. Get an initial context for looking up JNDI from server 0
            initialContext = new InitialContext();

            // Step 2. Look-up the JMS Queue object from JNDI
            Queue queue = (Queue) initialContext.lookup("queue/exampleQueue");

            // Step 3. Look-up a JMS Connection Factory object from JNDI on server 0
            ConnectionFactory connectionFactory = (ConnectionFactory) initialContext.lookup("ConnectionFactory");

            Thread.sleep(2000);
            // Step 6. We create a JMS Connection connection0 which is a connection to server 0
            connectionA = connectionFactory.createConnection("jdoe", "password");

            // Step 7. We create a JMS Connection connection1 which is a connection to server 1
            connectionB = connectionFactory.createConnection("jdoe", "password");

            // Step 6. We create a JMS Connection connection0 which is a connection to server 0
            connectionC = connectionFactory.createConnection("jdoe", "password");

            // Step 5. We create JMS Sessions
            Session sessionA = connectionA.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Session sessionB = connectionB.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Session sessionC = connectionC.createSession(false, Session.AUTO_ACKNOWLEDGE);

            System.out.println("Session A - " + ((ClientSessionInternal) ((org.apache.activemq.artemis.jms.client.ActiveMQSession) sessionA).getCoreSession()).getConnection().getRemoteAddress());
            System.out.println("Session B - " + ((ClientSessionInternal) ((org.apache.activemq.artemis.jms.client.ActiveMQSession) sessionB).getCoreSession()).getConnection().getRemoteAddress());
            System.out.println("Session C - " + ((ClientSessionInternal) ((org.apache.activemq.artemis.jms.client.ActiveMQSession) sessionC).getCoreSession()).getConnection().getRemoteAddress());

            // Step 6. We create JMS MessageProducer objects on the sessions
            MessageProducer producerA = sessionA.createProducer(queue);
            MessageProducer producerB = sessionB.createProducer(queue);
            MessageProducer producerC = sessionC.createProducer(queue);

            // Step 7. We send some messages on each producer
            final int numMessages = 10000;

            for (int i = 0; i < numMessages; i++) {
                try {
                    TextMessage messageA = sessionA.createTextMessage("A:This is text message " + i);
                    producerA.send(messageA);
                    System.out.println("Sent message: " + messageA.getText());

                    TextMessage messageB = sessionB.createTextMessage("B:This is text message " + i);
                    producerB.send(messageB);
                    System.out.println("Sent message: " + messageB.getText());

                    TextMessage messageC = sessionC.createTextMessage("C:This is text message " + i);
                    producerC.send(messageC);
                    System.out.println("Sent message: " + messageC.getText());
                } catch (Exception ex) {
                    System.out.println("Failover event triggered :" + ex.toString());
                    continue;
                }
            }

        } finally {
            // Step 10. Be sure to close our resources!

            if (connectionA != null) {
                connectionA.close();
            }
            if (connectionB != null) {
                connectionB.close();
            }
            if (connectionC != null) {
                connectionC.close();
            }

            if (initialContext != null) {
                initialContext.close();
            }
        }
    }

}
