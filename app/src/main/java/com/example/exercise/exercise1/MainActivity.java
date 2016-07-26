package com.example.exercise.exercise1;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.webkit.WebViewClient;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    WebView webView;

    private class MyWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url)
        {
            view.loadUrl(url);
            return true;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        webView = (WebView)findViewById(R.id.webView);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new MyWebViewClient());
        loadMap();
    }

    public void loadMap() {
        webView.loadUrl("file:///android_asset/maphtml.html");
}

    public void buttonLoadClick(View view) {
        //Получение данных.
        JSONArray unprocessedPoints = getPointsFromURL("example.com/route.txt");
        //"https://dl.dropboxusercontent.com/u/5842089/route.txt"
        //Расстановка точек на карте.
        JSONObject point = null;
        //Координаты, которые определят границы отображения карты. Инициализированы невозможными для координат значениеми.
        double maxLat = -222, minLat = 222, maxLon = -222, minLon = 222;
        if(unprocessedPoints.length() <2){
            Toast message = Toast.makeText(getApplicationContext(),
                    "Слишком мало точек для построения линии", Toast.LENGTH_LONG);
            message.show();
        }
        else {
            webView.loadUrl("javascript:var mapPointsArray = [];");
            try {
                point = unprocessedPoints.getJSONObject(0);
                maxLat = minLat = point.getDouble("la");
                maxLon = minLon = point.getDouble("lo");
                for (int i = 1; i < unprocessedPoints.length(); ++i) {
                    point = unprocessedPoints.getJSONObject(i);
                    double lat = point.getDouble("la");
                    double lon = point.getDouble("lo");
                    webView.loadUrl("javascript:mapPointsArray.push(new google.maps.LatLng(" +
                            String.valueOf(lat) + "," + String.valueOf(lon) + ");");
                    if(lat > maxLat)
                        maxLat = lat;
                    else if(lat < minLat)
                        minLat = lat;
                    if(lon > maxLon)
                        maxLon = lon;
                    else if(lon < minLon)
                        minLon = lon;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            webView.loadUrl("javascript:" +
                    "var pathLine=new google.maps.Polyline({\n" +
                    "path:mapPointsArray,\n" +
                    "strokeColor:\"#3B6EA5\",\n" +
                    "strokeOpacity:0.8,\n" +
                    "strokeWeight:2\n" +
                    "});");
            webView.loadUrl("javascript:pathLine.setMap(map);");
            webView.loadUrl("javascript:flightpath.setMap(map);");
        }
    }
    public JSONArray getPointsFromURL(String URL)
    {
        //TODO Взять данные из дропбокса.
        String unparsed = getString(R.string.pointsArray);

        JSONObject unprocessedPointsWrapped = null; //Объект coords, содержащий массив координат.
        JSONArray unprocessedPointsUnwrapped = null; //Массив координат сам по себе.
        try {
            unprocessedPointsWrapped = new JSONObject(unparsed);
            unprocessedPointsUnwrapped = unprocessedPointsWrapped.getJSONArray("coords");
            return unprocessedPointsUnwrapped;
        }
        catch (JSONException parseException) {
            Toast exceptionInfo = Toast.makeText(getApplicationContext(),
                    parseException.getLocalizedMessage(), Toast.LENGTH_LONG);
            exceptionInfo.show();
            return null;
        }
    }
}
