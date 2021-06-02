package com.quimibot.handler;

import com.quimibot.service.CovidService;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

public class CovidHandler implements Handler {

    private final TelegramLongPollingBot longPollingBot;

    public CovidHandler(TelegramLongPollingBot longPollingBot){
        this.longPollingBot = longPollingBot;
    }

    @Override
    public void handle(long chatId, String messageTextReceived, SendMessage message) {
        try {
            SendMessage aviso = new SendMessage();
            aviso.setChatId(chatId).setText("Se están realizando los cálculos, espera...");
            longPollingBot.execute(aviso);
            String messagefromFile = CovidService.getCovidInfoTop10FromFile();
            if(messagefromFile.equals("")){
            	System.out.println("No estoy leyendo del fichero");
            	messagefromFile = CovidService.getCovidInfoTop10();
            }
            message.setChatId(chatId).setText(messagefromFile);
        } catch (Exception ioe) {
            System.out.println("Se ha producido un error: ");
            ioe.printStackTrace();
        }
    }
}
