//AndroidBridgeModule.java
package com.demo1;

import android.content.Intent;
import android.widget.Toast;

import com.demo1.PlayerActivity;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.logituit.player.ui.LivePlayerActivity;

class AndroidBridgeModule extends ReactContextBaseJavaModule {
    ReactApplicationContext reactContext;
    AndroidBridgeModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
    }

    @Override
    public String getName() {
        return "AndroidBridge";
    }

    @ReactMethod
    void navigateToAndroidNativeActivity(String text) {

        ReactApplicationContext context = getReactApplicationContext();
        Intent intent = new Intent(context, PlayerActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }
    @ReactMethod
    void navigateToLivePlayer(String text) {

        ReactApplicationContext context = getReactApplicationContext();
        Intent intent = new Intent(context, LivePlayerActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

 }