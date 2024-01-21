package com.payubiz;

import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.WebView;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.ReadableMapKeySetIterator;
import com.facebook.react.bridge.UiThreadUtil;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.payu.base.models.ErrorResponse;
import com.payu.base.models.OrderDetails;
import com.payu.base.models.PayUBillingCycle;
import com.payu.base.models.PayUPaymentParams;
import com.payu.base.models.PayUSIParams;
import com.payu.base.models.PaymentMode;
import com.payu.base.models.PaymentType;
import com.payu.checkoutpro.PayUCheckoutPro;
import com.payu.checkoutpro.models.PayUCheckoutProConfig;
import com.payu.checkoutpro.utils.PayUCheckoutProConstants;
import com.payubiz.PayUBizConstants;
import com.payu.ui.model.listeners.PayUCheckoutProListener;
import com.payu.ui.model.listeners.PayUHashGenerationListener;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import android.os.Handler;


public class PayUBizSdkModule extends ReactContextBaseJavaModule implements PayUCheckoutProListener {

    private final ReactApplicationContext reactContext;
    private PayUHashGenerationListener listener;

    public PayUBizSdkModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
    }

    @Override
    public String getName() {
        return PayUBizConstants.PAYUBIZ_SDK;
    }

    @ReactMethod
    public void openCheckoutScreen(ReadableMap paymentParams) {
        if (reactContext.getCurrentActivity() != null) {
            if (paymentParams.hasKey(PayUBizConstants.PAYU_PAYMENT_PARAMS)) {
                PayUPaymentParams payUPaymentParams = parsePaymentParams(paymentParams.getMap(PayUBizConstants.PAYU_PAYMENT_PARAMS));
                if (paymentParams.hasKey(PayUBizConstants.PAYU_CHECKOUT_PRO_CONFIG)) {
                    PayUCheckoutPro.open(
                            reactContext.getCurrentActivity(),
                            payUPaymentParams,
                            getConfig(paymentParams.getMap(PayUBizConstants.PAYU_CHECKOUT_PRO_CONFIG)),
                            this);
                } else {
                    PayUCheckoutPro.open(
                            reactContext.getCurrentActivity(),
                            payUPaymentParams,
                            this);
                }
            } else {
                throw new NullPointerException("PayUPaymentParams cannot be null");
            }
        } else {
            throw new NullPointerException("ReactContext cannot run with Activity null");
        }
//        Intent intent = new Intent(reactContext, PayUBaseActivity.class);
//        intent.putExtra(PayUBizConstants.PAYU_HASHES, parseHashes(payUHashes));
//        intent.putExtra(PayUBizConstants.PAYMENT_PARAMS, parsePaymentParams(paymentParams,payUHashes));
//        intent.putExtra(PayUBizConstants.PAYU_CONFIG, parsePayuConfig(paymentParams));
//        intent.putExtra("cb_config", getCbConfig(paymentParams));
//        intent.putExtra(SdkUIConstants.VERSION_KEY, BuildConfig.VERSION_NAME);
//        intent.putExtra("review_order", getReviewOrderBundle(paymentParams).getReviewOrderDatas());
//        reactContext.startActivityForResult(intent, 101, null);
//        reactContext.addActivityEventListener(this);
    }

//    @Override
//    public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {
//
//        if (resultCode == Activity.RESULT_OK) {
//            WritableMap map = new WritableNativeMap();
//            map.putString("result", "success");
//            map.putString("payu_response", data.getStringExtra("payu_response"));
//            map.putString("merchant_response", data.getStringExtra("result"));
//            promise.resolve(map);
//        } else {
//            if (null != data) {
//                WritableMap map = new WritableNativeMap();
//                map.putString("result", "failure");
//                map.putString("payu_response", data.getStringExtra("payu_response"));
//                map.putString("merchant_response", data.getStringExtra("result"));
//                promise.resolve(map);
//            } else {
//                promise.reject("101", "Error in transaction");
//            }
//
//        }
//    }

//    @Override
//    public void onNewIntent(Intent intent) {
//
//    }

