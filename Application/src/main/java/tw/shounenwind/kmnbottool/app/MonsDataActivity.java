package tw.shounenwind.kmnbottool.app;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import tw.shounenwind.kmnbottool.R;

public class MonsDataActivity extends AppCompatActivity {

    String[] list;
    String[] detail;
    private JSONObject player;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mons_data);
        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Intent intent = getIntent();
        try {
            JSONArray monsters = new JSONArray(intent.getStringExtra("monster"));
            player = new JSONObject(intent.getStringExtra("player"));
            int len = monsters.length();
            list = new String[len];
            detail = new String[len];
            ListView listView;

            ArrayAdapter<String> listAdapter;

            for(int i = 0; i < len; i++){
                JSONObject monster = monsters.getJSONObject(i);
                list[i] = monster.getString("寵物名稱");
                detail[i] =
                        "原TYPE：" + monster.getString("原TYPE") + "\n" +
                        "下場TYPE：" + monster.getString("下場TYPE") + "\n" +
                        "等級：" + monster.getString("等級") + "\n" +
                        "階級：" + monster.getString("階級") + "\n" +
                        "\n" + monster.getString("技能");
            }
            listView = (ListView)findViewById(R.id.list);
            listAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, list);
            listView.setAdapter(listAdapter);
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    new AlertDialog.Builder(MonsDataActivity.this)
                            .setTitle(list[position])
                            .setMessage(detail[position])
                            .setPositiveButton("關閉", null)
                            .show();
                }
            });
        } catch (JSONException e) {
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
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

                android.support.v7.app.AlertDialog alertDialog = new android.support.v7.app.AlertDialog.Builder(this)
                        .setMessage("蒐集完成度: " + s)
                        .setPositiveButton("確認", null)
                        .create();
                alertDialog.show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
