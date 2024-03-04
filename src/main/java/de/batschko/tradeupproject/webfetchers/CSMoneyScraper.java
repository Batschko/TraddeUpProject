package de.batschko.tradeupproject.webfetchers;

import de.batschko.tradeupproject.enums.Condition;
import de.batschko.tradeupproject.utils.SkinUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

import java.net.URI;
import java.net.URL;
import java.util.*;

/**
 * Selenium Scraper for CSMoney/Wiki data
 */
public class CSMoneyScraper {
    /**
     * Gets cs money wiki prices for a list of {@link SkinUtils.SkinFullName}.
     *
     * @param skinList the skin list as list of {@link SkinUtils.SkinFullName}
     * @return list of price maps with skin condition as key and price as value,
     * the first list entry holds a map of all used Conditions with null as value
     */
    public static List<Map<String, Double>> getCSMoneyWikiPriceMapList(List<SkinUtils.SkinFullName> skinList) {
        List<Map<String, Double>> priceMapList = new ArrayList<>();
        Map<String,Double> conditionSet = new HashMap<>();
        WebDriver driver = new ChromeDriver();
        Map<String, String> specialCharsMapping = SkinUtils.getSpecialSkinNamesMap(true);
        Set<String> usedUrls = new HashSet<>();
        for (SkinUtils.SkinFullName skin : skinList) {
            try {
                conditionSet.put(skin.getCondition().getText(), null);
                String title = skin.getTitle();
                String newTitle = specialCharsMapping.get(title);
                if (newTitle != null) {
                    title = newTitle.replaceAll("[^a-zA-Z]", "-");
                }
                String urlString = "https://wiki.cs.money/weapons/" + skin.getWeapon().replace(" ", "-") + "/" + title.replace(" ", "-");
                URL url = new URI(urlString.toLowerCase()).toURL();
                if(usedUrls.contains(url.toString()))continue;


                driver.get(url.toString());
                usedUrls.add(url.toString());
                WebElement priceBox;
                String basicStat;
                if (skin.getStattrak() == 1) {
                    priceBox = driver.findElement(By.cssSelector(".yyofkzfzpvevfrzbkbcwiwleal > tr:nth-child(2)"));
                    basicStat = priceBox.findElement(By.cssSelector("td:nth-child(1)")).getText();
                    if (!basicStat.equals("StatTrak™")) {
                        throw new RuntimeException("Error finding StatTrak prices");
                    }
                } else {
                    priceBox = driver.findElement(By.cssSelector(".yyofkzfzpvevfrzbkbcwiwleal > tr:nth-child(1)"));
                    basicStat = priceBox.findElement(By.cssSelector("td:nth-child(1)")).getText();
                    if (!basicStat.equals("Basic")) {
                        throw new RuntimeException("Error finding Basic prices");
                    }
                }
                Map<String, Double> priceMap = new HashMap<>();
                Condition[] conditions = Condition.values();
                for (int i = 2; i < 6; i++) {
                    try{
                        double price = Double.parseDouble(priceBox.findElement(By.cssSelector("td:nth-child(" + i + ")")).getText().substring(2));

                        priceMap.put(conditions[i - 2].getText(), price);
                    }catch (Exception e){

                    }

                }
                priceMapList.add(priceMap);


            } catch (Exception e) {
                driver.quit();
                e.printStackTrace();
            }

        }
        driver.quit();
        priceMapList.addFirst(conditionSet);
        return priceMapList;
    }
}
