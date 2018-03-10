package tw.shounenwind.kmnbottool.app;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.annotation.UiThread;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.common.base.Preconditions;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import okhttp3.CacheControl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import tw.shounenwind.kmnbottool.R;

public class MainActivity extends AppCompatActivity {

    private static String TAG = "kmnBot";
    private static final int LASTEST_ID_VER = 2;
    private ProgressDialog progressDialog;
    private Spinner attacker;
    private Spinner supporter;
    private Spinner team;
    private String[] monstersArray;
    private int oldTeam;
    private String[] chipValues;
    private Spinner chipsSpinner;
    private OkHttpClient okHttpClient;
    private FlowJob flowJob;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_screen);
        screenPrepare();
        okHttpClient = LinkUtil.getLink();
        SharedPreferences sharedPreferences = getSharedPreferences("data", MODE_PRIVATE);
        String userId = sharedPreferences.getString("user_id", null);
        int userIdVer = sharedPreferences.getInt("user_id_ver", 1);
        if (userId == null || userIdVer != LASTEST_ID_VER) {
            showPlurkIdInput();
        } else {
            flowJob = new FlowJob(this)
                    .addUIJob(() -> showProgressDialog(getString(R.string.monster_loading)))
                    .addIOJob(() -> getBotData(userId))
                    .addUIJob(this::readBotData)
                    .addUIJob(this::dismissProgressDialog)
                    .addIOJob(() -> getChips(userId));
            flowJob.start();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @SuppressLint("ApplySharedPref")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menuitem_license:{
                Intent intent = new Intent();
                intent.setClass(this, LicenseActivity.class);
                startActivity(intent);
                return true;
            }
            case R.id.bot_link:{
                Intent intent = new Intent();
                intent.setData(Uri.parse("https://www.plurk.com/KMN_BOT"));
                intent.setAction(Intent.ACTION_VIEW);
                startActivity(intent);
            }
            case R.id.logout:{
                SharedPreferences sharedPreferences = getSharedPreferences("data", MODE_PRIVATE);
                sharedPreferences.edit()
                        .putInt("user_id_ver", 1)
                        .commit();
                showPlurkIdInput();
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
        View layout = inflater.inflate(R.layout.plurk_name_dialog, findViewById(R.id.dialog));
        final EditText input = layout.findViewById(R.id.name);

        final AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle(R.string.input_plurk_username)
                .setView(layout)
                .setPositiveButton(R.string.confirm, (dialog, which) -> receivePlurkId(input.getText().toString()))
                .show();
        input.setOnEditorActionListener((textView, actionId, keyEvent) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH
                    || actionId == EditorInfo.IME_ACTION_DONE
                    || keyEvent.getAction() == KeyEvent.ACTION_DOWN
                    && keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                receivePlurkId(input.getText().toString());
                alertDialog.dismiss();
                return true;
            }
            return false;
        });
    }

    @SuppressLint("ApplySharedPref")
    private void receivePlurkId(String id){
        try {
            SharedPreferences sharedPreferences = getSharedPreferences("data", MODE_PRIVATE);
            sharedPreferences.edit()
                    .putInt("user_id_ver", LASTEST_ID_VER)
                    .putString("user_id", id)
                    .commit();
            flowJob = new FlowJob(this)
                    .addUIJob(() -> showProgressDialog(getString(R.string.monster_loading)))
                    .addIOJob(() -> getBotData(id))
                    .addUIJob(this::readBotData)
                    .addUIJob(this::dismissProgressDialog)
                    .addIOJob(() -> getChips(id));
            flowJob.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showProgressDialog(String text){
        progressDialog = ProgressDialog.show(this,
                getString(R.string.loading), text, true);
    }

    private void dismissProgressDialog(){
        try{
            progressDialog.dismiss();
        }catch (Exception e){
            flowJob.stop();
            e.printStackTrace();
        }
    }

    private void screenPrepare() {
        Button bot_draw = findViewById(R.id.bot_draw);
        bot_draw.setOnClickListener(v -> {
            String options[] = new String[]{
                    getString(R.string.bot_draw_normal),
                    getString(R.string.bot_draw_ultra)
            };
            new AlertDialog.Builder(MainActivity.this)
                    .setItems(options, (dialogInterface, i) -> {
                        switch (i) {
                            case 0:
                                sendCommand(getString(R.string.command_draw));
                                break;
                            case 1:
                                sendCommand(getString(R.string.command_draw_ultra));
                                break;
                        }
                    })
                    .show();
        });
        Button exp_draw = findViewById(R.id.bot_exp);
        exp_draw.setOnClickListener(v -> {
            if (attacker.getSelectedItemPosition() == 0 || attacker.getSelectedItem() == null) {
                Toast.makeText(MainActivity.this, R.string.no_selection, Toast.LENGTH_SHORT).show();
            } else {
                String options[] = new String[]{
                        getString(R.string.bot_exp_normal),
                        getString(R.string.bot_exp_ultra)
                };
                new AlertDialog.Builder(MainActivity.this)
                        .setItems(options, (dialogInterface, i) -> {
                            switch (i) {
                                case 0:
                                    sendCommand(getString(R.string.command_exp, attacker.getSelectedItem().toString()));
                                    break;
                                case 1:
                                    sendCommand(getString(R.string.command_exp_ultra, attacker.getSelectedItem().toString()));
                                    break;
                            }
                        })
                        .show();
            }
            writeTeamInfo();
        });
        Button bot_battle = findViewById(R.id.bot_battle);
        bot_battle.setOnClickListener(v -> {
            if (attacker.getSelectedItemPosition() == 0 || attacker.getSelectedItem() == null) {
                Toast.makeText(MainActivity.this, R.string.no_selection, Toast.LENGTH_SHORT).show();
            } else {
                String target = attacker.getSelectedItem().toString();

                if (supporter.getSelectedItemPosition() != 0) {
                    target += " " + supporter.getSelectedItem().toString();
                }
                if (chipsSpinner != null && chipsSpinner.getSelectedItemPosition() != 0) {
                    target += "\n" + chipValues[chipsSpinner.getSelectedItemPosition()];
                }
                String options[] = new String[]{
                        getString(R.string.bot_battle_normal),
                        getString(R.string.bot_battle_hell),
                        getString(R.string.bot_battle_ultra_hell)
                };
                final String finalTarget = target;
                new AlertDialog.Builder(MainActivity.this)
                        .setItems(options, (dialogInterface, i) -> {
                            switch (i) {
                                case 0:
                                    sendCommand(getString(R.string.command_battle, finalTarget));
                                    break;
                                case 1:
                                    sendCommand(getString(R.string.command_hell_battle, finalTarget));
                                    break;
                                case 2:
                                    sendCommand(getString(R.string.command_ultra_hell_battle, finalTarget));
                                    break;
                            }
                        })
                        .show();

            }
            writeTeamInfo();
        });
        Button bot_mons_box = findViewById(R.id.bot_mons_box);
        bot_mons_box.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, MonsDataActivity.class);
            startActivity(intent);
        });
        CardView team_card = findViewById(R.id.team_card);
        CardView attacker_card = findViewById(R.id.attacter_card);
        CardView supporter_card = findViewById(R.id.supporter_card);

        team = findViewById(R.id.team);
        attacker = findViewById(R.id.attacter);
        supporter = findViewById(R.id.supporter);

        team_card.setOnClickListener(view -> team.performClick());
        attacker_card.setOnClickListener(view -> attacker.performClick());
        supporter_card.setOnClickListener(view -> supporter.performClick());

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    @UiThread
    private void getBotData(final String plurk_id){
        ResponseBody body = null;
        try {
            Request request = new Request.Builder()
                    .cacheControl(
                            new CacheControl.Builder()
                                    .noCache()
                                    .build()
                    ).url("http://www.kmnbot.ga/pets/" + plurk_id + ".json")
                    .build();
            Response response = okHttpClient.newCall(request).execute();
            body = Preconditions.checkNotNull(response.body());
            final String result = body.string();
            MonsterDataManager monsterDataManager = MonsterDataManager.getInstance();
            monsterDataManager.parse(result);

        } catch (Exception e) {
            flowJob.stop();
            e.printStackTrace();
            runOnUiThread(() -> {
                dismissProgressDialog();
                Toast.makeText(MainActivity.this, R.string.load_monster_failed, Toast.LENGTH_LONG).show();
            });
        } finally {
            if (body != null)
                body.close();
        }
    }

    @UiThread
    private void getChips(final String plurk_id) {
        ResponseBody body = null;
        try {
            Request request = new Request.Builder()
                    .cacheControl(
                            new CacheControl.Builder()
                                    .noCache()
                                    .build()
                    ).url("http://www.kmnbot.ga/chips/" + plurk_id + ".json")
                    .build();
            Response response = okHttpClient.newCall(request).execute();
            body = Preconditions.checkNotNull(response.body());
            final String result = body.string();
            runOnUiThread(() -> readChips(result));

        } catch (Exception e) {
            flowJob.stop();
            e.printStackTrace();
            runOnUiThread(() -> {
                dismissProgressDialog();
                Toast.makeText(MainActivity.this, R.string.load_monster_failed, Toast.LENGTH_LONG).show();
            });
        } finally {
            if (body != null)
                body.close();
        }
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
            ArrayAdapter<CharSequence> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, chipNames);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            chipsSpinner = findViewById(R.id.chips);
            chipsSpinner.setAdapter(adapter);
            findViewById(R.id.chips_card).setVisibility(View.VISIBLE);
        }catch (Exception e){
            e.printStackTrace();
            String[] chipNames = new String[]{"請選擇"};
            chipValues = new String[]{""};
            ArrayAdapter<CharSequence> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, chipNames);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            chipsSpinner = findViewById(R.id.chips);
            chipsSpinner.setAdapter(adapter);
        }

    }

    @SuppressLint("ApplySharedPref")
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

    @UiThread
    private void readBotData() {
        try {
            JSONArray monsters = MonsterDataManager.getInstance().getMonsters();
            int len = monsters.length();
            monstersArray = new String[len+1];
            monstersArray[0] = getString(R.string.select_one);
            for(int i = 0; i < len; i++){
                monstersArray[i+1] = monsters.getJSONObject(i).getString("寵物名稱");
            }
            ArrayAdapter<CharSequence> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, monstersArray);
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
            ArrayAdapter<CharSequence> teamAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, teamArray);
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
            findViewById(R.id.bot_mons_box).setEnabled(true);

        } catch (Exception e) {
            flowJob.stop();
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
        MonsterDataManager.clear();
        super.onDestroy();
    }

    private void sendCommand(String command){

        Uri targetUri = Uri.parse(getString(R.string.command_prefix) + command + getString(R.string.command_append));
        Intent intent = new Intent(Intent.ACTION_VIEW, targetUri);
        List<ResolveInfo> resInfo = getPackageManager().queryIntentActivities(intent, 0);
        List<Intent> targetedShareIntents = new ArrayList<>();
        if (!resInfo.isEmpty()) {

            for (ResolveInfo info : resInfo) {
                ActivityInfo activityInfo = info.activityInfo;
                Log.d("packageName", activityInfo.packageName);
                if (!activityInfo.packageName.equalsIgnoreCase("com.plurk.android")) {
                    Intent targeted = new Intent(Intent.ACTION_VIEW, targetUri);
                    targeted.setPackage(activityInfo.packageName);
                    targetedShareIntents.add(targeted);
                }

            }

        }

        String[] textSharedTarget = new String[]{
                "com.plurk.android",
                "tw.anddev.aplurk",
                "com.skystar.plurk",
                "com.roguso.plurk",
                "com.nauj27.android.pifeb",
                "idv.brianhsu.maidroid.plurk"
        };

        for (String aTarget : textSharedTarget) {

            PackageManager manager = getPackageManager();
            Intent searchIntent = new Intent().setPackage(aTarget);
            List<ResolveInfo> infoList = manager.queryIntentActivities(searchIntent, 0);

            if (infoList != null && infoList.size() > 0)
            {
                Intent targeted = new Intent(Intent.ACTION_SEND);
                targeted.setType("text/plain");
                targeted.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name));
                targeted.putExtra(Intent.EXTRA_TEXT, command + " " + getString(R.string.bz));
                targeted.setPackage(aTarget);
                targetedShareIntents.add(targeted);
            }

        }

        if(targetedShareIntents.size() > 0){
            Intent chooserIntent = Intent.createChooser(targetedShareIntents.remove(0), "Open...");
            chooserIntent.putExtra(Intent.EXTRA_TITLE, "Open...");
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, targetedShareIntents.toArray(new Parcelable[]{}));
            try {
                startActivity(chooserIntent);
            } catch (android.content.ActivityNotFoundException ex) {
                Toast.makeText(this, R.string.no_available_app, Toast.LENGTH_SHORT).show();
            }
        }else{
            Toast.makeText(this, R.string.no_available_app, Toast.LENGTH_SHORT).show();
        }
    }
}
