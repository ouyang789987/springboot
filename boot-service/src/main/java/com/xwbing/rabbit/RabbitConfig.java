package com.xwbing.rabbit;

import com.alibaba.fastjson.JSONArray;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

/**
 * 项目名称: boot-module-demo
 * 创建时间: 2018/4/27 13:42
 * 作者: xiangwb
 * 说明: 配置类
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "spring.rabbitmq")
public class RabbitConfig {
    private String host;
    private String port;
    private String username;
    private String password;
    private String virtualHost;
    private int connectionTimeout;
    private boolean publisherConfirms;
    private boolean publisherReturns;
    private final Logger logger = LoggerFactory.getLogger(RabbitConfig.class);

    @Bean
    public ConnectionFactory connectionFactory() {
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory();
        connectionFactory.setAddresses(host + ":" + port);
        connectionFactory.setUsername(username);
        connectionFactory.setPassword(password);
        connectionFactory.setVirtualHost(virtualHost);
        connectionFactory.setConnectionTimeout(connectionTimeout);
        //如果要进行消息回调,则这里必须要设置为true
        connectionFactory.setPublisherConfirms(publisherConfirms);
        connectionFactory.setPublisherReturns(publisherReturns);
        return connectionFactory;
    }

    /**
     * 因为要设置回调类，所以应是prototype类型，如果是singleton类型，则回调类为最后一次设置
     * ConfirmCallback接口用于实现消息发送到RabbitMQ交换器后接收ack回调   即消息发送到exchange  ack
     * ReturnCallback接口用于实现消息发送到RabbitMQ 交换器，但无相应队列与交换器绑定时的回调  即消息发送不到任何一个队列中  ack
     */
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    @Bean
    public RabbitTemplate rabbitTemplate() {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory());
//        rabbitTemplate.setEncoding("UTF-8");
        rabbitTemplate.setMandatory(true);
        //相应交换机接收后异步回调
        rabbitTemplate.setConfirmCallback((correlationData, ack, cause) -> {
            if (ack) {
                logger.info("交换机接收信息成功,id:{}", correlationData.getId());
            } else {
                logger.error("交换机接收信息失败:{}", cause);
            }
        });
        //无相应队列与交换机绑定异步回调
        rabbitTemplate.setReturnCallback((message, replyCode, replyText, exchange, routingKey) -> {
            byte[] body = message.getBody();
            JSONArray jsonArray = new JSONArray();
            String msg = new String(message.getBody());
            logger.error("消息:{} 发送失败, 应答码:{} 原因:{} 交换机:{} 路由键:{}", msg, replyCode, replyText, exchange, routingKey);
        });
        return rabbitTemplate;
    }
}