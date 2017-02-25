package tw.shounenwind.kmnbottool.app;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.Proxy;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import okhttp3.CacheControl;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import tw.shounenwind.kmnbottool.R;

public class MainActivity extends AppCompatActivity {

    private static String TAG = "kmnBot";
    private ProgressDialog progressDialog;
    private Spinner attacker;
    private Spinner supporter;
    private Spinner team;
    private String[] monstersArray;
    private int oldTeam;
    private MonsterDataManager monsterDataManager;
    private String[] chipValues;
    private Spinner chipsSpinner;

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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menuitem_license:{
                Intent intent = new Intent();
                intent.setClass(this, LicenseActivity.class);
                startActivity(intent);
                return true;
            }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
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
                            sharedPreferences.edit()
                                    .putInt("user_id_ver", 2)
                                    .putString("user_id", input.getText().toString())
                                    .commit();
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

    private void screenPrepare() {
        Button bot_draw = (Button) findViewById(R.id.bot_draw);
        bot_draw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.command_draw)));
                startActivity(intent);
            }
        });
        Button exp_draw = (Button) findViewById(R.id.bot_exp);
        exp_draw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Spinner spinner = (Spinner) findViewById(R.id.attacter);
                if (spinner.getSelectedItemPosition() == 0 || spinner.getSelectedItem() == null) {
                    Toast.makeText(MainActivity.this, R.string.no_selection, Toast.LENGTH_SHORT).show();
                } else {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.command_exp, spinner.getSelectedItem().toString())));
                    startActivity(intent);
                }
                writeTeamInfo();
            }

        });
        Button bot_battle = (Button) findViewById(R.id.bot_battle);
        bot_battle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Spinner spinner = (Spinner) findViewById(R.id.attacter);
                if (spinner.getSelectedItemPosition() == 0 || spinner.getSelectedItem() == null) {
                    Toast.makeText(MainActivity.this, R.string.no_selection, Toast.LENGTH_SHORT).show();
                } else {
                    String target = spinner.getSelectedItem().toString();
                    Spinner support = (Spinner) findViewById(R.id.supporter);
                    if (support.getSelectedItemPosition() != 0) {
                        target += " " + support.getSelectedItem().toString();
                    }
                    if (chipsSpinner != null && chipsSpinner.getSelectedItemPosition() != 0){
                        target += "\n" + chipValues[chipsSpinner.getSelectedItemPosition()];
                    }
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.command_battle, target)));
                    startActivity(intent);
                }
                writeTeamInfo();
            }
        });
        Button bot_hell_battle = (Button) findViewById(R.id.bot_hell_battle);
        bot_hell_battle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Spinner spinner = (Spinner) findViewById(R.id.attacter);
                if (spinner.getSelectedItemPosition() == 0 || spinner.getSelectedItem() == null) {
                    Toast.makeText(MainActivity.this, R.string.no_selection, Toast.LENGTH_SHORT).show();
                } else {
                    String target = spinner.getSelectedItem().toString();
                    Spinner support = (Spinner) findViewById(R.id.supporter);
                    if (support.getSelectedItemPosition() != 0) {
                        target += " " + support.getSelectedItem().toString();
                    }
                    if (chipsSpinner != null && chipsSpinner.getSelectedItemPosition() != 0){
                        target += "\n" + chipValues[chipsSpinner.getSelectedItemPosition()];
                    }
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.command_hell_battle, target)));
                    startActivityForResult(intent, 0);
                    writeTeamInfo();
                }

            }
        });
        Button bot_mons_box = (Button) findViewById(R.id.bot_mons_box);
        bot_mons_box.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, MonsDataActivity.class);
                startActivity(intent);
            }
        });
        CardView team_card = (CardView) findViewById(R.id.team_card);
        CardView attacker_card = (CardView) findViewById(R.id.attacter_card);
        CardView supporter_card = (CardView) findViewById(R.id.supporter_card);

        team = (Spinner) findViewById(R.id.team);
        attacker = (Spinner) findViewById(R.id.attacter);
        supporter = (Spinner) findViewById(R.id.supporter);

        team_card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                team.performClick();
            }
        });
        attacker_card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attacker.performClick();
            }
        });
        supporter_card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                supporter.performClick();
            }
        });

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    private void getBotData(final String plurk_id){
        showProgressDialog(getString(R.string.monster_loading));
        Log.d(TAG, "id: "+plurk_id);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    OkHttpClient okHttpClient = new OkHttpClient.Builder()
                            .protocols(Arrays.asList(Protocol.HTTP_2, Protocol.HTTP_1_1))
                            .proxy(Proxy.NO_PROXY)
                            .connectTimeout(15, TimeUnit.SECONDS)
                            .readTimeout(20, TimeUnit.SECONDS)
                            .writeTimeout(20, TimeUnit.SECONDS)
                            .build();
                    Request request = new Request.Builder()
                            .cacheControl(
                                    new CacheControl.Builder()
                                            .noCache()
                                            .build()
                            ).url("http://www.kmnbot.ga/pets/" + plurk_id + ".json")
                            .build();
                    Response response = okHttpClient.newCall(request).execute();
                    final String result = response.body().string();
                    response.close();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            readBotData(result);
                            dismissProgressDialog();
                            getChips(plurk_id);
                        }
                    });

                } catch (Exception e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            dismissProgressDialog();
                            Toast.makeText(MainActivity.this, R.string.load_monster_failed, Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        }).start();
    }

    private void getChips(final String plurk_id) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    OkHttpClient okHttpClient = new OkHttpClient.Builder()
                            .protocols(Arrays.asList(Protocol.HTTP_2, Protocol.HTTP_1_1))
                            .proxy(Proxy.NO_PROXY)
                            .connectTimeout(15, TimeUnit.SECONDS)
                            .readTimeout(20, TimeUnit.SECONDS)
                            .writeTimeout(20, TimeUnit.SECONDS)
                            .build();
                    Request request = new Request.Builder()
                            .cacheControl(
                                    new CacheControl.Builder()
                                            .noCache()
                                            .build()
                            ).url("http://www.kmnbot.ga/chips/" + plurk_id + ".json")
                            .build();
                    Response response = okHttpClient.newCall(request).execute();
                    final String result = response.body().string();
                    response.close();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            readChips(result);
                        }
                    });

                } catch (Exception e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            dismissProgressDialog();
                            Toast.makeText(MainActivity.this, R.string.load_monster_failed, Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        }).start();
    }

    private void readChips(String data){
        try {
            JSONArray jsonArray = new JSONArray(new JSONObject(data).getString("晶片"));
            int len = jsonArray.length();
            if(len == 0){
                throw new Exception("No chips exist.");
            }
            String[] chipNames = new String[len + 1];
            chipValues = new String[len + 1];
            chipNames[0] = "請選擇";
            chipValues[0] = "";
            for(int i = 0; i < len ; i++){
                chipNames[i+1] = jsonArray.getJSONObject(i).getString("名稱") + "(" + jsonArray.getJSONObject(i).getString("組件") + ")";
                chipValues[i+1] = jsonArray.getJSONObject(i).getString("名稱");
            }
            ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(this, android.R.layout.simple_spinner_dropdown_item, chipNames);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            chipsSpinner = (Spinner) findViewById(R.id.chips);
            chipsSpinner.setAdapter(adapter);
            findViewById(R.id.chips_card).setVisibility(View.VISIBLE);
        }catch (Exception e){
            e.printStackTrace();
            String[] chipNames = new String[]{"請選擇"};
            chipValues = new String[]{""};
            ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(this, android.R.layout.simple_spinner_dropdown_item, chipNames);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            chipsSpinner = (Spinner) findViewById(R.id.chips);
            chipsSpinner.setAdapter(adapter);
        }

    }

    private void writeTeamInfo(){
        String defaultAttacker = "attacker";
        String defaultSupporter = "supporter";
        switch (oldTeam){
            case 0:
                defaultAttacker = "attacker";
                defaultSupporter = "supporter";
                break;
            case 1:
                defaultAttacker = "attacter1";
                defaultSupporter = "supporter1";
                break;
            case 2:
                defaultAttacker = "attacter2";
                defaultSupporter = "supporter2";
                break;
            case 3:
                defaultAttacker = "attacter3";
                defaultSupporter = "supporter3";
                break;
            case 4:
                defaultAttacker = "attacter4";
                defaultSupporter = "supporter4";
                break;
            case 5:
                defaultAttacker = "attacter5";
                defaultSupporter = "supporter5";
                break;
        }
        PreferenceManager
                .getDefaultSharedPreferences(MainActivity.this)
                .edit()
                .putString(defaultAttacker, ((Spinner) findViewById(R.id.attacter)).getSelectedItem().toString())
                .putString(defaultSupporter, ((Spinner) findViewById(R.id.supporter)).getSelectedItem().toString())
                .putInt("team", ((Spinner) findViewById(R.id.team)).getSelectedItemPosition())
                .commit();
    }

    private void readTeamInfo(){
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String defaultAttacker = getString(R.string.select_one);
        String defaultSupporter = getString(R.string.select_one);
        int defaultTeam = sharedPref.getInt("team", 0);
        oldTeam = defaultTeam;
        switch (defaultTeam){
            case 0:
                defaultAttacker = sharedPref.getString("attacker", getString(R.string.select_one));
                defaultSupporter = sharedPref.getString("supporter", getString(R.string.select_one));
                break;
            case 1:
                defaultAttacker = sharedPref.getString("attacter1", getString(R.string.select_one));
                defaultSupporter = sharedPref.getString("supporter1", getString(R.string.select_one));
                break;
            case 2:
                defaultAttacker = sharedPref.getString("attacter2", getString(R.string.select_one));
                defaultSupporter = sharedPref.getString("supporter2", getString(R.string.select_one));
                break;
            case 3:
                defaultAttacker = sharedPref.getString("attacter3", getString(R.string.select_one));
                defaultSupporter = sharedPref.getString("supporter3", getString(R.string.select_one));
                break;
            case 4:
                defaultAttacker = sharedPref.getString("attacter4", getString(R.string.select_one));
                defaultSupporter = sharedPref.getString("supporter4", getString(R.string.select_one));
                break;
            case 5:
                defaultAttacker = sharedPref.getString("attacter5", getString(R.string.select_one));
                defaultSupporter = sharedPref.getString("supporter5", getString(R.string.select_one));
                break;
        }
        int len = monstersArray.length;
        for (int i = 0; i < len; i++){
            if(defaultAttacker.equals(monstersArray[i])){
                attacker.setSelection(i);
            }
            if(defaultSupporter.equals(monstersArray[i])){
                supporter.setSelection(i);
            }
        }
        team.setSelection(defaultTeam);

    }

    private void readBotData(String data){
        try {
            monsterDataManager = MonsterDataManager.getInstance();
            monsterDataManager.parse(data);
            JSONArray monsters = monsterDataManager.getMonsters();
            int len = monsters.length();
            monstersArray = new String[len+1];
            monstersArray[0] = getString(R.string.select_one);
            for(int i = 0; i < len; i++){
                monstersArray[i+1] = monsters.getJSONObject(i).getString("寵物名稱");
            }
            ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(this, android.R.layout.simple_spinner_dropdown_item, monstersArray);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            supporter.setAdapter(adapter);
            attacker.setAdapter(adapter);
            String[] teamArray = new String[6];
            teamArray[0] = "1";
            teamArray[1] = "2";
            teamArray[2] = "3";
            teamArray[3] = "4";
            teamArray[4] = "5";
            teamArray[5] = "6";
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
