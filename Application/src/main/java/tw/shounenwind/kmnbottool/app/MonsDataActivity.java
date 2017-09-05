package tw.shounenwind.kmnbottool.app;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.bumptech.glide.request.target.Target;
import com.google.common.base.Function;
import com.google.common.collect.Ordering;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import okhttp3.OkHttpClient;
import tw.shounenwind.kmnbottool.R;

public class MonsDataActivity extends AppCompatActivity {

    private JSONObject player;
    private RecyclerView listView;
    private MonsterDataManager monsterDataManager;
    private ArrayAdapter listAdapter;
    private ProgressDialog progressDialog;
    private static final int RED_TYPE = Color.parseColor("#ff4081");
    private static final int GREEN_TYPE = Color.parseColor("#8bc34a");
    private static final int BLUE_TYPE = Color.parseColor("#00b0ff");
    private static final int YELLOW_TYPE = Color.parseColor("#ffea00");
    private static final int BLACK_TYPE = Color.parseColor("#bdbdbd");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mons_data);
        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Glide.get(this)
                .register(GlideUrl.class, InputStream.class, new OkHttpUrlLoader.Factory(new OkHttpClient()));
        monsterDataManager = MonsterDataManager.getInstance();
        JSONArray monsters = monsterDataManager.getMonsters();
        player = monsterDataManager.getPlayer();

        listAdapter = new ArrayAdapter(monsters);

        listView = (RecyclerView)findViewById(R.id.list);
        listView.setLayoutManager(new LinearLayoutManager(this));
        listView.setAdapter(listAdapter);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.mons_data_menu, menu);
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
                return true;
            case R.id.sort_series:
                item.setChecked(true);
                sort("系列");
                return true;
            case R.id.sort_name:
                item.setChecked(true);
                sort("寵物名稱");
                return true;
            case R.id.sort_rare:
                item.setChecked(true);
                sort("稀有度");
                return true;
            case R.id.sort_level:
                item.setChecked(true);
                sort("等級");
                return true;
            case R.id.sort_class:
                item.setChecked(true);
                sort("階級");
                return true;
            case R.id.sort_type:
                item.setChecked(true);
                sort("原TYPE");
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void sort(final String key){
        showProgressDialog(getString(R.string.monster_loading));
        new Thread(new Runnable() {
            @Override
            public void run() {
                JSONArray monsters = monsterDataManager.getMonsters();
                List<JSONObject> jsonValues = new ArrayList<>();
                int len = monsters.length();
                for (int i = 0; i < len; i++) {
                    try {
                        jsonValues.add(monsters.getJSONObject(i));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }if (key.equals("等級") || key.equals("階級") || key.equals("稀有度")) {
                    Collections.sort(jsonValues, Ordering.natural().nullsFirst()
                            .onResultOf(new Function<JSONObject, Integer>() {
                                @Override
                                public Integer apply(JSONObject p) {
                                    try {


                                        String aString = p.getString(key);

                                        if (aString.contains("Max"))
                                            aString = aString.substring(0, aString.indexOf("（"));
                                        return Integer.valueOf(aString);

                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    return 101;
                                }
                            }));
                } else {
                    final Collator collator = Collator.getInstance(Locale.CHINESE);
                    Collections.sort(jsonValues, new Comparator<JSONObject>() {
                        @Override
                        public int compare(JSONObject o1, JSONObject o2) {

                            try {
                                return collator.compare(o1.getString(key)+o1.getString("寵物名稱"), o2.getString(key)+o2.getString("寵物名稱"));
                            } catch (JSONException e) {
                                e.printStackTrace();
                                return 0;
                            }
                        }
                    });
                }
                final JSONArray sortedJsonArray = new JSONArray(jsonValues);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        listAdapter.setMonsters(sortedJsonArray);
                        listAdapter.notifyDataSetChanged();
                        dismissProgressDialog();
                    }
                });

            }
        }).start();

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

    private class ArrayAdapter extends RecyclerView.Adapter<ArrayAdapter.ListViewHolder>{
        private JSONArray monsters;

        ArrayAdapter(JSONArray monsters) {
            this.monsters = monsters;
        }

        void setMonsters(JSONArray monsters) {
            this.monsters = monsters;
        }

        @Override
        public ArrayAdapter.ListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ListViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.monster_unit, null));
        }

        @SuppressLint("SetTextI18n")
        @Override
        public void onBindViewHolder(final ArrayAdapter.ListViewHolder holder, int position) {
            JSONObject monster = null;
            try {
                monster = monsters.getJSONObject(position);
                if(monster.getString("等級").contains("Lv.Max"))
                    holder.monsterName.setText(monster.getString("寵物名稱") + " (Lv.Max)");
                else
                    holder.monsterName.setText(monster.getString("寵物名稱") + " (Lv." + monster.getString("等級") + ")");
                final ImageView imageView = holder.monsterImg;
                Glide.with(MonsDataActivity.this)
                        .load(monster.getString("圖片"))
                        .asBitmap()
                        .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                        .centerCrop()
                        .error(R.drawable.ic_launcher)
                        .placeholder(R.drawable.ic_launcher)
                        .into(new BitmapImageViewTarget(imageView) {
                            @Override
                            protected void setResource(Bitmap resource) {
                                RoundedBitmapDrawable circularBitmapDrawable =
                                        RoundedBitmapDrawableFactory.create(MonsDataActivity.this.getResources(), resource);
                                circularBitmapDrawable.setCircular(true);
                                imageView.setImageDrawable(circularBitmapDrawable);
                            }
                        });
                StringBuilder star = new StringBuilder();
                int len = monster.getInt("稀有度");
                for(int i = 0; i < len; i++) star.append("☆");
                holder.monsterType.setText(star.toString());
                holder.monsterType.setTextColor(getMonsterColor(monster.getString("原TYPE")));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            final JSONObject finalMonster = monster;
            holder.monster_unit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        ScrollView m_dialogView = (ScrollView)getLayoutInflater().inflate(R.layout.monster_dialog, null);
                        ImageView m_imageView = (ImageView) m_dialogView.findViewById(R.id.monster_img);
                        TextView m_textView = ((TextView) m_dialogView.findViewById(R.id.monster_type));
                        Display display = getWindowManager().getDefaultDisplay();
                        Point size = new Point();
                        int imageWidth;
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
                            display.getSize(size);
                            imageWidth = size.x;
                        }else{
                            imageWidth = display.getWidth();
                        }
                        imageWidth -= 150;
                        Glide.with(MonsDataActivity.this)
                                .load(finalMonster.getString("圖片"))
                                .asBitmap()
                                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                                .fitCenter()
                                .error(R.drawable.ic_launcher)
                                .placeholder(R.drawable.ic_launcher)
                                .override(Target.SIZE_ORIGINAL, imageWidth)
                                .into(m_imageView);
                        m_textView.setText(getString(R.string.monster_type) + "：" + finalMonster.getString("原TYPE") + "\n" +
                                getString(R.string.battle_type) + "：" + finalMonster.getString("下場TYPE") + "\n" +
                                getString(R.string.monster_series) + "：" + finalMonster.getString("系列") + "\n" +
                                getString(R.string.monster_level) + " " + finalMonster.getString("等級") + "\n" +
                                getString(R.string.monster_class) + " " + finalMonster.getString("階級") + "\n" +
                                "\n" + finalMonster.getString("技能"));
                        new AlertDialog.Builder(MonsDataActivity.this)
                                .setView(m_dialogView)
                                .setTitle(finalMonster.getString("寵物名稱"))
                                .setPositiveButton(R.string.close, null)
                                .show();
                    }catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            });

        }

        private int getMonsterColor(String type){
            switch (type) {
                case "[紅]":
                    return RED_TYPE;
                case "[綠]":
                    return GREEN_TYPE;
                case "[藍]":
                    return BLUE_TYPE;
                case "[黃]":
                    return YELLOW_TYPE;
                case "[黑]":
                    return BLACK_TYPE;
                default:
                    return Color.WHITE;
            }
        }

        @Override
        public void onViewRecycled(ArrayAdapter.ListViewHolder holder) {
            super.onViewRecycled(holder);
            Glide.clear(holder.monsterImg);
        }

        @Override
        public int getItemCount() {
            return monsters.length();
        }

        class ListViewHolder extends RecyclerView.ViewHolder{
            RelativeLayout monster_unit;
            ImageView monsterImg;
            TextView monsterName;
            TextView monsterType;

            ListViewHolder(View itemView) {
                super(itemView);
                monster_unit = (RelativeLayout)itemView.findViewById(R.id.monster_unit);
                monsterImg = (ImageView)itemView.findViewById(R.id.monster_img);
                monsterName = (TextView)itemView.findViewById(R.id.monster_name);
                monsterType = (TextView)itemView.findViewById(R.id.monster_type);
            }
        }
    }
}
