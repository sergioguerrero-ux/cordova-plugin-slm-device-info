package com.slm.deviceinfo;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.BatteryManager;
import android.os.Build;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.Log;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class SLMDeviceInfo extends CordovaPlugin {

    private static final String TAG = "SLMDeviceInfo";

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        switch (action) {
            case "getDeviceInfo":
                getDeviceInfo(callbackContext);
                return true;
            case "getBatteryInfo":
                getBatteryInfo(callbackContext);
                return true;
            case "getNetworkInfo":
                getNetworkInfo(callbackContext);
                return true;
            default:
                return false;
        }
    }

    // ============================================
    // getDeviceInfo
    // ============================================

    private void getDeviceInfo(CallbackContext callbackContext) {
        cordova.getThreadPool().execute(() -> {
            try {
                Context context = cordova.getActivity().getApplicationContext();
                DisplayMetrics metrics = new DisplayMetrics();
                cordova.getActivity().getWindowManager().getDefaultDisplay().getRealMetrics(metrics);

                JSONObject info = new JSONObject();
                info.put("uuid", Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID));
                info.put("model", Build.MODEL);
                info.put("manufacturer", Build.MANUFACTURER);
                info.put("platform", "Android");
                info.put("osVersion", Build.VERSION.RELEASE);
                info.put("sdkVersion", Build.VERSION.SDK_INT);
                info.put("deviceName", Build.DEVICE);
                info.put("brand", Build.BRAND);
                info.put("product", Build.PRODUCT);
                info.put("hardware", Build.HARDWARE);
                info.put("isPhysicalDevice", !isEmulator());
                info.put("screenWidth", metrics.widthPixels);
                info.put("screenHeight", metrics.heightPixels);
                info.put("screenScale", metrics.density);
                info.put("totalMemory", Runtime.getRuntime().maxMemory() / (1024 * 1024));
                info.put("processorCount", Runtime.getRuntime().availableProcessors());

                callbackContext.success(info);
            } catch (JSONException e) {
                Log.e(TAG, "getDeviceInfo error: " + e.getMessage());
                callbackContext.error("Error obteniendo info del dispositivo: " + e.getMessage());
            }
        });
    }

    // ============================================
    // getBatteryInfo
    // ============================================

    private void getBatteryInfo(CallbackContext callbackContext) {
        try {
            Context context = cordova.getActivity().getApplicationContext();
            IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
            Intent batteryStatus = context.registerReceiver(null, filter);

            JSONObject info = new JSONObject();

            if (batteryStatus != null) {
                int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
                int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);

                float batteryLevel = (level >= 0 && scale > 0) ? (float) level / (float) scale : -1f;
                boolean isCharging = (status == BatteryManager.BATTERY_STATUS_CHARGING
                        || status == BatteryManager.BATTERY_STATUS_FULL);

                info.put("level", batteryLevel);
                info.put("isCharging", isCharging);
            } else {
                info.put("level", -1);
                info.put("isCharging", false);
            }

            callbackContext.success(info);
        } catch (JSONException e) {
            Log.e(TAG, "getBatteryInfo error: " + e.getMessage());
            callbackContext.error("Error obteniendo info de batería: " + e.getMessage());
        }
    }

    // ============================================
    // getNetworkInfo
    // ============================================

    private void getNetworkInfo(CallbackContext callbackContext) {
        try {
            Context context = cordova.getActivity().getApplicationContext();
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

            JSONObject info = new JSONObject();
            String connectionType = "none";
            boolean isConnected = false;

            if (cm != null) {
                Network activeNetwork = cm.getActiveNetwork();
                if (activeNetwork != null) {
                    NetworkCapabilities caps = cm.getNetworkCapabilities(activeNetwork);
                    if (caps != null) {
                        isConnected = caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
                        if (caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                            connectionType = "wifi";
                        } else if (caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                            connectionType = "cellular";
                        } else if (caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
                            connectionType = "ethernet";
                        } else {
                            connectionType = "unknown";
                        }
                    }
                }
            }

            // Carrier name
            String carrierName = "unknown";
            TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            if (tm != null) {
                String name = tm.getNetworkOperatorName();
                if (name != null && !name.isEmpty()) {
                    carrierName = name;
                }
            }

            info.put("connectionType", connectionType);
            info.put("isConnected", isConnected);
            info.put("carrierName", carrierName);

            callbackContext.success(info);
        } catch (JSONException e) {
            Log.e(TAG, "getNetworkInfo error: " + e.getMessage());
            callbackContext.error("Error obteniendo info de red: " + e.getMessage());
        }
    }

    // ============================================
    // Helpers
    // ============================================

    private boolean isEmulator() {
        return Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.startsWith("unknown")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86")
                || Build.MANUFACTURER.contains("Genymotion")
                || (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic"))
                || "google_sdk".equals(Build.PRODUCT);
    }
}
