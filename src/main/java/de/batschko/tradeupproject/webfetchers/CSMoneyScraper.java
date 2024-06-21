package de.batschko.tradeupproject.webfetchers;

import de.batschko.tradeupproject.db.query.QRCSMoneyPrice;
import de.batschko.tradeupproject.enums.Condition;
import de.batschko.tradeupproject.utils.SkinUtils;
import lombok.extern.slf4j.Slf4j;
import org.jooq.Record3;
import org.jooq.Record4;
import org.jooq.Result;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.Duration;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;



/**
 * Selenium Scraper for CSMoney/Wiki data
 */

@Slf4j
public class CSMoneyScraper {


    public static void updatePrice(Result<Record4<String, String, Double, Double>> nameList) {
        WebDriver driver = new ChromeDriver();
        Map<String, String> specialCharsMapping = SkinUtils.getCSMoneyWikiSpecialNames();
        log.info("updating {} names", nameList.size());
        int loops = 0;
        for (Record4<String, String, Double, Double> name : nameList) {
            loops++;
            String weapon = name.get(0, String.class);
            String title = name.get(1, String.class);

            log.info("{} {} ",weapon,title);
            final String originalTitle = title;
            String newTitle = specialCharsMapping.get(title);
            if (newTitle != null) {
                title = newTitle;
            }

            int index = title.lastIndexOf("'");
            if(index !=-1 && title.charAt(index+2)==' '){
                title = title.replaceAll("'", "-");
            }else if(index !=-1 && title.charAt(index+1)!='-'){
                title = title.replaceAll("'", "-");
            }
            title = title.replaceAll("['._]", "");



            String urlString = "https://wiki.cs.money/weapons/" + weapon.replace(" ", "-") + "/" + title.replace(" ", "-");
            URL url;
            try {
                url = new URI(urlString.toLowerCase()).toURL();
            } catch (MalformedURLException | URISyntaxException e) {
                throw new RuntimeException(e);
            }

            driver.get(url.toString());
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
            if(driver.getTitle().isEmpty()){
                log.warn("NO URL TITLE   {}",url);
                continue;
            }

            WebElement priceBoxStat = null;
            WebElement priceBox;
            String basicStat;

            boolean stattrak = true;
            try {

                wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".yyofkzfzpvevfrzbkbcwiwleal > tr:nth-child(1) > td:nth-child(2)")));
                priceBoxStat =driver.findElement(By.cssSelector(".yyofkzfzpvevfrzbkbcwiwleal > tr:nth-child(2)"));
                basicStat = priceBoxStat.findElement(By.cssSelector("td:nth-child(1)")).getText();
                if (!basicStat.equals("StatTrak™")) {
                    if(!basicStat.equals("Souvenir™")){
                        throw new RuntimeException("Error finding StatTrak prices");
                    }
                    stattrak=false;

                }

            } catch (Exception e) {
                // Handle the exception
                //kein stat
               // System.out.println("Stat Element not found. Continuing with the program...");
                stattrak=false;
            }



            priceBox = driver.findElement(By.cssSelector(".yyofkzfzpvevfrzbkbcwiwleal > tr:nth-child(1)"));
            basicStat = priceBox.findElement(By.cssSelector("td:nth-child(1)")).getText();
            if (!basicStat.equals("Basic")) {
                throw new RuntimeException("Error finding Basic prices");
            }

            Map<String, Double> priceMap = new HashMap<>();
            Map<String, Double> priceMapStat = new HashMap<>();

            List<WebElement> conditionElements = driver.findElements(By.cssSelector("tr.qfcqvyincvwuzealgalydtkcew > th" ));


            Pattern pattern = Pattern.compile(">(.*)<");
            Matcher matcher;

            for (int i = 2; i < 7; i++) {
                try {
                    double price = Double.parseDouble(priceBox.findElement(By.cssSelector("td:nth-child(" + i + ")")).getText().substring(2).replaceAll(" ",""));

                    matcher = pattern.matcher(conditionElements.get(i-1).getAttribute("outerHTML"));
                    matcher.find();
                    String cond = matcher.group(1);
                    priceMap.put(cond, price);

                    if(stattrak){
                        double priceStat = Double.parseDouble(priceBoxStat.findElement(By.cssSelector("td:nth-child(" + i + ")")).getText().substring(2).replaceAll(" ",""));
                        priceMapStat.put(cond, priceStat);
                    }
                } catch (Exception e) {
                    //TODO
                   // throw new RuntimeException("todo");
                }

            }

            Result<Record3<Integer, Condition, Byte>> missingSkins = QRCSMoneyPrice.getSkinIdsFromName(weapon, originalTitle);
            for (Record3<Integer, Condition, Byte> skin : missingSkins) {
                byte stat = skin.get(2, Byte.class);
                String cond = skin.get(1, Condition.class).getText();
                int id = skin.get(0, Integer.class);
                try{
                    if (stat == 1) {
                        QRCSMoneyPrice.update(id, priceMapStat.get(cond));
                    } else {
                        QRCSMoneyPrice.update(id, priceMap.get(cond));
                    }
                    log.info("updated {} {} stat:{}  {}",weapon,title, stat, cond);
                }catch (NullPointerException e){
                    log.warn("skipping {} {} {} {}",weapon, title, stat, cond);
                    log.warn("map {} ",priceMap);
                    log.warn("mapStat {} ",priceMapStat);
                }


            }
            log.info("loop {}", loops);
        }

        driver.quit();
    }


}
