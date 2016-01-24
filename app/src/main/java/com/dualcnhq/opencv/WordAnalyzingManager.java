package com.dualcnhq.opencv;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

public class WordAnalyzingManager {

    public String[] process(String text) {
        try {
            String encodedText = URLEncoder.encode(text, "UTF-8");

            String url = "http://access.alchemyapi.com/calls/text/TextGetRankedTaxonomy?" +
                    "apikey=bd63c60dad3d8b6b4791fa2ddc4b5ab26a036c68&outputMode=json&text=(?)";
            url = url.replace("(?)", encodedText);

            // Connecting to alchemyUrl
            URL alchemyUrl = new URL(url);
            URLConnection urlConnection = alchemyUrl.openConnection();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));


            StringBuilder stringBuilder = new StringBuilder();
            boolean isValid = false;
            String jsonResult;
            while ((jsonResult = bufferedReader.readLine()) != null) {
                stringBuilder.append(jsonResult);
            }


            JSONObject obj = new JSONObject(stringBuilder.toString());
            JSONArray jsonArray = obj.getJSONArray("taxonomy");
            String[] strings = new String[jsonArray.length()];
            for (int i=0; i<jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String score = jsonObject.getString("score");
                String label = jsonObject.getString("label");
//                String confident = jsonObject.getString("confident");
                strings[i] = label + " - relevancy score: " + score;
            }

            bufferedReader.close();
            return strings;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }
}
