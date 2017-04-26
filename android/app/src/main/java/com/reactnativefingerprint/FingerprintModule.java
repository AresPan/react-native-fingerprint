package com.reactnativefingerprint;

import android.annotation.TargetApi;
import android.app.KeyguardManager;
import android.content.Context;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.CancellationSignal;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.facebook.react.uimanager.IllegalViewOperationException;

/**
 * Created by Mars on 8/24/16 14:27.
 */
public class FingerprintModule extends ReactContextBaseJavaModule implements LifecycleEventListener {

    private static final String GAT_FINGERPRINT = "GATFingerprint";

    private ReactContext mContext;
    private KeyguardManager mKeyguardManager;
    private FingerprintManager mFingerprintManager;
    private CancellationSignal mCancellationSignal;
    private boolean mSelfCancelled = true;

    public FingerprintModule(ReactApplicationContext reactContext) {
        super(reactContext);
        mContext = reactContext;
        initObject();
    }

    @Override
    public String getName() {
        return GAT_FINGERPRINT;
    }


    @TargetApi(Build.VERSION_CODES.M)
    @ReactMethod
    public void startTouch(final String eventName, String iosPrompt) {

        if (!isFingerprintAuthAvailable()) {
            touchCallback(eventName, false, 102, "not support");
            return;
        }
        mCancellationSignal = new CancellationSignal();
//        FingerprintManager.CryptoObject crypto, CancellationSignal cancel, int flags, FingerprintManager.AuthenticationCallback callback, Handler handler

//        0:成功
//        7:尝试次数过多
//        101:识别错误
//        1011:手指放置时间过短
        mFingerprintManager.authenticate(null, mCancellationSignal, 0, new FingerprintManager.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
//                    errorCode is 7 many try.
                touchCallback(eventName, false, errorCode, "" + errString);
            }

            @Override
            public void onAuthenticationHelp(int helpCode, CharSequence helpString) {
                super.onAuthenticationHelp(helpCode, helpString);
//                    1011  finger put time is short.
                touchCallback(eventName, false, helpCode, "" + helpString);
            }

            @Override
            public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                touchCallback(eventName, true, 0, "success");
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                touchCallback(eventName, false, 101, "fingerprint failed");
            }
        }, null);

    }

    /**
     * touch callback
     *
     * @param eventName
     * @param isSuccess
     * @param errorCode
     * @param errorMessage
     */
    private void touchCallback(String eventName, boolean isSuccess, int errorCode, String errorMessage) {
        WritableMap map = Arguments.createMap();
        map.putBoolean("isSuccess", isSuccess);
        map.putInt("errorCode", errorCode);
        map.putString("errorMessage", errorMessage);
        sendEvent(mContext, eventName, map);
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void initObject() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return;
        }
        mKeyguardManager = mContext.getSystemService(KeyguardManager.class);
        mFingerprintManager = mContext.getSystemService(FingerprintManager.class);
    }


    @ReactMethod
    public void isSupport(Promise promise) {
        try {
            WritableMap map = Arguments.createMap();
            map.putBoolean("isSupport", false);
//            0:支持
//            101:手机系统不支持
//            102:未设置指纹
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                //api not support. current should less than api 23.
                map.putInt("errorCode", 101);
                map.putString("errorMessage", "api not support. current is " + Build.VERSION.SDK_INT + ", should less than api 23.");
            } else {
                if (isFingerprintAuthAvailable()) {
                    map.putBoolean("isSupport", isFingerprintAuthAvailable());
                    map.putInt("errorCode", 0);
                    map.putString("errorMessage", "support");
                } else {
                    map.putInt("errorCode", 102);
                    map.putString("errorMessage", "not set up a fingerprint");
                }
            }
            promise.resolve(map);
        } catch (IllegalViewOperationException e) {
            promise.reject(e);
        }
    }

    @Override
    public void onHostResume() {

    }

    @Override
    public void onHostPause() {

    }

    @Override
    public void onHostDestroy() {
        stopListening();
    }

    @ReactMethod
    public void stopListening() {
        if (mCancellationSignal != null) {
            mSelfCancelled = true;
            mCancellationSignal.cancel();
            mCancellationSignal = null;
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    private boolean isFingerprintAuthAvailable() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return false;
        }
        return mFingerprintManager.isHardwareDetected()
                && mFingerprintManager.hasEnrolledFingerprints();
    }

    private void sendEvent(ReactContext reactContext,
                           String eventName,
                           @Nullable WritableMap params) {
        reactContext
                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit(eventName, params);
    }
}
