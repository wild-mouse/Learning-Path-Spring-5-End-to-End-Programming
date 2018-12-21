package springfive.airline.airlinepayments.infra.rabbitmq;

import lombok.val;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.rabbitmq.ReactorRabbitMq;
import reactor.rabbitmq.Receiver;
import reactor.rabbitmq.ReceiverOptions;
import reactor.rabbitmq.Sender;
import reactor.rabbitmq.SenderOptions;

@Configuration
public class RabbitMQConfiguration {

  private final String pass;

  private final String user;

  private final String host;

  private final Integer port;

  private final String requestPaymentQueue;

  private final String responsePaymentQueue;

  public RabbitMQConfiguration(@Value("${spring.rabbitmq.password}") String pass,
      @Value("${spring.rabbitmq.username}") String user,
      @Value("${spring.rabbitmq.host}") String host,
      @Value("${spring.rabbitmq.port}") Integer port,
      @Value("${payment.request-payment-queue}") String requestPaymentQueue,
      @Value("${payment.response-payment-queue}") String responsePaymentQueue) {
    this.pass = pass;
    this.user = user;
    this.host = host;
    this.port = port;
    this.requestPaymentQueue = requestPaymentQueue;
    this.responsePaymentQueue = responsePaymentQueue;
  }

  @Bean("springConnectionFactory")
  public ConnectionFactory connectionFactory() {
    CachingConnectionFactory factory = new CachingConnectionFactory();
    factory.setUsername(this.user);
    factory.setPassword(this.pass);
    factory.setHost(this.host);
    factory.setPort(this.port);
    return factory;
  }

  @Bean
  public AmqpAdmin amqpAdmin(@Qualifier("springConnectionFactory") ConnectionFactory connectionFactory) {
    return new RabbitAdmin(connectionFactory);
  }

  @Bean("requestPaymentExchange")
  public TopicExchange requestPaymentExchange() {
    return new TopicExchange("request-payment", true, false);
  }

  @Bean("responsePaymentExchange")
  public TopicExchange responsePaymentExchange() {
    return new TopicExchange("response-payment", true, false);
  }

  @Bean("requestPaymentQueue")
  public Queue requestPaymentQueue() {
    return new Queue(this.requestPaymentQueue, true, false, false);
  }

  @Bean("responsePaymentQueue")
  public Queue responsePaymentQueue() {
    return new Queue(this.responsePaymentQueue, true, false, false);
  }

  @Bean("requestPaymentExchangeBinding")
  public Binding requestPaymentExchangeBinding(@Qualifier("requestPaymentQueue") Queue requestPaymentQueue,@Qualifier("requestPaymentExchange") TopicExchange requestPaymentExchange) {
    return BindingBuilder.bind(requestPaymentQueue).to(requestPaymentExchange).with("*");
  }

  @Bean("responsePaymentExchangeBinding")
  public Binding responsePaymentExchangeBinding(@Qualifier("responsePaymentQueue") Queue responsePaymentQueue,@Qualifier("responsePaymentExchange") TopicExchange responsePaymentExchange) {
    return BindingBuilder.bind(responsePaymentQueue).to(responsePaymentExchange).with("*");
  }

  @Bean("reactorConnectionFactory")
  public com.rabbitmq.client.ConnectionFactory rabbitReactorConnectionFactory(){
    com.rabbitmq.client.ConnectionFactory connectionFactory = new com.rabbitmq.client.ConnectionFactory();
    connectionFactory.setUsername(this.user);
    connectionFactory.setPassword(this.pass);
    connectionFactory.setPort(this.port);
    connectionFactory.setHost(this.host);
    return connectionFactory;
  }

  @Bean
  public Receiver receiver(com.rabbitmq.client.ConnectionFactory connectionFactory) {
    val options = new ReceiverOptions();
    options.connectionFactory(connectionFactory);
    return ReactorRabbitMq.createReceiver(options);
  }

  @Bean
  public Sender sender(com.rabbitmq.client.ConnectionFactory connectionFactory){
    val options = new SenderOptions();
    options.connectionFactory(connectionFactory);
    return ReactorRabbitMq.createSender(options);
  }

}
