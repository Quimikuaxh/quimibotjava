package com.quimibot.service;

import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


public class PokemonService {

    private static String urlDB = "https://pokemondb.net/pokedex/national";
    private static String urlSmogon = "https://www.smogon.com/dex/ss/pokemon/";

    public static String getPokemonInfo(String nombre) throws IOException {
        String minus = nombre.toLowerCase();
        String res = nombre + " types: ";

        // Conexión con la página
        Document doc = Jsoup.connect(urlDB).get();

        // Busca los tipos por pokémon
        Elements types = doc.select("a[href='/pokedex/" + minus + "']~small>a");
        if (!types.isEmpty()) {
            for (Element e : types) {
                res += e.text() + ", ";
            }
            res = res.substring(0, res.length() - 2);
        } else {
            res += "Not found";
        }
        return res;
    }

	/*public static String getMovimientos(String nombre) throws Exception{
		//https://www.scrapingbee.com/blog/introduction-to-web-scraping-with-java/
	}*/

}
