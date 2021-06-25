package com.example.rabbitmq;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.DirectMessageListenerContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

@SpringBootApplication
public class RabbitmqApplication implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(RabbitmqApplication.class, args);
    }

    private static final boolean NON_DURABLE = false;
    private static final String MY_QUEUE_NAME = "myQueue";
    private static final String MY_QUEUE_NAME1 = "myQueue1";
    private static final String MY_QUEUE_NAME2 = "myQueue2";
    private static final String MY_QUEUE_NAME3 = "myQueue3";
    private static final String MY_QUEUE_NAME4 = "myQueue4";
    private static final String MY_QUEUE_NAME5 = "myQueue5";

    List<String> queues = Arrays.asList(MY_QUEUE_NAME1, MY_QUEUE_NAME2, MY_QUEUE_NAME3, MY_QUEUE_NAME4, MY_QUEUE_NAME5);
    @Autowired
    ConnectionFactory connectionFactory;

    @Autowired
    RabbitAdmin rabbitAdmin;

    @Autowired
    RabbitTemplate template;
    @Bean
    public ApplicationRunner runner() {
        createQueues();
        registerQueues();
        return args -> {

        };
    }

    private void createQueue(String name) {
        Properties queueProperties = rabbitAdmin.getQueueProperties(name);
        if (queueProperties == null) {
            Queue queue = QueueBuilder.durable(name).build();
            rabbitAdmin.declareQueue(queue);
        }

    }

    @Override
    public void run(String... args) {
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        for (int i = 0; i < 50; i++) {
            template.convertAndSend(MY_QUEUE_NAME + (i / 10 + 1), "value1 " + i);
        }
    }

    private void createQueues() {
        queues.forEach(this::createQueue);
    }

    public void registerQueues() {
        DirectMessageListenerContainer container = new DirectMessageListenerContainer(connectionFactory);
        container.setQueueNames(queues.toArray(new String[]{}));
        container.setConsumersPerQueue(1);
        container.setMessageListener(message -> {
            System.out.println(LocalTime.now() + " " + new String(message.getBody()));
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        container.start();
    }

//    @Bean
//    public DirectRabbitListenerContainerFactory messageListenerContainer() {
//        DirectRabbitListenerContainerFactory directRabbitListenerContainerFactory = new DirectRabbitListenerContainerFactory();
//        directRabbitListenerContainerFactory.setConnectionFactory(connectionFactory);
//        directRabbitListenerContainerFactory.setConsumersPerQueue(1);
//        return directRabbitListenerContainerFactory;
//    }

//    @Bean
//    public SimpleRabbitListenerContainerFactory myRabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
//        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
//        factory.setConnectionFactory(connectionFactory);
//        factory.setMaxConcurrentConsumers(1);
//        return factory;
//    }

//    @RabbitListener(queues = {MY_QUEUE_NAME, MY_QUEUE_NAME2, MY_QUEUE_NAME3}, concurrency = "10" , bindings = )

//    @RabbitListener(bindings = @QueueBinding(
//            value = @org.springframework.amqp.rabbit.annotation.Queue(value = "#{queue1.name}", durable = "true", exclusive = "false", autoDelete = "false"),
//            exchange = @Exchange(value="U19013.shareOrder", type= ExchangeTypes.DIRECT),
//            key = "#{queue1.name}",ignoreDeclarationExceptions="true"))
//    @RabbitHandler
//    public void listen(String in) {
//        System.out.println(LocalTime.now() + " Message: " + in);
//        try {
//            Thread.sleep(1000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//    }

//    @RabbitListener(queues = {MY_QUEUE_NAME, MY_QUEUE_NAME2, MY_QUEUE_NAME3}, containerFactory = "messageListenerContainer")
//    public void listen(String in) {
//        System.out.println(LocalTime.now() + " Message: " + in);
//        try {
//            Thread.sleep(1000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//    }


}
