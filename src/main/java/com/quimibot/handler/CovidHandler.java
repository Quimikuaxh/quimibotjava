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
    	String messagefromFile ="";
        try {
            SendMessage aviso = new SendMessage();
            aviso.setChatId(chatId).setText("Se están realizando los cálculos, espera...");
            longPollingBot.execute(aviso);
            messagefromFile = CovidService.getCovidInfoTop10FromFile();
            if(messagefromFile.equals("")){
            	System.out.println("No estoy leyendo del fichero");
            	messagefromFile = CovidService.getCovidInfoTop10();
            }
            message.setChatId(chatId).setText(messagefromFile);
        } catch (Exception ioe) {
        	message.setChatId(chatId).setText("No se han podido obtener los datos, puede ser que aún no se hayan actualizado. Por favor, prueba de nuevo más tarde.");
            System.out.println("Se ha producido un error: ");
            ioe.printStackTrace();
        }
    }
}
