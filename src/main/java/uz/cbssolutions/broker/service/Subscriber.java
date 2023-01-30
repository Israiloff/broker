package uz.cbssolutions.broker.service;

import uz.cbssolutions.broker.model.Message;

public interface Subscriber<TMessage> {
    Class<TMessage> getMsgClass();

    String getTopic();

    void handle(Message<TMessage> message);
}

