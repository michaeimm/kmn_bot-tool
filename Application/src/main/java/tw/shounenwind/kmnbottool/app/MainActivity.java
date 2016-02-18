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
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.util.HashMap;

import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import tw.shounenwind.PlurkConnection;
import tw.shounenwind.kmnbottool.R;

public class MainActivity extends AppCompatActivity {

    private JSONObject player;
    private JSONArray monsters;
    private String APPKEY = "jmhU8XgtEHkn";
    private String APPSECRET = "zfKBevAPuLNq3od4lbVGy7dgxVDSB2LD";
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_screen);

        screenPrepare();
        SharedPreferences sharedPreferences = getSharedPreferences("data", MODE_PRIVATE);
        String user_id = sharedPreferences.getString("user_id", null);
        if(user_id != null)
            getBotData(user_id);
        else{
            showPlurkIdInput();
        }
    }

    private void showPlurkIdInput() {
        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.plurk_name_dialog, (ViewGroup) findViewById(R.id.dialog));
        final EditText input = (EditText)layout.findViewById(R.id.name);
        new AlertDialog.Builder(this)
                .setTitle("請輸入噗浪帳號")
                .setView(layout)
                .setPositiveButton("確定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            showProgressDialog("正在讀取噗浪帳號");
                            getPlurkId(input.getText().toString());
                            dialog.dismiss();
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                })
                .show();
    }

    private void showProgressDialog(String text){
        progressDialog = ProgressDialog.show(this,
                "讀取中", text, true);
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
        showProgressDialog("正在載入寵物資料");
        Observable
                .create(new Observable.OnSubscribe<String>() {
                    @Override
                    public void call(Subscriber<? super String> subscriber) {
                        try {
                            URL url = new URL("http://133.130.102.121:9163/petInfos/" + plurk_id + ".json");
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
                        Toast.makeText(MainActivity.this, "無法載入寵物資料", Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onNext(String s) {
                        readBotData(s);
                        dismissProgressDialog();
                    }
                });


    }

    private void getPlurkId(final String plurk_name){
        Observable
                .create(new Observable.OnSubscribe<String>() {
                    @Override
                    public void call(Subscriber<? super String> subscriber) {
                        try {
                            PlurkConnection plurkConnection = new PlurkConnection(APPKEY, APPSECRET, true);
                            HashMap<String, String> args = new HashMap<String, String>();
                            args.put("query", plurk_name);
                            plurkConnection.startConnect("UserSearch/search", args);
                            if(plurkConnection.getStatusCode() != 200){
                                throw new Exception(plurkConnection.getResponse());
                            }
                            JSONObject jsonObject = new JSONObject(plurkConnection.getResponse());
                            subscriber.onNext(jsonObject.getJSONArray("users").getJSONObject(0).getString("id"));
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
                        Toast.makeText(MainActivity.this, "無法載入噗浪帳號", Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onNext(String s) {
                        SharedPreferences sharedPreferences = getSharedPreferences("data", MODE_PRIVATE);
                        sharedPreferences.edit().putString("user_id", s).commit();
                        findViewById(R.id.bot_draw).setEnabled(true);
                        dismissProgressDialog();
                        getBotData(s);
                    }
                });


    }

    private void readBotData(String data){
        try {
            player = (new JSONObject(data)).getJSONObject("玩家");
            monsters = (new JSONObject(data)).getJSONArray("寵物");
            //Log.d("player", player.toString());
            //Log.d("mons", mons.toString());
            int len = monsters.length();
            String[] monstersArray = new String[len+1];
            monstersArray[0] = "請選擇";
            for(int i = 0; i < len; i++){
                monstersArray[i+1] = monsters.getJSONObject(i).getString("寵物名稱");
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
            findViewById(R.id.bot_draw).setEnabled(true);
            findViewById(R.id.attacter).setEnabled(true);
            findViewById(R.id.supporter).setEnabled(true);
            findViewById(R.id.bot_exp).setEnabled(true);
            findViewById(R.id.bot_battle).setEnabled(true);
            findViewById(R.id.bot_hell_battle).setEnabled(true);
            findViewById(R.id.bot_mons_box).setEnabled(true);


            /*attacter.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                    if(position == 0){
                        return false;
                    }
                    else{
                        try {
                            new AlertDialog.Builder(MainActivity.this)
                                    .setTitle(monsters.getJSONObject(position - 1).getString("寵物名稱"))
                                    .setMessage(
                                            new StringBuilder()
                                                    .append("原TYPE：")
                                                    .append(monsters.getJSONObject((int) (id - 1)).getString("原TYPE"))
                                                    .append("\n")
                                                    .append("下場TYPE：")
                                                    .append(monsters.getJSONObject((int) (id - 1)).getString("下場TYPE"))
                                                    .append("\n")
                                                    .append("等級 ")
                                                    .append(monsters.getJSONObject((int) (id - 1)).getString("等級"))
                                                    .append(" / ")
                                                    .append("階級 ")
                                                    .append(monsters.getJSONObject((int) (id - 1)).getString("階級"))
                                                    .append("\n")
                                                    .append(monsters.getJSONObject((int) (id - 1)).getString("技能"))
                                    ).setPositiveButton("確認", null)
                                    .show();
                            return true;
                        } catch (JSONException e) {
                            e.printStackTrace();
                            return false;
                        }
                    }
                }
            });*/

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "無法載入寵物資料", Toast.LENGTH_LONG).show();
        }
    }


}
