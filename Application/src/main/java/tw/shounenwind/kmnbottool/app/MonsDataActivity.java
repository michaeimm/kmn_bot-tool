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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
            case R.id.menu_profile:
                String s;


                try {
                    if(player == null){
                        throw new Exception();
                    }
                    s = player.getString("蒐集完成度");
                } catch (Exception e) {
                    s = getString(R.string.no_data);
                }

                android.support.v7.app.AlertDialog alertDialog = new android.support.v7.app.AlertDialog.Builder(this)
                        .setMessage(getString(R.string.completion) + ": " + s)
                        .setPositiveButton(R.string.confirm, null)
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
                    try {
                        new AlertDialog.Builder(MonsDataActivity.this)
                                .setTitle(finalMonster.getString("寵物名稱"))
                                .setMessage(getString(R.string.monster_type) + "：" + finalMonster.getString("原TYPE") + "\n" +
                                                getString(R.string.battle_type) + "：" + finalMonster.getString("下場TYPE") + "\n" +
                                                getString(R.string.monster_level) + finalMonster.getString("等級") + "\n" +
                                                getString(R.string.monster_class) + finalMonster.getString("階級") + "\n" +
                                                "\n" + finalMonster.getString("技能")
                                ).setPositiveButton(R.string.close, null)
                                .show();
                    }catch (Exception e) {
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
