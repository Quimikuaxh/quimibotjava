package com.quimibot.handler;

import com.quimibot.service.AnimalCrossingService;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

public class AnimalCrossingHandler implements Handler {

    private final TelegramLongPollingBot longPollingBot;

    public AnimalCrossingHandler(TelegramLongPollingBot longPollingBot) {
        this.longPollingBot = longPollingBot;
    }

    @Override
    public void handle(long chatId, String messageTextReceived, SendMessage message) {
        if (messageTextReceived.startsWith("/reset")) {
            this.handleResetPredicciones(chatId, messageTextReceived, message);
        } else if (messageTextReceived.startsWith("/prediccion")) {
            this.handlePrediccionNabos(chatId, messageTextReceived, message);
        } else if (messageTextReceived.startsWith("/nabos")) {
            this.handleNabos(chatId, messageTextReceived, message);
        }
    }

    private void handleNabos(long chatId, String messageTextReceived, SendMessage message) {
        String messageToSend = "";
        String[] atributos = messageTextReceived.split(" ");
        try {
            if (atributos.length == 4) {
                if ((atributos[2].toLowerCase().equals("mañana") || atributos[2].toLowerCase().equals("tarde")) && atributos[3].chars().allMatch(Character::isDigit)) {
                    messageToSend = AnimalCrossingService.setNabosBBDD(atributos[1], atributos[2], Integer.valueOf(atributos[3]));
                    message.setChatId(chatId).setText(messageToSend);
                } else {
                    messageToSend = "Los parámetros enviados no cumplen el formato esperado. Para saber todo lo que puedo hacer, usa el comando /start";
                    message.setChatId(chatId).setText(messageToSend);
                }
            } else if (atributos.length == 2) {
                if (atributos[1].toLowerCase().equals("ahora")) {
                    messageToSend = AnimalCrossingService.getNabosPorDia();
                    message.setChatId(chatId).setText(messageToSend);
                } else {
                    messageToSend = AnimalCrossingService.getNabos(atributos[1]);
                    message.setChatId(chatId).setText(messageToSend);
                }
            } else {
                message.setChatId(chatId).setText("Los parámetros introducidos no son correctos. Para saber todo lo que puedo hacer, usa el comando /start");
            }
        } catch (Exception ioe) {
            System.out.println("Se ha producido un error: ");
            ioe.printStackTrace();
            messageToSend = "Se ha producido un error al intentar realizar el comando solicitado.";
            message.setChatId(chatId).setText(messageToSend);
        }
    }

    private void handlePrediccionNabos(long chatId, String messageTextReceived, SendMessage message) {
        String messageToSend = "";
        try {
            String usuario = messageTextReceived.split(" ")[1];
            System.out.println("\"" + messageTextReceived + "\"");
            SendMessage aviso = new SendMessage();
            aviso.setChatId(chatId).setText("Se están realizando los cálculos, espera...");
            longPollingBot.execute(aviso);
            messageToSend = AnimalCrossingService.getPrediccion(usuario);
            message.setChatId(chatId).setText(messageToSend);
        } catch (Exception ioe) {
            System.out.println("Se ha producido un error: ");
            ioe.printStackTrace();
            messageToSend = "Se ha producido un error al intentar realizar el comando solicitado.";
            message.setChatId(chatId).setText(messageToSend);
        }
    }

    private void handleResetPredicciones(long chatId, String messageTextReceived, SendMessage message) {
        String messageToSend = "";
        try {
            AnimalCrossingService.reset();
            System.out.println("\"" + messageTextReceived + "\"");
            messageToSend = "Se han reseteado las predicciones con éxito.";
            message.setChatId(chatId).setText(messageToSend);
        } catch (Exception ioe) {
            System.out.println("Se ha producido un error: ");
            ioe.printStackTrace();
            messageToSend = "Se ha producido un error al intentar realizar el comando solicitado.";
            message.setChatId(chatId).setText(messageToSend);
        }
    }

}
