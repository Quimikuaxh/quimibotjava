package com.quimibot.handler;

import com.quimibot.service.GameReleaseService;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

public class GameReleaseHandler implements Handler {

    public GameReleaseHandler(){ }
    
    @Override
    public void handle(long chatId, String messageTextReceived, SendMessage message) {
    	if (messageTextReceived.startsWith("/cuantofalta")) {
            GameReleaseService.buildGameRelease((messageTextReceived.replace("/cuantofalta ", "")));
        }
    }

}
