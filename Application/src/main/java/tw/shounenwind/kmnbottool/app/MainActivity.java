package tw.shounenwind.kmnbottool.app;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;

import tw.shounenwind.kmnbottool.R;

public class MainActivity extends AppCompatActivity {

    private KmnBotHandler handler = new KmnBotHandler(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_screen);

        screenPrepare();
        getBotData();
    }

    private void screenPrepare(){
        Button bot_draw = (Button) findViewById(R.id.bot_draw);
        bot_draw.setOnClickListener(bot_draw_command);

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

    private void getBotData(){
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {

                    URL url = new URL("http://133.130.102.121:9163/petInfos/8433188.json");
                    HttpURLConnection httpURLConnection = (HttpURLConnection)url.openConnection(Proxy.NO_PROXY);
                    httpURLConnection.setUseCaches(false);
                    httpURLConnection.setRequestMethod("GET");
                    httpURLConnection.connect();
                    BufferedReader bufferedReader;
                    if(httpURLConnection.getResponseCode() == HttpURLConnection.HTTP_OK){
                        bufferedReader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream(), "UTF-8"));
                    }else{
                        bufferedReader = new BufferedReader(new InputStreamReader(httpURLConnection.getErrorStream(), "UTF-8"));
                    }

                    String line;
                    StringBuilder stringBuilder = new StringBuilder();

                    while((line = bufferedReader.readLine()) != null) {
                        stringBuilder.append(line);
                        stringBuilder.append("\n");
                    }
                    bufferedReader.close();
                    Message message = new Message();
                    message.obj = stringBuilder.toString();
                    message.what = handler.READ_BOT_DATA;
                    handler.sendMessage(message);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }

    private void readBotData(String data){
        try {
            JSONObject player = (new JSONObject(data)).getJSONObject("玩家");
            JSONArray mons = (new JSONObject(data)).getJSONArray("寵物");
            //Log.d("player", player.toString());
            //Log.d("mons", mons.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // BEGIN_INCLUDE(create_menu)
    /**
     * Use this method to instantiate your menu, and add your items to it. You
     * should return true if you have added items to it and want the menu to be displayed.
     */
    /*@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate our menu from the resources by using the menu inflater.
        getMenuInflater().inflate(R.menu.main, menu);

        // It is also possible add items here. Use a generated id from
        // resources (ids.xml) to ensure that all menu ids are distinct.
        MenuItem locationItem = menu.add(0, R.id.menu_location, 0, R.string.menu_location);
        locationItem.setIcon(R.drawable.ic_action_location);

        // Need to use MenuItemCompat methods to call any action item related methods
        MenuItemCompat.setShowAsAction(locationItem, MenuItem.SHOW_AS_ACTION_IF_ROOM);

        return true;
    }*/
    // END_INCLUDE(create_menu)

    // BEGIN_INCLUDE(menu_item_selected)
    /**
     * This method is called when one of the menu items to selected. These items
     * can be on the Action Bar, the overflow menu, or the standard options menu. You
     * should return true if you handle the selection.
     */
    /*@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            //TODO: menu item
        }

        return super.onOptionsItemSelected(item);
    }*/
    // END_INCLUDE(menu_item_selected)

    private class KmnBotHandler extends Handler{
        public final int READ_BOT_DATA = 0;
        private final WeakReference<MainActivity> mActivity;

        public KmnBotHandler(MainActivity activity){
            mActivity = new WeakReference<MainActivity>(activity);
        }

        public void handleMessage (Message msg){
            MainActivity activity = mActivity.get();
            if(activity != null){
                switch (msg.what){
                    case READ_BOT_DATA:
                        activity.readBotData((String)msg.obj);
                        break;
                }
            }

        }
    }
}
