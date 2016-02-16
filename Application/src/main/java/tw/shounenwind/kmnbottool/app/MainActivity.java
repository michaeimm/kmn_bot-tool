package tw.shounenwind.kmnbottool.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;

import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import tw.shounenwind.kmnbottool.R;

public class MainActivity extends AppCompatActivity {

    private JSONObject player;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_screen);

        screenPrepare();
        //getBotData("8433188");
        getBotData("6820974");
    }

    private void screenPrepare(){
        Button bot_draw = (Button) findViewById(R.id.bot_draw);
        bot_draw.setOnClickListener(bot_draw_command);
        Button exp_draw = (Button) findViewById(R.id.bot_exp);
        exp_draw.setOnClickListener(exprience_command);
        Button bot_battle = (Button) findViewById(R.id.bot_battle);
        bot_battle.setOnClickListener(battle_command);
        Button bot_hell_battle = (Button) findViewById(R.id.bot_hell_battle);
        bot_hell_battle.setOnClickListener(hell_battle_command);

        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    private View.OnClickListener bot_draw_command = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.command_draw)));
            startActivity(intent);
        }
    };

    private View.OnClickListener exprience_command = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Spinner spinner = (Spinner)findViewById(R.id.attacter);
            if(spinner.getSelectedItemPosition() == 0 || spinner.getSelectedItem() == null){
                Toast.makeText(MainActivity.this, "未選擇對象", Toast.LENGTH_SHORT).show();
            }else {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.command_exp, spinner.getSelectedItem().toString())));
                startActivity(intent);
            }
            PreferenceManager
                    .getDefaultSharedPreferences(MainActivity.this)
                    .edit()
                    .putString("attacter", ((Spinner) findViewById(R.id.attacter)).getSelectedItem().toString())
                    .commit();
        }

    };

    private View.OnClickListener battle_command = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Spinner spinner = (Spinner)findViewById(R.id.attacter);
            if(spinner.getSelectedItemPosition() == 0 || spinner.getSelectedItem() == null){
                Toast.makeText(MainActivity.this, "未選擇對象", Toast.LENGTH_SHORT).show();
            }else {
                String target = spinner.getSelectedItem().toString();
                Spinner support = (Spinner)findViewById(R.id.supporter);
                if(support.getSelectedItemPosition() != 0){
                    target += " "+support.getSelectedItem().toString();
                }
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.command_battle, target)));
                startActivity(intent);
            }
            PreferenceManager
                    .getDefaultSharedPreferences(MainActivity.this)
                    .edit()
                    .putString("attacter", ((Spinner) findViewById(R.id.attacter)).getSelectedItem().toString())
                    .putString("supporter", ((Spinner) findViewById(R.id.supporter)).getSelectedItem().toString())
                    .commit();
        }
    };

    private View.OnClickListener hell_battle_command = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Spinner spinner = (Spinner)findViewById(R.id.attacter);
            if(spinner.getSelectedItemPosition() == 0 || spinner.getSelectedItem() == null){
                Toast.makeText(MainActivity.this, "未選擇對象", Toast.LENGTH_SHORT).show();
            }else {
                String target = spinner.getSelectedItem().toString();
                Spinner support = (Spinner)findViewById(R.id.supporter);
                if(support.getSelectedItemPosition() != 0){
                    target += " "+support.getSelectedItem().toString();
                }
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.command_hell_battle, target)));
                startActivity(intent);
            }
            PreferenceManager
                    .getDefaultSharedPreferences(MainActivity.this)
                    .edit()
                    .putString("attacter", ((Spinner) findViewById(R.id.attacter)).getSelectedItem().toString())
                    .putString("supporter", ((Spinner) findViewById(R.id.supporter)).getSelectedItem().toString())
                    .commit();
        }
    };

    private void getBotData(final String plurk_id){
        Observable
                .create(new Observable.OnSubscribe<String>() {
                    @Override
                    public void call(Subscriber<? super String> subscriber) {
                        try {
                            URL url = new URL("http://133.130.102.121:9163/petInfos/"+plurk_id+".json");
                            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection(Proxy.NO_PROXY);
                            httpURLConnection.setUseCaches(false);
                            httpURLConnection.setRequestMethod("GET");
                            httpURLConnection.connect();
                            BufferedReader bufferedReader;
                            if (httpURLConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                                bufferedReader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream(), "UTF-8"));
                            } else {
                                bufferedReader = new BufferedReader(new InputStreamReader(httpURLConnection.getErrorStream(), "UTF-8"));
                            }

                            String line;
                            StringBuilder stringBuilder = new StringBuilder();

                            while ((line = bufferedReader.readLine()) != null) {
                                stringBuilder.append(line);
                                stringBuilder.append("\n");
                            }
                            bufferedReader.close();
                            subscriber.onNext(stringBuilder.toString());
                        } catch (Exception e) {
                            subscriber.onError(e);
                        }
                        subscriber.onCompleted();
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<String>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onNext(String s) {
                        readBotData(s);
                    }
                });


    }

    private void readBotData(String data){
        try {
            player = (new JSONObject(data)).getJSONObject("玩家");
            JSONArray mons = (new JSONObject(data)).getJSONArray("寵物");
            //Log.d("player", player.toString());
            //Log.d("mons", mons.toString());
            int len = mons.length();
            String[] monstersArray = new String[len+1];
            monstersArray[0] = "請選擇";
            for(int i = 0; i < len; i++){
                monstersArray[i+1] = mons.getJSONObject(i).getString("寵物名稱");
            }
            Spinner attacter = (Spinner)findViewById(R.id.attacter);
            Spinner supporter = (Spinner)findViewById(R.id.supporter);
            ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(this, android.R.layout.simple_spinner_dropdown_item, monstersArray);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            supporter.setAdapter(adapter);
            attacter.setAdapter(adapter);
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
            String defaultAttacter = sharedPref.getString("attacter", "請選擇");
            String defaultSupporter = sharedPref.getString("supporter", "請選擇");
            for (int i = 0; i < len; i++){
                if(defaultAttacter.equals(monstersArray[i])){
                    attacter.setSelection(i);
                }
                if(defaultSupporter.equals(monstersArray[i])){
                    supporter.setSelection(i);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    // BEGIN_INCLUDE(create_menu)
    /**
     * Use this method to instantiate your menu, and add your items to it. You
     * should return true if you have added items to it and want the menu to be displayed.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate our menu from the resources by using the menu inflater.
        getMenuInflater().inflate(R.menu.main, menu);

        // It is also possible add items here. Use a generated id from
        // resources (ids.xml) to ensure that all menu ids are distinct.

        return true;
    }
    // END_INCLUDE(create_menu)

    // BEGIN_INCLUDE(menu_item_selected)
    /**
     * This method is called when one of the menu items to selected. These items
     * can be on the Action Bar, the overflow menu, or the standard options menu. You
     * should return true if you handle the selection.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            //TODO: menu item
            case R.id.menu_profile:
                String s;
                if(player == null){
                    s = "無資料";
                }else{
                    try {
                        s = player.getString("蒐集完成度");
                    } catch (JSONException e) {
                        e.printStackTrace();
                        s = "無資料";
                    }
                }

                AlertDialog alertDialog = new AlertDialog.Builder(this)
                        .setMessage("蒐集完成度: " + s)
                        .setPositiveButton("確認", null)
                        .create();
                alertDialog.show();
                break;
        }

        return super.onOptionsItemSelected(item);
    }
    // END_INCLUDE(menu_item_selected)

}