//    private PayuConfig parsePayuConfig(ReadableMap map) {
//        PayuConfig payuConfig = new PayuConfig();
//        payuConfig.setEnvironment(Integer.parseInt(map.getString(PayUBizConstants.ENV)));
//        return payuConfig;
//    }

    /**
     * Parser of PayUPaymentParams
     * @param paymentParamMap
     * @return PayUPaymentParams object
     */
    private PayUPaymentParams parsePaymentParams(ReadableMap paymentParamMap) {
        PayUPaymentParams.Builder params = new PayUPaymentParams.Builder();
        params.setAmount(paymentParamMap.getString(PayUBizConstants.AMOUNT));
        params.setTransactionId(paymentParamMap.getString(PayUBizConstants.TRANSACTION_ID));
        params.setKey(paymentParamMap.getString(PayUBizConstants.KEY));
        params.setProductInfo(paymentParamMap.getString(PayUBizConstants.PRODUCT_INFO));
        params.setSurl(paymentParamMap.getString(PayUBizConstants.ANDROID_SURL));
        params.setFurl(paymentParamMap.getString(PayUBizConstants.ANDROID_FURL));
        params.setIsProduction(Integer.parseInt(paymentParamMap.getString(PayUBizConstants.ENVIRONMENT)) == 0);
        if (paymentParamMap.hasKey(PayUBizConstants.ADDITION_PARAM)) {
            ReadableMap additionalParamMap = paymentParamMap.getMap(PayUBizConstants.ADDITION_PARAM);
            HashMap<String, Object> additionalParams = new HashMap<>();
            if(additionalParamMap.hasKey(PayUCheckoutProConstants.CP_UDF1))
                additionalParams.put(PayUCheckoutProConstants.CP_UDF1, additionalParamMap.getDynamic(PayUBizConstants.UDF1).asString());
            if(additionalParamMap.hasKey(PayUCheckoutProConstants.CP_UDF2))
                additionalParams.put(PayUCheckoutProConstants.CP_UDF2, additionalParamMap.getDynamic(PayUBizConstants.UDF2).asString());
            if(additionalParamMap.hasKey(PayUCheckoutProConstants.CP_UDF3))
                additionalParams.put(PayUCheckoutProConstants.CP_UDF3, additionalParamMap.getDynamic(PayUBizConstants.UDF3).asString());
            if(additionalParamMap.hasKey(PayUCheckoutProConstants.CP_UDF4))
                additionalParams.put(PayUCheckoutProConstants.CP_UDF4, additionalParamMap.getDynamic(PayUBizConstants.UDF4).asString());
            if(additionalParamMap.hasKey(PayUCheckoutProConstants.CP_UDF5))
                additionalParams.put(PayUCheckoutProConstants.CP_UDF5, additionalParamMap.getDynamic(PayUBizConstants.UDF5).asString());
            if (additionalParamMap.hasKey(PayUBizConstants.VAS_FOR_MOBILE_SDK))
                additionalParams.put(PayUCheckoutProConstants.CP_VAS_FOR_MOBILE_SDK, additionalParamMap.getString(PayUBizConstants.VAS_FOR_MOBILE_SDK));
            if (additionalParamMap.hasKey(PayUBizConstants.PAYMENT))
                additionalParams.put(PayUBizConstants.PAYMENT_SOURCE, additionalParamMap.getString(PayUBizConstants.PAYMENT));
            if (additionalParamMap.hasKey(PayUBizConstants.PAYMENT_RELATED_DETAILS_FOR_MOBILE_SDK))
                additionalParams.put(PayUCheckoutProConstants.CP_PAYMENT_RELATED_DETAILS_FOR_MOBILE_SDK, additionalParamMap.getString(PayUBizConstants.PAYMENT_RELATED_DETAILS_FOR_MOBILE_SDK));
            if (additionalParamMap.hasKey(PayUBizConstants.WALLET_URN))
                additionalParams.put(PayUCheckoutProConstants.WALLET_URN, additionalParamMap.getString(PayUBizConstants.WALLET_URN));
            additionalParams.put(PayUCheckoutProConstants.CP_ANALYTICS_DATA,addAnalytics());
            params.setAdditionalParams(additionalParams);
        }else {
            HashMap<String, Object> additionalParams = new HashMap<>();
            additionalParams.put(PayUCheckoutProConstants.CP_ANALYTICS_DATA,addAnalytics());
            params.setAdditionalParams(additionalParams);
        }
        if(paymentParamMap.hasKey(PayUBizConstants.PAYU_SI_PARAMS)){
            params.setPayUSIParams(getSIParams(paymentParamMap.getMap(PayUBizConstants.PAYU_SI_PARAMS)));
        }
        params.setEmail(paymentParamMap.getString(PayUBizConstants.EMAIL));
        params.setPhone(paymentParamMap.getString(PayUBizConstants.PHONE));
        params.setFirstName(paymentParamMap.getString(PayUBizConstants.FIRST_NAME));
        params.setUserCredential(paymentParamMap.getString(PayUBizConstants.USER_CREDENTIALS));
        return params.build();
    }

    /**
     * Parser of PayUCheckoutProConfig
     * @param map
     * @return PayUCheckoutProConfig object
     */
    private PayUCheckoutProConfig getConfig(ReadableMap map) {
        PayUCheckoutProConfig config = new PayUCheckoutProConfig();
        if (map.hasKey(PayUBizConstants.MERCHANT_SMS_PERMISSION))
            config.setMerchantSmsPermission(map.getBoolean(PayUBizConstants.MERCHANT_SMS_PERMISSION));
        if (map.hasKey(PayUBizConstants.SURE_PAY_COUNT))
            config.setSurePayCount(map.getInt(PayUBizConstants.SURE_PAY_COUNT));
        if (map.hasKey(PayUBizConstants.AUTO_APPROVE))
            config.setAutoApprove(map.getBoolean(PayUBizConstants.AUTO_APPROVE));
        if (map.hasKey(PayUBizConstants.MERCHANT_RESPONSE_TIMEOUT))
            config.setMerchantResponseTimeout(map.getInt(PayUBizConstants.MERCHANT_RESPONSE_TIMEOUT));
        if (map.hasKey(PayUBizConstants.MERCHANT_NAME))
            config.setMerchantName(map.getString(PayUBizConstants.MERCHANT_NAME));
        if (map.hasKey(PayUBizConstants.AUTO_SELECT_OTP))
            config.setAutoSelectOtp(map.getBoolean(PayUBizConstants.AUTO_SELECT_OTP));
        if (map.hasKey(PayUBizConstants.SHOW_CB_TOOL_BAR))
            config.setShowCbToolbar(map.getBoolean(PayUBizConstants.SHOW_CB_TOOL_BAR));
        if (map.hasKey(PayUBizConstants.SHOW_EXIT_CONFIRMATION_ON_CHECKOUT_SCREEN))
            config.setShowExitConfirmationOnCheckoutScreen(map.getBoolean(PayUBizConstants.SHOW_EXIT_CONFIRMATION_ON_CHECKOUT_SCREEN));
        if (map.hasKey(PayUBizConstants.SHOW_EXIT_CONFIRMATION_ON_PAYMENT_SCREEN))
            config.setShowExitConfirmationOnPaymentScreen(map.getBoolean(PayUBizConstants.SHOW_EXIT_CONFIRMATION_ON_PAYMENT_SCREEN));
        if (map.hasKey(PayUBizConstants.CARD_DETAILS))
            config.setCartDetails(getCartDetails(map.getArray(PayUBizConstants.CARD_DETAILS)));
        if (map.hasKey(PayUBizConstants.PAYMENT_MODES_ORDER))
            config.setPaymentModesOrder(getPaymentOrder(map.getArray(PayUBizConstants.PAYMENT_MODES_ORDER)));
        if(map.hasKey(PayUBizConstants.MERCHANT_LOGO)){
            try{
                int resId = reactContext.getResources().getIdentifier(map.getString(PayUBizConstants.MERCHANT_LOGO),"drawable",reactContext.getPackageName());
                Drawable drawable = reactContext.getResources().getDrawable(resId);
                if(null!=drawable)
                    config.setMerchantLogo(resId);
            }catch (Resources.NotFoundException e){
                Log.e("PayU",e.getLocalizedMessage());            }
        }
        if ( map.hasKey(PayUBizConstants.ENFORCEDLIST) )
            config.setEnforcePaymentList(getEnforcePaymentList(Objects.requireNonNull(map.getArray(PayUBizConstants.ENFORCEDLIST))));

//            config.setMerchantLogo(reactContext.getResources().getIdentifier(map.getString("merchantLogo"),"drawable",reactContext.getPackageName()));
        return config;
    }

    /**
     * Utility method to return {@link com.payu.base.models.PayUSIParams}
     * @param map Map containing object of PayUSIParams
     * @return {@link com.payu.base.models.PayUSIParams}
     */
    private PayUSIParams getSIParams(ReadableMap map){
        PayUSIParams.Builder siParamBuilder = new PayUSIParams.Builder();

        try {
            if (map.hasKey(PayUBizConstants.IS_FREE_TRIAL)) {
                siParamBuilder.setIsFreeTrial(map.getBoolean(PayUBizConstants.IS_FREE_TRIAL));
            }
            if (map.hasKey(PayUBizConstants.BILLING_AMOUNT)) {
                siParamBuilder.setBillingAmount(Objects.requireNonNull(map.getString(PayUBizConstants.BILLING_AMOUNT)));
            }
            if (map.hasKey(PayUBizConstants.BILLING_INTERVAL)) {
                siParamBuilder.setBillingInterval(map.getInt(PayUBizConstants.BILLING_INTERVAL));
            }
            if (map.hasKey(PayUBizConstants.BILLING_START_DATE)) { //In format "2021-10-15"
                siParamBuilder.setPaymentStartDate(Objects.requireNonNull(map.getString(PayUBizConstants.BILLING_START_DATE)));
            }
            if (map.hasKey(PayUBizConstants.BILLING_END_DATE)) {//In format "2021-10-15"
                siParamBuilder.setPaymentEndDate(Objects.requireNonNull(map.getString(PayUBizConstants.BILLING_END_DATE)));
            }
            if (map.hasKey(PayUBizConstants.BILLING_CYCLE)) {//Can be "daily","monthly","weekly","yearly","once","adhoc"
                siParamBuilder.setBillingCycle(Objects.requireNonNull(getBillingCycle(Objects.requireNonNull(map.getString(PayUBizConstants.BILLING_CYCLE)))));
            }
            if (map.hasKey(PayUBizConstants.BILLING_REMARKS)) {
                siParamBuilder.setRemarks(Objects.requireNonNull(map.getString(PayUBizConstants.BILLING_REMARKS)));
            }
            if (map.hasKey(PayUBizConstants.BILLING_CURRENCY)) {
                siParamBuilder.setBillingCurrency(Objects.requireNonNull(map.getString(PayUBizConstants.BILLING_CURRENCY)));
            }
        }catch (NullPointerException e){
            e.printStackTrace();
        }
        return siParamBuilder.build();
    }

    /**
     * Utility method to convert billingCycle value to {@link com.payu.base.models.PayUBillingCycle}
     * @param billingCycle billingcycle value can be any of ("daily","weekly","monthly","yearly","once","adhoc")
     * @return {@link com.payu.base.models.PayUBillingCycle} object
     */
    private PayUBillingCycle getBillingCycle(String billingCycle){
        switch (billingCycle.toLowerCase()){
            case "daily":return PayUBillingCycle.DAILY;
            case "weekly": return PayUBillingCycle.WEEKLY;
            case "once": return PayUBillingCycle.ONCE;
            case "yearly": return PayUBillingCycle.YEARLY;
            case "adhoc": return PayUBillingCycle.ADHOC;
            case "monthly": return PayUBillingCycle.MONTHLY;
        }
        return null;
    }
    /**
     * Convert cartDetailArray to CartDetails list
     * @param cartDetailArray cartDetailArray
     * @return ArrayList of CartDetails
     */
    private ArrayList<OrderDetails> getCartDetails(ReadableArray cartDetailArray) {
        ArrayList<OrderDetails> resultList = new ArrayList<>(cartDetailArray.size());
        for(int i=0;i<cartDetailArray.size();i++){
            ReadableMap reviewOrderMap = cartDetailArray.getMap(i);
            ReadableMapKeySetIterator keyIterator = reviewOrderMap.keySetIterator();
            if (null != keyIterator) {
                while (keyIterator.hasNextKey()) {
                    String key = keyIterator.nextKey();
                    OrderDetails details = new OrderDetails(key, reviewOrderMap.getString(key));
                    resultList.add(details);
                }
            }
        }
        return resultList;
    }

    private ArrayList<HashMap<String, String>> getEnforcePaymentList(ReadableArray enforcelist) {
        ArrayList<HashMap<String,String>> enforceList = new ArrayList();
        for(int i=0;i<enforcelist.size();i++){
            ReadableMap enforcemap = enforcelist.getMap(i);
            ReadableMapKeySetIterator keyIterator = enforcemap.keySetIterator();
            if ( keyIterator!=null ){
                while (keyIterator.hasNextKey()) {
                    String key = keyIterator.nextKey();
                    HashMap<String,String> map1 = new HashMap<>();
                    map1.put(key,enforcemap.getString(key));
                       enforceList.add(map1)    ;
                }
            }
        }
        return enforceList;
    }

    /**
     * Convert paymentOrderArray to PaymentMode list
     * @param paymentOrderArray paymentOrderArray
     * @return List of PaymentMode
     */
    private ArrayList<PaymentMode> getPaymentOrder(ReadableArray paymentOrderArray) {
        ArrayList<PaymentMode> resultList = new ArrayList<>(paymentOrderArray.size());
        for(int i=0;i<paymentOrderArray.size();i++){
            ReadableMap reviewOrderMap = paymentOrderArray.getMap(i);
            ReadableMapKeySetIterator keyIterator = reviewOrderMap.keySetIterator();
            if (null != keyIterator) {
                while (keyIterator.hasNextKey()) {
                    String key = keyIterator.nextKey();
                    PaymentType type = getValidPaymentType(key);
                    PaymentMode mode = null;
                    if(null!=type){
                        String bankCode = reviewOrderMap.getString(key);
                        if(TextUtils.isEmpty(bankCode)){
                            mode = new PaymentMode(type);
                        }else {
                            mode = new PaymentMode(type, getValidPaymentCode(bankCode));
                        }
                        resultList.add(mode);
                    }else {
                        throw new RuntimeException(PayUBizConstants.TYPE_SHOULD_BE_ONLY_NET_BANKING_UPI_WALLETS);
                    }
                }
            }
        }
        return resultList;
    }

    /**
     * Convert paymentType String to valid PaymentType
     * @param typeValue
     * @return PaymentType if valid else null
     */
    private PaymentType getValidPaymentType(String typeValue){
        switch (typeValue){
            case  "Net Banking":return PaymentType.NB;
            case "UPI":return PaymentType.UPI;
            case "Wallets":return PaymentType.WALLET;
            case "EMI":return PaymentType.EMI;
            case "Cards":return PaymentType.CARD;
        }
        return null;
    }
    private String getValidPaymentCode(String typeValue){
        switch (typeValue){
            case  "TEZ": return PayUCheckoutProConstants.CP_GOOGLE_PAY;
            case  "PAYTM": return PayUCheckoutProConstants.CP_PAYTM;
            case  "PHONEPE": return PayUCheckoutProConstants.CP_PHONEPE;
        }
        return typeValue;
    }


    @Override
    public void generateHash(@NotNull HashMap<String, String> hashMap, @NotNull PayUHashGenerationListener payUHashGenerationListener) {
        this.listener = payUHashGenerationListener;
        WritableMap map = new WritableNativeMap();
        for (Map.Entry<String, String> entry : hashMap.entrySet()) {
            map.putString(entry.getKey(), entry.getValue());
        }
        sendResultBack(PayUBizConstants.GENERATE_HASH, map);
    }

    @ReactMethod
    public void hashGenerated(final ReadableMap map) {
        UiThreadUtil.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                HashMap<String, String> valueMap = new HashMap<>();
                ReadableMapKeySetIterator iterator = map.keySetIterator();
                while (iterator.hasNextKey()) {
                    String key = iterator.nextKey();
                    valueMap.put(key, map.getString(key));
                }

                listener.onHashGenerated(valueMap);
            }
        });

    }

    @Override
    public void onError(@NotNull ErrorResponse errorResponse) {
        WritableMap map = new WritableNativeMap();
        map.putString(PayUBizConstants.ERROR_MSG,errorResponse.getErrorMessage());
        map.putString(PayUBizConstants.ERROR_CODE,errorResponse.getErrorCode()+"");
        sendResultBack(PayUBizConstants.ON_ERROR, map);
    }

    @Override
    public void onPaymentCancel(boolean b) {
        WritableMap map = new WritableNativeMap();
        map.putBoolean(PayUBizConstants.IS_TXN_INITIATED, b);
        sendResultBack(PayUBizConstants.ON_PAYMENT_CANCEL, map);
    }

    @Override
    public void onPaymentFailure(@NotNull Object o) {
        HashMap<String, String> result = (HashMap<String, String>) o;
        WritableMap map = new WritableNativeMap();
        map.putString(PayUCheckoutProConstants.CP_MERCHANT_RESPONSE, result.get(PayUCheckoutProConstants.CP_MERCHANT_RESPONSE));
        map.putString(PayUCheckoutProConstants.CP_PAYU_RESPONSE, result.get(PayUCheckoutProConstants.CP_PAYU_RESPONSE));
        sendResultBack(PayUBizConstants.ON_PAYMENT_FAILURE, map);
    }

    @Override
    public void onPaymentSuccess(@NotNull Object o) {
        HashMap<String, String> result = (HashMap<String, String>) o;
        WritableMap map = new WritableNativeMap();
        map.putString(PayUCheckoutProConstants.CP_MERCHANT_RESPONSE, result.get(PayUCheckoutProConstants.CP_MERCHANT_RESPONSE));
        map.putString(PayUCheckoutProConstants.CP_PAYU_RESPONSE, result.get(PayUCheckoutProConstants.CP_PAYU_RESPONSE));
        sendResultBack(PayUBizConstants.ON_PAYMENT_SUCCESS, map);
    }

    private void sendResultBack(String eventName, WritableMap params) {
        Handler handler = new Handler(reactContext.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                PayUBizSdkModule.this.reactContext
                        .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                        .emit(eventName, params);
            }
        });
    }

    private String addAnalytics(){
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(PayUBizConstants.NAME_KEY, "react");
            jsonObject.put(PayUBizConstants.PLATFORM_KEY, PayUBizConstants.PLATFORM_VALUE);
            jsonObject.put(PayUBizConstants.VERSION_KEY, BuildConfig.VERSION_NAME);
        }catch (JSONException e){
            e.printStackTrace();
        }
        return jsonObject.toString();
    }

    @Override
    public void setWebViewProperties(@Nullable WebView webView, @Nullable Object o) {

    }

    /**
     * Convert payment types array string values to ArrayList
     * @param paymentTypesArray
     * @return List of {@link com.payu.base.models.PaymentType}
     */
    private ArrayList<PaymentType> getPaymentTypesList(ReadableArray paymentTypesArray){
        ArrayList<PaymentType> result = new ArrayList<>();
        for(int i=0;i<paymentTypesArray.size();i++){
            PaymentType paymentType = getValidPaymentType(Objects.requireNonNull(paymentTypesArray.getString(i)));
            result.add(paymentType);
        }
        return result;
    }
}
