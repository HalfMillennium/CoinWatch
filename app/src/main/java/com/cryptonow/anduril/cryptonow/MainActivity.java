package com.cryptonow.anduril.cryptonow;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.TypefaceSpan;
import android.util.Log;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.google.android.gms.ads.MobileAds;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    ArrayList<HashMap<String,String>> list = new ArrayList<HashMap<String,String>>();
    private SimpleAdapter sa;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MobileAds.initialize(this, "ca-app-pub-4748698902608744~4165370052");

        Toolbar toolbar = findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setLogo(R.drawable.ic_action_coin_watch_logo_app);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        getSupportActionBar().setTitle("");

        new JsonTask().execute("https://newsapi.org/v2/everything?q=bitcoin AND (cryptocurrency OR ethereum)&language=en&sortBy=publishedAt&pageSize=100&apiKey=7b9a5818d54749d4b91fcb7639bd3278");
    }

    private Date today() {
        final Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, 0);
        return cal.getTime();
    }

    private String getTodaysDateString() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
        return dateFormat.format(today());
    }

    private class JsonTask extends AsyncTask<String, String, String> {

        String txtJson = null;
        ArrayList<JSONObject> art = new ArrayList<>();
        HashMap<String,String> item;    //Used to link data to lines

        String[] titles, desc, names, dates, urls;

        protected void onPreExecute() {
            super.onPreExecute();
        }

        protected String doInBackground(String... params) {


            HttpURLConnection connection = null;
            BufferedReader reader = null;

            try {
                URL url = new URL(params[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();


                InputStream stream = connection.getInputStream();

                reader = new BufferedReader(new InputStreamReader(stream));

                StringBuffer buffer = new StringBuffer();
                String line = "";

                while ((line = reader.readLine()) != null) {
                    buffer.append(line+"\n");
                    Log.d("Response: ", "> " + line);   // full response

                }

                return buffer.toString();


            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
                try {
                    if (reader != null) {
                        reader.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            txtJson = result;

            Log.d("test result", txtJson);

            try {
                JSONObject txt = new JSONObject(txtJson);
                JSONArray articles = txt.getJSONArray("articles");

                int[] removal = new int[articles.length()];

                Log.d("test-tag", "" + articles.length());


                for(int i = 0; i < articles.length(); i++)
                {
                    JSONObject obj = articles.getJSONObject(i);
                    JSONObject s = obj.getJSONObject("source");

                    if(s.get("name").toString().equals("Python.org"))
                    {
                        removal[i] = 1;
                    }
                }

                for(int i = 0; i < removal.length; i++)
                {
                    if(removal[i] == 1)
                    {
                        articles.remove(i);
                    }
                }

                buildList(articles);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        public String formatDate(String raw)
        {
            String inputPattern = "yyyy-MM-dd";
            String outputPattern = "dd-MMM-yyyy";
            SimpleDateFormat inputFormat = new SimpleDateFormat(inputPattern);
            SimpleDateFormat outputFormat = new SimpleDateFormat(outputPattern);

            Date date = null;
            String str = null;

            try {
                date = inputFormat.parse(raw);
                str = outputFormat.format(date);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            return str;
        }


        public void buildList(JSONArray arr)
        {
            titles = new String[arr.length()];
            desc = new String[arr.length()];
            names = new String[arr.length()];
            dates = new String[arr.length()];
            urls = new String[arr.length()];



            for(int i = 0; i < arr.length(); i++)
            {
                JSONObject article = null;

                try {
                    article = arr.getJSONObject(i);

                    titles[i] = article.get("title").toString();
                    desc[i] = article.get("description").toString();

                    JSONObject source = article.getJSONObject("source");
                    names[i] = source.get("name").toString();

                    // get and parse date
                    dates[i] = formatDate(article.get("publishedAt").toString().substring(0, 10));

                    urls[i] = article.get("url").toString();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            //Load the data
            for(int i = 0; i < arr.length(); i++){
                item = new HashMap<String,String>();
                item.put("line1", titles[i]);
                item.put("line2", names[i]);
                item.put("line3", desc[i]);
                item.put("line4", dates[i]);
                list.add(item);
            }

            //Use an Adapter to link data to Views
            sa = new SimpleAdapter(MainActivity.this, list,
                    R.layout.fourlines,
                    new String[] { "line1","line2", "line3", "line4"},
                    new int[] {R.id.line_a, R.id.line_b, R.id.line_c, R.id.line_d});
            //Link the Adapter to the list

            ListView articleList = (ListView)findViewById(R.id.articleView);

            articleList.setAdapter(sa);

            articleList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    Uri uri = Uri.parse(urls[position]);
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    startActivity(intent);
                }
            });
        }


    }
}
