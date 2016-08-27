package tw.shounenwind.kmnbottool.app;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
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
    private JSONArray monsters;
    private ProgressDialog progressDialog;
    private Spinner attacter;
    private Spinner supporter;
    private Spinner team;
    private String[] monstersArray;
    private int oldTeam;

    private static String TAG = "kmnBot";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_screen);

        screenPrepare();
        SharedPreferences sharedPreferences = getSharedPreferences("data", MODE_PRIVATE);
        String user_id = sharedPreferences.getString("user_id", null);
        int user_id_ver = sharedPreferences.getInt("user_id_ver", 1);
        if(user_id != null && user_id_ver == 2)
            getBotData(user_id);
        else{
            showPlurkIdInput();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode != 0){
            return;
        }
        if(resultCode == RESULT_OK && data != null && data.getStringExtra("response") != null){
            try {
                final String id = Long.toString(new JSONObject(data.getStringExtra("response")).getLong("plurk_id"), 36);
                LinearLayout wv = (LinearLayout) findViewById(R.id.main_layout);
                Snackbar snackbar = Snackbar.make(wv, R.string.submit_success, Snackbar.LENGTH_LONG)
                        .setAction(R.string.open, new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.plurk.com/m/p/"+id));
                                startActivity(intent);
                            }
                        });
                TextView sbtv = (TextView) snackbar.getView().findViewById(android.support.design.R.id.snackbar_text);
                sbtv.setTextColor(Color.WHITE);
                snackbar.show();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    private void showPlurkIdInput() {
        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.plurk_name_dialog, (ViewGroup) findViewById(R.id.dialog));
        final EditText input = (EditText)layout.findViewById(R.id.name);
        new AlertDialog.Builder(this)
                .setTitle(R.string.input_plurk_username)
                .setView(layout)
                .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            SharedPreferences sharedPreferences = getSharedPreferences("data", MODE_PRIVATE);
                            sharedPreferences.edit().putInt("user_id_ver", 2).commit();
                            sharedPreferences.edit().putString("user_id", input.getText().toString()).commit();
                            getBotData(input.getText().toString());
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                })
                .show();
    }

    private void showProgressDialog(String text){
        progressDialog = ProgressDialog.show(this,
                getString(R.string.loading), text, true);
    }

    private void dismissProgressDialog(){
        try{
            progressDialog.dismiss();
        }catch (Exception e){
            e.printStackTrace();
        }
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
        Button bot_mons_box = (Button) findViewById(R.id.bot_mons_box);
        bot_mons_box.setOnClickListener(box_command);

        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    private View.OnClickListener bot_draw_command = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.command_draw)));
            startActivityForResult(intent, 0);
        }
    };

    private View.OnClickListener exprience_command = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Spinner spinner = (Spinner)findViewById(R.id.attacter);
            if(spinner.getSelectedItemPosition() == 0 || spinner.getSelectedItem() == null){
                Toast.makeText(MainActivity.this, R.string.no_selection, Toast.LENGTH_SHORT).show();
            }else {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.command_exp, spinner.getSelectedItem().toString())));
                startActivityForResult(intent, 0);
            }
            writeTeamInfo();
        }

    };

    private View.OnClickListener battle_command = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Spinner spinner = (Spinner)findViewById(R.id.attacter);
            if(spinner.getSelectedItemPosition() == 0 || spinner.getSelectedItem() == null){
                Toast.makeText(MainActivity.this, R.string.no_selection, Toast.LENGTH_SHORT).show();
            }else {
                String target = spinner.getSelectedItem().toString();
                Spinner support = (Spinner)findViewById(R.id.supporter);
                if(support.getSelectedItemPosition() != 0){
                    target += " "+support.getSelectedItem().toString();
                }
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.command_battle, target)));
                startActivityForResult(intent, 0);
            }
            writeTeamInfo();
        }
    };

    private View.OnClickListener hell_battle_command = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Spinner spinner = (Spinner)findViewById(R.id.attacter);
            if(spinner.getSelectedItemPosition() == 0 || spinner.getSelectedItem() == null){
                Toast.makeText(MainActivity.this, R.string.no_selection, Toast.LENGTH_SHORT).show();
            }else {
                String target = spinner.getSelectedItem().toString();
                Spinner support = (Spinner)findViewById(R.id.supporter);
                if(support.getSelectedItemPosition() != 0){
                    target += " "+support.getSelectedItem().toString();
                }
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.command_hell_battle, target)));
                startActivityForResult(intent, 0);
                writeTeamInfo();
            }

        }
    };

    private View.OnClickListener box_command = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(MainActivity.this, MonsDataActivity.class);
            intent.putExtra("monster", monsters.toString());
            intent.putExtra("player", player.toString());
            startActivity(intent);
        }
    };

    private void getBotData(final String plurk_id){
        showProgressDialog(getString(R.string.monster_loading));
        Log.d("kmn-bot", "id: "+plurk_id);
        Observable
                .create(new Observable.OnSubscribe<String>() {
                    @Override
                    public void call(Subscriber<? super String> subscriber) {
                        try {
                            URL url = new URL("http://www.kmnbot.ga/pets/" + plurk_id + ".json");
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
                            subscriber.onCompleted();
                        } catch (Exception e) {
                            subscriber.onError(e);
                        }
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
                        dismissProgressDialog();
                        Toast.makeText(MainActivity.this, R.string.load_monster_failed, Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onNext(String s) {
                        readBotData(s);
                        dismissProgressDialog();
                    }
                });
    }

    private void writeTeamInfo(){
        String defaultAttacter = "attacter";
        String defaultSupporter = "supporter";
        switch (oldTeam){
            case 0:
                defaultAttacter = "attacter";
                defaultSupporter = "supporter";
                break;
            case 1:
                defaultAttacter = "attacter1";
                defaultSupporter = "supporter1";
                break;
            case 2:
                defaultAttacter = "attacter2";
                defaultSupporter = "supporter2";
                break;
            case 3:
                defaultAttacter = "attacter3";
                defaultSupporter = "supporter3";
                break;
            case 4:
                defaultAttacter = "attacter4";
                defaultSupporter = "supporter4";
                break;
            case 5:
                defaultAttacter = "attacter5";
                defaultSupporter = "supporter5";
                break;
        }
        PreferenceManager
                .getDefaultSharedPreferences(MainActivity.this)
                .edit()
                .putString(defaultAttacter, ((Spinner) findViewById(R.id.attacter)).getSelectedItem().toString())
                .putString(defaultSupporter, ((Spinner) findViewById(R.id.supporter)).getSelectedItem().toString())
                .putInt("team", ((Spinner) findViewById(R.id.team)).getSelectedItemPosition())
                .commit();
    }

    private void readTeamInfo(){
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String defaultAttacter = getString(R.string.select_one);
        String defaultSupporter = getString(R.string.select_one);
        int defaultTeam = sharedPref.getInt("team", 0);
        oldTeam = defaultTeam;
        switch (defaultTeam){
            case 0:
                defaultAttacter = sharedPref.getString("attacter", getString(R.string.select_one));
                defaultSupporter = sharedPref.getString("supporter", getString(R.string.select_one));
                break;
            case 1:
                defaultAttacter = sharedPref.getString("attacter1", getString(R.string.select_one));
                defaultSupporter = sharedPref.getString("supporter1", getString(R.string.select_one));
                break;
            case 2:
                defaultAttacter = sharedPref.getString("attacter2", getString(R.string.select_one));
                defaultSupporter = sharedPref.getString("supporter2", getString(R.string.select_one));
                break;
            case 3:
                defaultAttacter = sharedPref.getString("attacter3", getString(R.string.select_one));
                defaultSupporter = sharedPref.getString("supporter3", getString(R.string.select_one));
                break;
            case 4:
                defaultAttacter = sharedPref.getString("attacter4", getString(R.string.select_one));
                defaultSupporter = sharedPref.getString("supporter4", getString(R.string.select_one));
                break;
            case 5:
                defaultAttacter = sharedPref.getString("attacter5", getString(R.string.select_one));
                defaultSupporter = sharedPref.getString("supporter5", getString(R.string.select_one));
                break;
        }
        int len = monstersArray.length;
        for (int i = 0; i < len; i++){
            if(defaultAttacter.equals(monstersArray[i])){
                attacter.setSelection(i);
            }
            if(defaultSupporter.equals(monstersArray[i])){
                supporter.setSelection(i);
            }
        }
        team.setSelection(defaultTeam);

    }

    private void readBotData(String data){
        try {
            player = (new JSONObject(data)).getJSONObject("玩家");
            monsters = (new JSONObject(data)).getJSONArray("寵物");
            int len = monsters.length();
            monstersArray = new String[len+1];
            monstersArray[0] = getString(R.string.select_one);
            for(int i = 0; i < len; i++){
                monstersArray[i+1] = monsters.getJSONObject(i).getString("寵物名稱");
            }
            attacter = (Spinner)findViewById(R.id.attacter);
            supporter = (Spinner)findViewById(R.id.supporter);
            ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(this, android.R.layout.simple_spinner_dropdown_item, monstersArray);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            supporter.setAdapter(adapter);
            attacter.setAdapter(adapter);
            String[] teamArray = new String[6];
            teamArray[0] = "1";
            teamArray[1] = "2";
            teamArray[2] = "3";
            teamArray[3] = "4";
            teamArray[4] = "5";
            teamArray[5] = "6";
            team = (Spinner)findViewById(R.id.team);
            ArrayAdapter<CharSequence> teamAdapter = new ArrayAdapter<CharSequence>(this, android.R.layout.simple_spinner_dropdown_item, teamArray);
            teamAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            team.setAdapter(teamAdapter);

            team.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    writeTeamInfo();
                    readTeamInfo();
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });

            readTeamInfo();

            findViewById(R.id.bot_draw).setEnabled(true);
            findViewById(R.id.attacter).setEnabled(true);
            findViewById(R.id.supporter).setEnabled(true);
            findViewById(R.id.bot_exp).setEnabled(true);
            findViewById(R.id.bot_battle).setEnabled(true);
            findViewById(R.id.bot_hell_battle).setEnabled(true);
            findViewById(R.id.bot_mons_box).setEnabled(true);

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, R.string.load_monster_failed, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onDestroy() {
        try {
            writeTeamInfo();
        }catch (Exception e){
            e.printStackTrace();
        }
        super.onDestroy();
    }
}
