package com.itprom.jet.ofiice.listener;

import com.github.benmanes.caffeine.cache.Cache;
import com.itprom.jet.common.messages.Message;
import com.itprom.jet.common.processor.MessageConverter;
import com.itprom.jet.common.processor.MessageProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class MessageListener {
    private final MessageConverter messageConverter;
    private final Cache<String, WebSocketSession> sessionCache;

    @Autowired
    private Map<String, MessageProcessor<? extends Message>> processors = new HashMap<>();

    @KafkaListener(id = "OfficeGroupId", topics = "officeRoutes")
    public void kafkaListen(String data) {
        sendKafkaMessageToSocket(data);
        String coda = messageConverter.extractCode(data);

        try {
            processors.get(coda).process(data);
        } catch (Exception e) {
            log.error(e.getLocalizedMessage());
        }
    }

    private void sendKafkaMessageToSocket(String data) {
        sessionCache.asMap().values().forEach((webSocketSession -> {
            if (webSocketSession.isOpen()) {
                try {
                    webSocketSession.sendMessage(new TextMessage(data));
                } catch (IOException e) {
                    log.error(e.getLocalizedMessage());
                }
            }
        }));
    }

}
