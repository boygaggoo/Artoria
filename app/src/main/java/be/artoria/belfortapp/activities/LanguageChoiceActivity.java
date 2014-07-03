package be.artoria.belfortapp.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.util.Locale;

import be.artoria.belfortapp.R;
import be.artoria.belfortapp.app.DataManager;
import be.artoria.belfortapp.app.PrefUtils;


public class LanguageChoiceActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_language_choice);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.language_choice, menu);
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

    public void langSelected(View view) {
        /* Setting the locale in response to a button click */
        final Locale locale;
        switch (view.getId()) {
            case(R.id.english):
                locale = new Locale("en");
                DataManager.lang = DataManager.Language.ENGLISH;
                break;
            case(R.id.french):
                locale = new Locale("fr");
                DataManager.lang = DataManager.Language.FRENCH;
                break;
            case(R.id.dutch):
            default:
                /* Default case is dutch */
                DataManager.lang = DataManager.Language.DUTCH;
                locale = new Locale("nl");
        }

        Locale.setDefault(locale);
        final Configuration config = new Configuration();
        config.locale = locale;
        view.getContext().getApplicationContext().getResources().updateConfiguration(config, null);
        /* Saving preferences*/
        PrefUtils.setNotFirstTime();

        /* Opening the Main screen */
        final Intent intent = new Intent(this,MainActivity.class);
        startActivity(intent);
    }
}
