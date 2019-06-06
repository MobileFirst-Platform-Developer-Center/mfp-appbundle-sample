package com.appbundle.test.dynamic_feature1;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import com.worklight.jsonstore.api.JSONStoreCollection;
import com.worklight.jsonstore.api.WLJSONStore;
import com.worklight.jsonstore.exceptions.*;
import com.worklight.wlclient.api.WLAccessTokenListener;
import com.worklight.wlclient.api.WLAuthorizationManager;
import com.worklight.wlclient.api.WLClient;
import com.worklight.wlclient.api.WLFailResponse;
import com.worklight.wlclient.auth.AccessToken;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ActivityFeature1 extends AppCompatActivity {
    Context context = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feature1);

        WLClient.createInstance(context);
        TextView tvGetAuthToken = findViewById(R.id.tv_get_auth_token);
        tvGetAuthToken.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WLAuthorizationManager.getInstance().obtainAccessToken("", new WLAccessTokenListener() {
                    @Override
                    public void onSuccess(final AccessToken accessToken) {
                        ((Activity) context).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(
                                        context,
                                        "WLAuthorizationManager: " + "Obtained Access Token",
                                        Toast.LENGTH_LONG
                                ).show();
                                addToJSONStore();
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
                                        "WLAuthorizationManager: " + "Failed to obtain AT: " + wlFailResponse.getErrorMsg(),
                                        Toast.LENGTH_LONG
                                ).show();
                            }
                        });
                    }
                });
            }
        });
    }

    private void addToJSONStore() {
        try {
            JSONStoreCollection collection = new JSONStoreCollection("credit_card");
            List<JSONStoreCollection> list = new ArrayList<>();
            list.add(collection);
            WLJSONStore.getInstance(context).openCollections(list);
            collection.addData(new JSONObject("{'card_number': '4639123456789012', 'expiration': '07/23', 'type':'VISA'}"));
            Toast.makeText(this, "added card details to JSONStore!", Toast.LENGTH_SHORT).show();
        } catch (JSONStoreInvalidSchemaException e) {
            e.printStackTrace();
        } catch (JSONStoreSchemaMismatchException e) {
            e.printStackTrace();
        } catch (JSONStoreInvalidPasswordException e) {
            e.printStackTrace();
        } catch (JSONStoreFileAccessException e) {
            e.printStackTrace();
        } catch (JSONStoreCloseAllException e) {
            e.printStackTrace();
        } catch (JSONStoreMigrationException e) {
            e.printStackTrace();
        } catch (JSONStoreTransactionDuringInitException e) {
            e.printStackTrace();
        } catch (JSONStoreDatabaseClosedException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (JSONStoreSyncException e) {
            e.printStackTrace();
        } catch (JSONStoreAddException e) {
            e.printStackTrace();
        }
    }
}
