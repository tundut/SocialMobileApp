package vn.edu.ueh.socialapplication;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        LinearLayout btnToggleTheme = findViewById(R.id.btnToggleTheme);
        LinearLayout btnClearCache = findViewById(R.id.btnClearCache);
        LinearLayout btnLogout = findViewById(R.id.btnLogout);
        TextView tvVersion = findViewById(R.id.tvVersion);

        // Display App Version
        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            String version = pInfo.versionName;
            tvVersion.setText("App Version " + version);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            tvVersion.setText("App Version Unknown");
        }

        // Toggle Theme Logic
        btnToggleTheme.setOnClickListener(v -> {
            int currentMode = AppCompatDelegate.getDefaultNightMode();
            if (currentMode == AppCompatDelegate.MODE_NIGHT_YES) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                Toast.makeText(this, "Switched to Light Mode", Toast.LENGTH_SHORT).show();
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                Toast.makeText(this, "Switched to Dark Mode", Toast.LENGTH_SHORT).show();
            }
        });

        // Clear Cache Logic
        btnClearCache.setOnClickListener(v -> {
            try {
                // Clear Glide Cache (if used)
                new Thread(() -> {
                     com.bumptech.glide.Glide.get(SettingsActivity.this).clearDiskCache();
                }).start();
                com.bumptech.glide.Glide.get(SettingsActivity.this).clearMemory();
                
                // Clear App Cache
                deleteCache(this);
                Toast.makeText(this, "Cache cleared successfully", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Failed to clear cache", Toast.LENGTH_SHORT).show();
            }
        });

        // Logout Logic
        btnLogout.setOnClickListener(v -> {
            // Clear User Session (Example using SharedPreferences)
            SharedPreferences preferences = getSharedPreferences("UserSession", MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.clear();
            editor.apply();

            Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show();
            
            // Navigate to Login Activity (If it existed) or finish all activities
            // For now, let's just close this activity and maybe restart the app
            finishAffinity(); 
        });
    }

    private static void deleteCache(Context context) {
        try {
            java.io.File dir = context.getCacheDir();
            deleteDir(dir);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static boolean deleteDir(java.io.File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new java.io.File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
            return dir.delete();
        } else if (dir != null && dir.isFile()) {
            return dir.delete();
        } else {
            return false;
        }
    }
}
