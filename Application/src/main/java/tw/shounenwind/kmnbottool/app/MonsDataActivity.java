package tw.shounenwind.kmnbottool.app;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import tw.shounenwind.kmnbottool.R;

public class MonsDataActivity extends AppCompatActivity {

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
            RecyclerView listView;

            ArrayAdapter listAdapter;

            listView = (RecyclerView)findViewById(R.id.list);
            listView.setLayoutManager(new LinearLayoutManager(this));
            listAdapter = new ArrayAdapter(monsters);
            listView.setAdapter(listAdapter);
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

    class ArrayAdapter extends RecyclerView.Adapter{
        private JSONArray monsters;

        public ArrayAdapter(JSONArray monsters) {
            this.monsters = monsters;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ListViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.mon_list, null));
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            JSONObject monster = null;
            try {
                monster = monsters.getJSONObject(position);
                ((ListViewHolder)holder).textView.setText(monster.getString("寵物名稱"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            final JSONObject finalMonster = monster;
            ((ListViewHolder)holder).textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try{
                        new AlertDialog.Builder(MonsDataActivity.this)
                                .setTitle(finalMonster.getString("寵物名稱"))
                                .setMessage(
                                        "原TYPE：" + finalMonster.getString("原TYPE") + "\n" +
                                        "下場TYPE：" + finalMonster.getString("下場TYPE") + "\n" +
                                        "等級：" + finalMonster.getString("等級") + "\n" +
                                        "階級：" + finalMonster.getString("階級") + "\n" +
                                        "\n" + finalMonster.getString("技能")
                                ).setPositiveButton("關閉", null)
                                .show();
                    }catch (Exception e){
                        e.printStackTrace();
                    }

                }
            });

        }

        public class ListViewHolder extends RecyclerView.ViewHolder{
            public TextView textView;

            public ListViewHolder(View itemView) {
                super(itemView);
                textView = (TextView)itemView.findViewById(android.R.id.text1);
            }
        }

        @Override
        public int getItemCount() {
            return monsters.length();
        }
    }
}
