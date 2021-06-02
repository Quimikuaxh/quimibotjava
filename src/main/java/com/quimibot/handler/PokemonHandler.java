package com.quimibot.handler;

import com.quimibot.service.PokemonService;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.io.IOException;

public class PokemonHandler implements Handler {
    @Override
    public void handle(long chatId, String messageTextReceived, SendMessage message) {
        try {
            String pokemon = messageTextReceived.split(" ")[1];
            System.out.println("\"" + messageTextReceived + "\"");
            String messageToSend = PokemonService.getPokemonInfo(pokemon);
            message.setChatId(chatId).setText(messageToSend);
        } catch (IOException ioe) {
            System.out.println("Se ha producido un error: ");
            ioe.printStackTrace();
        }
    }
}
