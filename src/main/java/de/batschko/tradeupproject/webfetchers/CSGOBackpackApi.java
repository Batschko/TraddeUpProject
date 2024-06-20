package de.batschko.tradeupproject.webfetchers;

import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Class to fetch from CSGOBackpack api.
 */
@Slf4j
@Deprecated
public class CSGOBackpackApi {

    /**
     * Fetch skin price data.
     * <p>switch between 'percent20InsteadOfPlus' to get a higher api request limit</p>
     * @param fullItemName           the full skin name
     * @param time                   the api time parameter
     * @param percent20InsteadOfPlus use %20 instead of plus in url
     * @return json data from skin price api
     */
    public static String fetchSkinPriceData(String fullItemName, int time, boolean percent20InsteadOfPlus)
    {
        StringBuilder content = new StringBuilder();
        String encodedItemId = URLEncoder.encode(fullItemName, StandardCharsets.UTF_8);
        String urlString = "http://csgobackpack.net/api/GetItemPrice/?currency=" + "EUR" + "&id=" + encodedItemId + "&time=" + time + "&extend=1";
        if(percent20InsteadOfPlus){
            urlString = urlString.replace("+","%20");
        }
        log.debug("Fetching content from: {}", urlString);
        try
        {
            URL url = new URI(urlString).toURL();
            URLConnection urlConnection = url.openConnection();

            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
            String line;
            while ((line = bufferedReader.readLine()) != null)
            {
                content.append(line);
            }
            bufferedReader.close();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        return content.toString();
    }
}
