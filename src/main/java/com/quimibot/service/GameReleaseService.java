package com.quimibot.service;


import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class GameReleaseService {

    private static String url = "https://crackwatch.com/search";

    public static String buildGameRelease(String game) {
    	String message="";
        WebDriverService webDriverService = new WebDriverService();
        WebDriver driver = webDriverService.getDriver();
    	driver.get(url);
    	WebDriverWait wait = new WebDriverWait(driver, 15);
    	try{
    		wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//input[@class='bar-search']")));
            driver.findElement(By.xpath("//input[@class='bar-search']")).sendKeys(game);
            wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//div[@class='game-row'][1]")));
            String gameFullName = driver.findElement(By.xpath("//div[@class='game-row'][1]/../a[contains('@href', '/game')]")).getText();
            if(gameFullName.contains("ADD A NEW GAME")){
            	message = "No se ha podido encontrar el juego, o no existe fecha de salida para él.";
            }
            else{
            	String releaseDate = driver.findElement(By.xpath("//div[@class='game-row'][1]/./div[@class='game-row-release-date']")).getText();
            	//game-row-release-date
            	String daysRemaining = driver.findElement(By.xpath("//font[@class='status-not-released'][1]")).getText().replace("UNRELEASED D-", "").trim();
            	//status-not-released
            	message += "Juego: "+gameFullName+"\n";
            	message += "Fecha de salida: "+releaseDate+"\n";
            	message += "Faltan "+daysRemaining+" días";
            }
            return message;	
    	}catch(Exception e){
    		System.out.println(e.getMessage());
    		message = "Se ha producido un error al buscar el juego solicitado.";
    		throw e;
    		
    	}
    	
        
    }
}
