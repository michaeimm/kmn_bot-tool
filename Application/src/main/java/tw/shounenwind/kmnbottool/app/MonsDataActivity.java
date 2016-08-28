package tw.shounenwind.kmnbottool.app;

import android.content.Intent;
import android.graphics.Bitmap;
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
import com.bumptech.glide.integration.okhttp.OkHttpUrlLoader;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.bumptech.glide.request.target.Target;
import com.squareup.okhttp.OkHttpClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;

import tw.shounenwind.kmnbottool.R;

public class MonsDataActivity extends AppCompatActivity {

    private JSONObject player;
    private RecyclerView listView;

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
        Intent intent = getIntent();
        try {
            JSONArray monsters = new JSONArray(intent.getStringExtra("monster"));
            player = new JSONObject(intent.getStringExtra("player"));

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
            return new ListViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.monster_unit, null));
        }

        @Override
        public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
            JSONObject monster = null;
            try {
                monster = monsters.getJSONObject(position);
                ((ListViewHolder)holder).monsterName.setText(monster.getString("寵物名稱"));
                final ImageView imageView = ((ListViewHolder) holder).monsterImg;
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
                ((ListViewHolder)holder).monsterType.setText(star.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            final JSONObject finalMonster = monster;
            ((ListViewHolder)holder).monster_unit.setOnClickListener(new View.OnClickListener() {
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
                                getString(R.string.monster_level) + finalMonster.getString("等級") + "\n" +
                                getString(R.string.monster_class) + finalMonster.getString("階級") + "\n" +
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

        @Override
        public int getItemCount() {
            return monsters.length();
        }

        public class ListViewHolder extends RecyclerView.ViewHolder{
            public RelativeLayout monster_unit;
            public ImageView monsterImg;
            public TextView monsterName;
            public TextView monsterType;

            public ListViewHolder(View itemView) {
                super(itemView);
                monster_unit = (RelativeLayout)itemView.findViewById(R.id.monster_unit);
                monsterImg = (ImageView)itemView.findViewById(R.id.monster_img);
                monsterName = (TextView)itemView.findViewById(R.id.monster_name);
                monsterType = (TextView)itemView.findViewById(R.id.monster_type);
            }
        }
    }
}
