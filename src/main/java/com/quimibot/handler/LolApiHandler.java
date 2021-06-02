package com.quimibot.handler;

import com.quimibot.service.LolApiService;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

public class LolApiHandler implements Handler {
    @Override
    public void handle(long chatId, String messageTextReceived, SendMessage message) {
        String messageToSend = "";
        try {
            String invocador = messageTextReceived.substring(messageTextReceived.split(" ")[0].length());
            System.out.println("\"" + messageTextReceived + "\"");
            messageToSend = LolApiService.getMasteriesSynchronous(invocador);
            message.setChatId(chatId).setText(messageToSend);
        } catch (Exception ioe) {
            System.out.println("Se ha producido un error: ");
            ioe.printStackTrace();
            messageToSend = "Se ha producido un error al intentar realizar el comando solicitado.";
            message.setChatId(chatId).setText(messageToSend);
        }
    }
}
