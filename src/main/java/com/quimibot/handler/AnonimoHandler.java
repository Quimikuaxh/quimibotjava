package com.quimibot.handler;

import com.quimibot.service.AnonimoService;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

public class AnonimoHandler implements Handler {

    public AnonimoHandler(){ }
    
    @Override
    public void handle(long chatId, String messageTextReceived, SendMessage message) {
        if (messageTextReceived.startsWith("/anonimoaqui")) {
            this.handleAnonimoAqui(chatId, message);
        } else if (messageTextReceived.startsWith("/anonimo")) {
            this.handleAnonimo(messageTextReceived.replace("/anonimo", ""), message);
        }
    }

    public void handleAnonimo(String messageTextReceived, SendMessage message) {
        try {
        	String chatAnonimo = AnonimoService.getChatId();
            message.setChatId(chatAnonimo).setText(messageTextReceived);
        } catch (Exception ioe) {
            System.out.println("Se ha producido un error: ");
            ioe.printStackTrace();
        }
    }
    
    public void handleAnonimoAqui(long chatId, SendMessage message) {
        try {
        	String chatAnonimo = AnonimoService.setChatIdBBDD("anonimo", Long.toString(chatId));
            message.setChatId(chatId).setText("Se ha actualizado correctamente el chat donde voy a escribir :)");
        } catch (Exception ioe) {
            System.out.println("Se ha producido un error: ");
            ioe.printStackTrace();
        }
    }
}
