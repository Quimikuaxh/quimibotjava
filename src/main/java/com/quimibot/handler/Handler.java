package com.quimibot.handler;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

public interface Handler {
    void handle(long chatId, String messageTextReceived, SendMessage message);
}
