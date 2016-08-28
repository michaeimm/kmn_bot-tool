package tw.shounenwind.kmnbottool.app;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import tw.shounenwind.kmnbottool.R;

public class LicenseActivity extends AppCompatActivity {
    private WebView wv;
    private PlurkWebViewClient pwvc = new PlurkWebViewClient();
    private Map<String, String> licenses = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setLicenses();
        screenPrepare();
    }

    private void screenPrepare() {
        setContentView(R.layout.license_screen);
        wv = (WebView) findViewById(R.id.webview);
        wv.setVerticalScrollBarEnabled(true);
        wv.setHorizontalScrollBarEnabled(false);
        wv.setWebViewClient(pwvc);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            wv.removeJavascriptInterface("searchBoxJavaBridge_");
            wv.removeJavascriptInterface("accessibility");
            wv.removeJavascriptInterface("accessibilityTraversal");
        }


        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        wv_load();
    }

    private void wv_load() {
        final StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("<!DOCTYPE html>"
                + "<html>"
                + "<head>"
                + " <meta charset=\"utf-8\">"
                + " <title>HealingPlurk</title>"
                + " <meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">"
                + " <script src=\"file:///android_asset/jquery-3.1.0.min.js\"></script>"
                + " <style>"
                + "body{\n" +
                "\tbackground: #303030;\n" +
                "\tbackground-attachment: fixed;\n" +
                "}"
                + ".plurk_cnt{\n" +
                "position: relative;\n" +
                "padding: 8px 8px 8px 5px;\n" +
                "margin-bottom: 8px;\n" +
                "max-width: auto;\n" +
                "border-radius: 2px;\n" +
                "display: block;\n" +
                "text-decoration: none;\n" +
                "overflow:hidden;\n" +
                "box-shadow: 0 2px 2px 0 rgba(0,0,0,.14),0 3px 1px -2px rgba(0,0,0,.2),0 1px 5px 0 rgba(0,0,0,.12);\n" +
                "background: rgba(66, 66, 66, 0.8);\n" +
                "border-left: 5px solid rbga(0, 0, 0, 0);\n" +
                "color: #EEE;" +
                "}"
                + ".plurk_content{\n" +
                "padding: 16px 8px 0px 2px;\n" +
                "margin: 16px 0 8px 0;\n" +
                "word-wrap: break-word;\n" +
                "max-width: 100%;\n" +
                "border-top: 1px solid #9e9e9e;\n" +
                "color: #EEE;" +
                "}"
                + " </style>"
                + "</head>"
                + "<body>");
        ExecutorService executor = Executors.newFixedThreadPool(4);
        final Object[] keys = licenses.keySet().toArray();
        int len = keys.length;
        final StringBuilder[] results = new StringBuilder[len];
        for (int i = 0; i < len; i++) {
            final int finali = i;
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        StringBuilder temp = new StringBuilder();
                        temp.append("<div class=\"plurk_cnt\">");
                        temp.append("Notice for ");
                        temp.append((String) keys[finali]);
                        temp.append("<div class=\"plurk_content\" style=\"font-size: 12px; margin-top: 4px; padding-top: 4px;\">");
                        //noinspection SuspiciousMethodCalls
                        temp.append(licenses.remove(keys[finali]).replace("\n", "<br>"));
                        temp.append("</div>");
                        temp.append("</div>");
                        results[finali] = temp;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }

        for (int i = 0; i < len; i++) {
            while (results[i] == null) {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            stringBuilder.append(results[i]);
            results[i] = null;
        }

        stringBuilder.append("</body>"
                + "</html>");
        wv.loadDataWithBaseURL("file:///android_asset/river.html", stringBuilder.toString(), "text/html", "UTF-8", null);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void setLicenses() {
        licenses.put("Material icons", "We have made these icons available for you to incorporate them into your products under the Creative Common Attribution 4.0 International License (CC-BY 4.0). Feel free to remix and re-share these icons and documentation in your products. We'd love attribution in your app's about screen, but it's not required. The only thing we ask is that you not re-sell the icons themselves.");
        licenses.put("Glide", "BSD, part MIT and Apache 2.0.");
    }

    private class PlurkWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
            wv.stopLoading();
            return true;
        }
    }
}