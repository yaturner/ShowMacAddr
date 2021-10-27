package com.cryoinnovations.showmacaddr;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.TargetApi;
import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.text.TextUtils;
import android.util.Log;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "ShowMacAddress";

    private TextView tv_macAddress = null;
    private Pattern pat1 = Pattern.compile("\\d+\\:\\s*wlan0\\:");
    private Pattern pat2 = Pattern.compile("link/ether\\s*([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})|([0-9a-fA-F]{4}\\.[0-9a-fA-F]{4}\\.[0-9a-fA-F]{4})");
    private boolean found = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tv_macAddress = findViewById(R.id.macAddress);
        if(isNetworkOnline(this)) {
            String o = shell_exec("ip addr");
            Log.d(TAG, o);
            String[] lines = o.split("\n");
            for (int lineCtr = 0; lineCtr < lines.length; lineCtr++) {
                if (!found) {
                    Matcher matcher1 = pat1.matcher(lines[lineCtr]);
                    if (matcher1.find()) {
                        Log.d("ADDR", "found pat1");
                        found = true;
                    }
                } else {
                    Matcher matcher2 = pat2.matcher(lines[lineCtr]);
                    if (matcher2.find()) {
                        String s = matcher2.group().split(" ")[1];
                        Log.d(TAG, "found " + s);
                        s = "MacAddress: " + s;
                        tv_macAddress.setText(s);
                    }
                }
            }
        } else {
            tv_macAddress.setText(R.string.notConnected);
        }
    }


    public String shell_exec(String cmd) {
        String o = null;
        try {
            Process p = Runtime.getRuntime().exec(cmd);
            BufferedReader b;
            b = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            while ((line = b.readLine()) != null) o += line + "\n";
        } catch (Exception e) {
            o = "error";
        }
        return o;
    }

    @TargetApi(Build.VERSION_CODES.M)
    public static boolean isNetworkOnline(Context context) {
        boolean isOnline = false;
        try {
            ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            String reason = manager.getActiveNetworkInfo().getReason();
            if(reason.equalsIgnoreCase("CONNECTED")) {
                return true;
            } else {
                return false;
            }
            //NetworkCapabilities capabilities = manager.getNetworkCapabilities(manager.getActiveNetwork());  // need ACCESS_NETWORK_STATE permission
            //isOnline = capabilities != null && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return isOnline;
    }
}