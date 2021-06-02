package com.quimibot.service;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

import org.json.JSONArray;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.async.Callback;
import com.mashape.unirest.http.exceptions.UnirestException;

import com.quimibot.model.ChampionMastery;
import com.quimibot.model.Summoner;

public class LolApiService {

    private static String TOKEN = "tokenlol";

    private static final String HOST = "host";
    private static final String USERNAME = "usuario";
    private static final String PASSWORD = "contraseña";
    private static Map<Integer, String> champions = new HashMap<Integer, String>();

    private static Connection getConnection() throws SQLException {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
            System.out.println("Connection failed! PostgreSQL JDBC Driver is not working.");
        }
        Connection conn = DriverManager.getConnection("jdbc:postgresql://" + HOST + "?sslmode=require", USERNAME, PASSWORD);
        return conn;
    }

    public static void PeticionAsincrona() {
        Future<HttpResponse<JsonNode>> future = Unirest.get("https://euw1.api.riotgames.com/lol/champion-mastery/v4/champion-masteries/by-summoner/dPmC3OMLs_eVS2iEdywbU_cH8rvRVf70b55H70OY0AoVXMQ")
                .header("accept", "application/json")
                .header("X-Riot-Token", TOKEN)
                .asJsonAsync(new Callback<JsonNode>() {

                    public void failed(UnirestException e) {
                        System.out.println("The request has failed");
                    }

                    public void completed(HttpResponse<JsonNode> response) {
                        int code = response.getStatus();
                        Map<String, List<String>> headers = response.getHeaders();
                        JsonNode body = response.getBody();
                        InputStream rawBody = response.getRawBody();
                        System.out.println(body.toString());
                    }

                    public void cancelled() {
                        System.out.println("The request has been cancelled");
                    }

                });
    }

    public static String getMasteriesSynchronous(String nombre) throws UnirestException {
        String summonerId = getSummonerId(nombre);
        String res = "MAESTRÍAS DEL INVOCADOR " + nombre.toUpperCase().trim() + ":\n\n";
        HttpResponse<JsonNode> response = Unirest.get("https://euw1.api.riotgames.com/lol/champion-mastery/v4/champion-masteries/by-summoner/" + summonerId)
                .header("accept", "application/json")
                .header("X-Riot-Token", TOKEN)
                .asJson();
        int code = response.getStatus();
        Map<String, List<String>> headers = response.getHeaders();
        JsonNode body = response.getBody();
        InputStream rawBody = response.getRawBody();
        System.out.println(body.toString());
        JSONArray lista = body.getArray();
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            ChampionMastery[] maestrias = objectMapper.readValue(body.toString(), ChampionMastery[].class);
            Integer maximo = 0;
            if (maestrias.length >= 5) {
                maximo = 5;
            } else {
                maximo = maestrias.length;
            }
            List<Integer> championIdList = new ArrayList<Integer>();
            for (int i = 0; i < maximo; i++) {
                championIdList.add(maestrias[i].getChampionId());
            }
            List<String> campeones = getChampions(championIdList);
            for (int i = 0; i < maximo; i++) {
                res += getMasteryEmote(maestrias[i].getChampionLevel()) + campeones.get(i) + ": Maestría " + maestrias[i].getChampionLevel() + ", Puntos: " + maestrias[i].getChampionPoints() + "\n";
            }
        } catch (Exception e) {
            e.printStackTrace();
            res = "No se han podido obtener las maestrías";
        }

        return res;
    }

    public static String getSummonerId(String nombre) throws UnirestException {
        String id = "";
        HttpResponse<JsonNode> response = Unirest.get("https://euw1.api.riotgames.com/lol/summoner/v4/summoners/by-name/" + nombre)
                .header("accept", "application/json")
                .header("X-Riot-Token", TOKEN)
                .asJson();
        JsonNode body = response.getBody();
        System.out.println(body.toString());
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            Summoner invocador = objectMapper.readValue(body.toString(), Summoner.class);
            System.out.println(invocador.getName() + ": " + invocador.getId());
            id = invocador.getId();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("No se han podido obtener los datos del invocador");
        }
        return id;
    }

    public static String getChampion(Integer id) throws Exception {
        if (champions.keySet().contains(id)) {
            return champions.get(id);
        } else {
            Connection conn = null;
            String campeon = "";
            try {
                conn = getConnection();
            } catch (SQLException ex) {
                ex.printStackTrace();
                System.out.println("Error al establecer la conexión.");
            }
            if (conn != null) {
                System.out.println("Conexión con la base de datos establecida.");
            } else {
                System.out.println("No se pudo establecer la conexión.");
            }

            Statement stat = conn.createStatement();

            try {
                ResultSet rs = stat.executeQuery("SELECT * FROM \"LOLCHAMPION\" n WHERE n.championId = '" + id + "'");
                conn.close();

                if (rs.next()) {
                    String obtenido = rs.getString("championname");
                    campeon = obtenido;
                    champions.put(id, campeon);
                } else {
                    System.out.println("No se ha podido encontrar el campeón con id " + id);
                    throw new Exception("No se ha podido encontrar el campeón con id " + id);
                }


            } catch (Exception ex) {
                ex.printStackTrace();
                System.out.println("No se ha podido realizar la consulta.");
                conn.close();
                throw ex;
            }
            return campeon;
        }

    }

    public static List<String> getChampions(List<Integer> lista) throws SQLException {
        Connection conn = null;
        List<String> campeones = new ArrayList<String>();
        try {
            conn = getConnection();
        } catch (SQLException ex) {
            ex.printStackTrace();
            System.out.println("Error al establecer la conexión.");
        }
        if (conn != null) {
            System.out.println("Conexión con la base de datos establecida.");
        } else {
            System.out.println("No se pudo establecer la conexión.");
        }

        Statement stat = conn.createStatement();

        try {
            for (Integer i : lista) {
                ResultSet rs = stat.executeQuery("SELECT * FROM \"LOLCHAMPION\" n WHERE n.championId = '" + i + "'");
                while (rs.next()) {
                    String obtenido = rs.getString("championname");
                    //System.out.println(obtenido);
                    campeones.add(obtenido);
                }
            }
			
			/*System.out.println("Query realizada con éxito.");
			System.out.println("Campeon:");*/
            conn.close();


        } catch (SQLException ex) {
            ex.printStackTrace();
            System.out.println("No se ha podido realizar la consulta.");
            conn.close();
        }
        return campeones;
    }

    private static String getMasteryEmote(Integer maestria) {
        String res = "";
        if (maestria == 7) {
            res = "\uD83D\uDD35 ";
        } else if (maestria == 6) {
            res = "\uD83D\uDFE3 ";
        } else if (maestria == 5) {
            res = "\uD83D\uDFE1 ";
        } else if (maestria == 4) {
            res = "\uD83D\uDFE4 ";
        } else {
            res = "\u26AA ";
        }
        return res;
    }

    //http://tutorials.jenkov.com/java-json/jackson-objectmapper.html
    //http://ddragon.leagueoflegends.com/cdn/6.24.1/data/en_US/champion.json
}
