package com.appbundle.test;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import com.airbnb.lottie.LottieAnimationView;
import com.google.android.play.core.splitcompat.SplitCompat;
import com.google.android.play.core.splitinstall.*;
import com.google.android.play.core.splitinstall.model.SplitInstallErrorCode;
import com.google.android.play.core.splitinstall.model.SplitInstallSessionStatus;
import com.google.android.play.core.tasks.OnFailureListener;
import com.google.android.play.core.tasks.OnSuccessListener;
import com.worklight.wlclient.api.WLAccessTokenListener;
import com.worklight.wlclient.api.WLAuthorizationManager;
import com.worklight.wlclient.api.WLFailResponse;
import com.worklight.wlclient.auth.AccessToken;

public class MainActivity extends AppCompatActivity implements SplitInstallStateUpdatedListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    Context context = this;
    SplitInstallManager splitInstallManager;
    TextView tvGotoFeature;
    TextView tvDownloadDynamicFeature;
    TextView tvWelcomeMsg;
    LottieAnimationView animationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toast.makeText(
                context,
                "Connecting to MobileFirst Server",
                Toast.LENGTH_LONG
        ).show();
        WLAuthorizationManager.getInstance().obtainAccessToken("", new WLAccessTokenListener() {
            @Override
            public void onSuccess(final AccessToken accessToken) {
                ((Activity) context).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(
                                context,
                                "Successfully Conected to MobileFirst Server" + "Obtained Access Token",
                                Toast.LENGTH_LONG
                        ).show();

                    }
                });
            }

            @Override
            public void onFailure(final WLFailResponse wlFailResponse) {
                ((Activity) context).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(
                                context,
                                "Failed to Connect to MobileFirst Server: " + wlFailResponse.getErrorMsg(),
                                Toast.LENGTH_LONG
                        ).show();
                    }
                });
            }
        });

        splitInstallManager = SplitInstallManagerFactory.create(context);
        SplitCompat.install(this);

        animationView = findViewById(R.id.animation);
        animationView.setSpeed(0.5f);

        tvGotoFeature = findViewById(R.id.tv_goto_feature);
        tvDownloadDynamicFeature = findViewById(R.id.tv_download_dynamic_feature);
        tvWelcomeMsg = findViewById(R.id.tv_welcome_msg);

        if(checkDynamicFeatureInstallStatus()) {
            tvGotoFeature.setVisibility(View.VISIBLE);
            tvDownloadDynamicFeature.setVisibility(View.GONE);
            tvWelcomeMsg.setText("welcome, click the button below to go to Credit Card section.");
        } else {
            tvWelcomeMsg.setText("uh oh! module not installed yet. Click on the button below to install the Credit Card Module!");
            tvGotoFeature.setVisibility(View.GONE);
            tvDownloadDynamicFeature.setVisibility(View.VISIBLE);
        }

        tvGotoFeature.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    startActivity(new Intent(MainActivity.this, Class.forName("com.appbundle.test.dynamic_feature1.ActivityFeature1")));
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        });

        tvDownloadDynamicFeature.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                downloadDynamicFeature("dynamic_feature1");
            }
        });
    }

    private boolean checkDynamicFeatureInstallStatus() {
        return splitInstallManager.getInstalledModules().contains("dynamic_feature1");
    }

    private void downloadDynamicFeature(final String moduleName) {
        SplitInstallRequest request = SplitInstallRequest.newBuilder()
                .addModule(moduleName)
                .build();

        splitInstallManager.registerListener(this);
        splitInstallManager.startInstall(request)
                .addOnSuccessListener(new OnSuccessListener<Integer>() {
                    @Override
                    public void onSuccess(Integer integer) {
                        logAndTag("Successfully Installed module: " + moduleName);
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception exception) {
                switch (((SplitInstallException) exception).getErrorCode()) {
                    case SplitInstallErrorCode.NETWORK_ERROR:
                        logAndTag("Failed to Install module. NETWORK_ERROR", exception);
                        break;
                    case SplitInstallErrorCode.MODULE_UNAVAILABLE:
                        logAndTag("Failed to Install module. MODULE_UNAVAILABLE", exception);
                        break;
                }
            }
        });
    }

    private void logAndTag(String msg) {
        Log.i(TAG, msg);
        Toast.makeText(MainActivity.this, msg, Toast.LENGTH_LONG).show();
    }

    private void logAndTag(String msg, Exception e) {
        e.printStackTrace();
        Log.e(TAG, msg + " " + e.getMessage());
        Toast.makeText(MainActivity.this, msg + " " + e.getMessage(), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onStateUpdate(SplitInstallSessionState state) {
        for (String name : state.moduleNames()) {
            // Handle changes in state.
            switch (state.status()) {
                case SplitInstallSessionStatus.DOWNLOADING:
                    logAndTag("downloading: " + name);
                    break;

                case SplitInstallSessionStatus.INSTALLED:
                    logAndTag("installed: " + name);
                    tvGotoFeature.setVisibility(View.VISIBLE);
                    tvDownloadDynamicFeature.setVisibility(View.GONE);
                    tvWelcomeMsg.setText("welcome, click the button below to go to Credit Card section.");
                    break;

                case SplitInstallSessionStatus.INSTALLING:
                    logAndTag("installing: " + name);
                    break;

                case SplitInstallSessionStatus.FAILED:
                    logAndTag("error... " + state.errorCode() + " for module: " + name);
                    break;
            }
        }
    }
}
