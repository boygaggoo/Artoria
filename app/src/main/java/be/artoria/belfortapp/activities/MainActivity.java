package be.artoria.belfortapp.activities;


import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.List;

import be.artoria.belfortapp.R;
import be.artoria.belfortapp.app.DataManager;
import be.artoria.belfortapp.app.POI;
import be.artoria.belfortapp.app.PrefUtils;
import be.artoria.belfortapp.sql.POIDAO;

public class MainActivity extends BaseActivity {
    ArrayAdapter<String> menuAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initGui();
        downloadData();
    }
    private static boolean downloading = false;

    private void downloadData() {
        final long lastDownload = PrefUtils.getTimeStampDownloads();
        final long timeSinceLastDownload = System.currentTimeMillis() - lastDownload;
        /* Either there is no last download ( case == 0)
        *  or it is older than 12 hours, which is 43200000 milliseconds according to google */
        System.out.println(downloading);
        System.out.println(timeSinceLastDownload);
          if((lastDownload == 0 || timeSinceLastDownload > 1000) && !downloading){
          //if((lastDownload == 0 || timeSinceLastDownload > 43200000) && !downloading){
            downloading = true;
            System.err.println("Downloading!");
            new DownloadDataTask().execute("https://raw.githubusercontent.com/oSoc14/ArtoriaData/master/poi.json");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        downloadData();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /*initialize the GUI content and clickhandlers*/
    private void initGui(){
        final ListView lstMenu = (ListView)findViewById(R.id.lstMenu);
        final Button btnSettings = (Button)findViewById(R.id.btnSettings);
        final Button btnAbout = (Button)findViewById(R.id.btnAbout);
        final Button btnRoute = (Button)findViewById(R.id.btnRoute);


        menuAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,getResources().getStringArray(R.array.lstMenu));
        lstMenu.setAdapter(menuAdapter);

        lstMenu.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                /* The second item are the buildings */
                if (i == 1) {
                    final Intent intent = new Intent(MainActivity.this, MonumentDetailActivity.class);
                    intent.putExtra("id", 1);
                    startActivity(intent);
                }
            }
        });

        btnSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*Go to settings*/
                final Intent i = new Intent(MainActivity.this, LanguageChoiceActivity.class);
                startActivity(i);
            }
        });

        btnAbout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*Go to the Artoria website*/
                final Uri webpage = Uri.parse(getResources().getString(R.string.artoria_url));
                final Intent webIntent = new Intent(Intent.ACTION_VIEW, webpage);
                startActivity(webIntent);
            }
        });

        btnRoute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*Go to the route overview*/
                final Intent i = new Intent(MainActivity.this,RouteActivity.class);
                startActivity(i);
            }
        });
    }

    private class DownloadDataTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            String response = "";
            for (String url : urls) {
                final DefaultHttpClient client = new DefaultHttpClient();
                final HttpGet httpGet = new HttpGet(url);
                try {
                    final HttpResponse execute = client.execute(httpGet);
                    final InputStream content = execute.getEntity().getContent();

                    final BufferedReader buffer = new BufferedReader(new InputStreamReader(content));
                    String s = "";
                    while ((s = buffer.readLine()) != null) {
                        response += s;
                    }

                } catch (final Exception e) {
                    e.printStackTrace();
                }
            }
            return response;
        }

        @Override
        protected void onPostExecute(String result) {
            final Gson gson = new Gson();
            final List<POI> list = gson.fromJson(result, new TypeToken<List<POI>>(){}.getType());
            downloading = false;
            if(list.isEmpty()){
                System.err.println("not good.");
            }
            else {
                PrefUtils.saveTimeStampDownloads();
                DataManager.clearpois();
                try {
                    DataManager.poidao.open();
                    DataManager.poidao.clearTable();

                for(POI poi : list){
                    System.out.println(poi.ENG_description);
                    System.out.println(poi.id);

                    DataManager.poidao.savePOI(poi);

                }
                DataManager.addAll(list);
                DataManager.poidao.close();
                } catch (SQLException e) {
                    //TODO sane error handling
                }
            }
        }
    }
}
