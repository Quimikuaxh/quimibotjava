package com.quimibot.service;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import org.apache.commons.io.FileUtils;
import org.jsoup.internal.StringUtil;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class CovidService {

    private static String urlDB = "https://www.worldometers.info/coronavirus/#countries";
    private static String lastDate = "";
    private static String updatedCovidInfoText = "";
    private static String filePath = "./files/";
    
    public static String getCovidInfoTop10FromFile() throws IOException{
    	String date = getDate();
    	String[] trozos = date.split("/");
    	String fileName = "covid"+trozos[0]+trozos[1]+trozos[2]+".txt";
    	System.out.println("Filename: "+fileName);
    	File file = new File(filePath+fileName);
    	if(!file.exists()){
    		return "";
    	}
    	String str = FileUtils.readFileToString(file, "utf-8");
    	return str;
    }

    public static String getCovidInfoTop10() {
        String today = getDate();
        if (lastDate.equals(today)) {
            return updatedCovidInfoText;
        }

        WebDriverService webDriverService = new WebDriverService();
        WebDriver driver = webDriverService.getDriver();

        try {
            lastDate = today;
            updatedCovidInfoText = scrapCovidInfoTop10(driver);
            driver.close();

        } catch (Exception e) {
            driver.close();
            e.printStackTrace();
            return "No ha funcionado :(";
        }

        return updatedCovidInfoText;
    }

    private static String buildCovidInfoTop10Text(WebDriver driver) {
        StringBuilder sbCovidInfoTop10 = new StringBuilder("TOP 10 PAÍSES CON CASOS DE COVID-19\n\n");

        List<WebElement> mainCountries = driver.findElements(By.xpath("//table[@id='main_table_countries_today']//a[contains(@href,'country')]"));

        for (int countryCounter = 1; countryCounter <= 10; countryCounter++) {
            WebElement countryWebElement = mainCountries.get((countryCounter - 1));
            String country = countryWebElement.getAttribute("innerText");

            WebElement casesWebElement = countryWebElement.findElement(By.xpath("./../../td[3]"));
            String totalCases = casesWebElement.getAttribute("innerText");

            sbCovidInfoTop10.append(
                    String.format("%d - %s: Casos: %s, Casos nuevos: %s \n",
                            countryCounter, country, totalCases,
                            getNewCasesFromCountry(driver, country)));
        }

        return sbCovidInfoTop10.toString();
    }

    private static String scrapCovidInfoTop10(WebDriver driver) throws InterruptedException {
        driver.get(urlDB);
        WebDriverWait wait = new WebDriverWait(driver, 15);
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("(//tbody)[1]//a[text()='China']")));
        Thread.sleep(1000);
        driver.findElement(By.xpath("//a[contains(text(),'Yesterday')]")).click();

        return buildCovidInfoTop10Text(driver);
    }

    private static String getNewCasesFromCountry(WebDriver driver, String country) {
        String newCases =
                driver.findElement(By.xpath("//div[@id='nav-yesterday']//a[text()='" + country + "']/../../td[4]"))
                        .getText();

        return StringUtil.isBlank(newCases) ? "No calculado" : newCases;
    }

    private static String getDate() {
        Calendar cal = Calendar.getInstance();
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        TimeZone tz = cal.getTimeZone();
        ZoneId zid = tz == null ? ZoneId.systemDefault() : tz.toZoneId();
        LocalDateTime now = LocalDateTime.ofInstant(cal.toInstant(), zid);
        return dtf.format(now);
    }
}
