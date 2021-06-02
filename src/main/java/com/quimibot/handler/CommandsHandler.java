package com.quimibot.handler;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;


public class CommandsHandler extends TelegramLongPollingBot {

    //private Map<String, Integer> dias = new HashMap<String, Integer>();
    private static Boolean pruebas = false;

    public String getBotUsername() {
        // Se devuelve el nombre que dimos al bot al crearlo con el BotFather
        return "quimi_bot";
    }

    @Override
    public String getBotToken() {
        // Se devuelve el token que nos generó el BotFather de nuestro bot
        String res = "token"; //quimi
        //String res = "token"; //qu1m1

        if ("tokenpruebas".equals(res)) {
            pruebas = true;
        }
        return res;
    }

    public void onUpdateReceived(Update update) {

        //Se crea un hilo nuevo para cada llamada.
        new Thread(() -> {
            if (update.hasMessage()) {
                // Se obtiene el mensaje escrito por el usuario
                final String messageTextReceived = update.getMessage().getText();
                System.out.println("INFO: Comando solicitado: \"" + messageTextReceived + "\"");
                SendMessage messageToSend = new SendMessage();
                messageToSend.setParseMode(ParseMode.MARKDOWN);

                // Se obtiene el id de chat del usuario
                final long chatId = update.getMessage().getChatId();

                if (messageTextReceived.startsWith("/start")) {
                    startHandler(chatId, messageToSend);
                } else if (messageTextReceived.startsWith("/pokemon")) {
                    PokemonHandler pokemonSimpleHandler = new PokemonHandler();
                    pokemonSimpleHandler.handle(chatId, messageTextReceived, messageToSend);
                } else if (messageTextReceived.startsWith("/covid")) {
                    CovidHandler covidSimpleHandler = new CovidHandler(this);
                    covidSimpleHandler.handle(chatId, messageTextReceived, messageToSend);
                } else if (messageTextReceived.startsWith("/nabos")
                        || messageTextReceived.startsWith("/prediccion")
                        || messageTextReceived.startsWith("/reset")) {
                    AnimalCrossingHandler animalCrossingHandler = new AnimalCrossingHandler(this);
                    animalCrossingHandler.handle(chatId, messageTextReceived, messageToSend);
                } else if (messageTextReceived.startsWith("/maestrias")) {
                    LolApiHandler lolApiSimpleHandler = new LolApiHandler();
                    lolApiSimpleHandler.handle(chatId, messageTextReceived, messageToSend);
                } else if(messageTextReceived.startsWith("/anonimo")
                		||messageTextReceived.startsWith("/anonimoaqui")){
                	AnonimoHandler anonimoHandler = new AnonimoHandler();
                	anonimoHandler.handle(chatId, messageTextReceived, messageToSend);
                } else {
                    if (messageTextReceived.startsWith("/")) {
                        messageToSend.setChatId(chatId).setText("No entiendo tu comando. Para saber todo lo que puedo hacer, usa el comando */start*");
                    }
                }

                try {
                    // Se envía el mensaje
                    execute(messageToSend);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void startHandler(long chatId, SendMessage message) {
        String messageToSend = "";
        try {
            messageToSend = "Hola, soy Quimibot, ¡y hago un montón de cosas! Entre ellas, puedo hacer las siguientes:\n\n" +
                    "*ANIMAL CROSSING:* \n" +
                    "*/nabos [persona] [mañana/tarde] [cantidad] |* Almaceno la cantidad de bayas que ofrecen por tus nabos hoy en el momento que me digas.\n" +
                    "*/nabos [persona] |* Te digo toda la información de esta semana sobre el precio de los nabos en tu isla que me hayas indicado. \n" +
                    "*/nabos ahora |* Te digo los precios de los nabos en todas las islas para las que me hayan dado sus precios. \n" +
                    "*/prediccion [persona] |* Te digo una predicción de a cuánto pueden comprarse los nabos en tu isla durante la semana, en función de la información que me hayas dado.\n\n" +

                    "*POKÉMON:*\n" +
                    "*/pokemon [nombre] |* Te digo los tipos del Pokémon que me pidas.\n\n" +

                    "*LEAGUE OF LEGENDS:*\n" +
                    "*/maestrias [nombreInvocador] |* Te digo el top 5 de campeones por puntos de maestría del invocador.\n\n" +

                    "*Otros:*\n" +
                    "*/covid |* Te digo los diez países con más casos de COVID-19 actualmente";

            message.setChatId(chatId).setText(messageToSend);
        } catch (Exception ioe) {
            System.out.println("Se ha producido un error: ");
            ioe.printStackTrace();
            messageToSend = "Se ha producido un error al intentar realizar el comando solicitado.";
            message.setChatId(chatId).setText(messageToSend);
        }
    }

    public static Boolean getPruebas() {
        return pruebas;
    }

}