package com.u8.server.sdk.ysdk;

import com.u8.server.data.UChannel;
import com.u8.server.data.UOrder;
import com.u8.server.data.UUser;
import com.u8.server.sdk.*;
import com.u8.server.utils.EncryptUtils;
import net.sf.json.JSONObject;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * 腾讯应用宝YSDK
 * Created by ant on 2016/5/9.
 */
public class SingleYSDK implements ISDKScript{

    private static Logger log = Logger.getLogger(SingleYSDK.class.getName());

    private static final String QQ_AUTH_PATH = "/auth/qq_check_token";
    private static final String WX_AUTH_PATH = "/auth/wx_check_token";

    @Override
    public void verify(final UChannel channel, String extension, final ISDKVerifyListener callback) {

        try{

            JSONObject json = JSONObject.fromObject(extension);
            final String openid = json.getString("openid");
            final String openkey = json.getString("openkey");

            final long time = System.currentTimeMillis()/1000;
            final int type = json.getInt("accountType");


            Map<String,String> params = new HashMap<String, String>();
            params.put("appid", channel.getCpAppID());
            params.put("openid", openid);
            params.put("openkey", openkey);
            params.put("timestamp", time+"");


            String fullUrl = channel.getChannelAuthUrl() + QQ_AUTH_PATH;
            String key = channel.getCpAppKey();

            if(type == 1){
                fullUrl = channel.getChannelAuthUrl() + WX_AUTH_PATH;
                key = channel.getCpAppSecret();
            }
            params.put("sig", EncryptUtils.md5(key+time));
            log.info("------------>SingleYSDK verify params:" + params.toString()+", url:"+fullUrl);

            UHttpAgent.getInstance().post(fullUrl, params, new UHttpFutureCallback() {
                @Override
                public void completed(String result) {

                    try {

                        JSONObject json = JSONObject.fromObject(result);

                        log.info("------------>SingleYSDK the verify result is "+result);

                        if (json.containsKey("ret") && json.getInt("ret") == 0) {

                            String accountType = "qq";

                            if(type ==2){
                                accountType = "wx";
                            }

                            log.info("------------>SingleYSDK the verify result is "+result);
                            callback.onSuccess(new SDKVerifyResult(true, openid, accountType+"-"+openid, ""));

                            return;
                        }


                    } catch (Exception e) {
                        log.error("------------>SingleYSDK the verify exception,result is "+e.getMessage());
                        e.printStackTrace();
                    }
                    log.error("------------>SingleYSDK the verify fail,result is "+result);
                    callback.onFailed(channel.getMaster().getSdkName() + " verify failed. the result is " + result);
                }

                @Override
                public void failed(String e) {
                    log.error("------------>SingleYSDK the verify callback fail,result is "+e);
                    callback.onFailed(channel.getMaster().getSdkName() + " verify failed. " + e);
                }

            });

        }catch(Exception e){
            e.printStackTrace();
            log.error("------------>SingleYSDK the verify total exception,result is "+e.getMessage());
            callback.onFailed(channel.getMaster().getSdkName() + " verify execute failed. the exception is "+e.getMessage());
        }

    }

    @Override
    public void onGetOrderID(UUser user, UOrder order, ISDKOrderListener callback) {
        log.error("------------>SingleYSDK the Order is "+order.toJSON()+", user="+user.toJSON());
        String isYsdk = "ysdk".equals(order.getChannel().getMaster().getSdkName())?"1":"0";
        JSONObject json = new JSONObject();
        json.put("isYsdk", isYsdk);
        if(callback != null){
            callback.onSuccess(json.toString());
        }
    }


}
