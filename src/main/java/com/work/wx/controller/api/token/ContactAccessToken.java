/*
 * work_wx
 * wuhen 2020/1/16.
 * Copyright (c) 2020  jianfengwuhen@126.com All Rights Reserved.
 */

package com.work.wx.controller.api.token;

import com.google.gson.Gson;
import com.work.wx.config.CustomConfig;
import com.work.wx.controller.modle.CorpModel;
import com.work.wx.controller.modle.TokenModel;
import com.work.wx.server.TokenServer;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

public class ContactAccessToken {
    private final static Logger logger = LoggerFactory.getLogger(ContactAccessToken.class);
    public static final int CONTACT_TOKEN_TYPE = 1;

    public static final String BASE_ADDRESS = "https://qyapi.weixin.qq.com/cgi-bin/gettoken";

    public String getContactAccessToken(TokenServer tokenServer, CorpModel corpModel) {
        TokenModel tokenModel = tokenServer.getTokenModel(new TokenModel(corpModel.getCorp(),CONTACT_TOKEN_TYPE));
        if (null != tokenModel) {
            if (tokenModel.getLoseTime() > System.currentTimeMillis()) {
                return tokenModel.getAccess_token();
            }
        }
        boolean status = requestContactToken(tokenServer,corpModel);
        return status ? getContactAccessToken(tokenServer,corpModel) : "";
    }



    private boolean setContactAcessToken(TokenModel queryModel,TokenModel tokenModel,TokenServer tokenServer) {
        return tokenServer.updateInsertToken(queryModel,tokenModel) > 0;
    }



    private boolean requestContactToken(TokenServer tokenServer,CorpModel corpModel) {
        String url = BASE_ADDRESS + "?" + "corpid=" + corpModel.getCorp() +"&" + "corpsecret=" + corpModel.getContactSecret();

        try {
            Response response = new OkHttpClient().newCall(new Request.Builder().url(url).get().build()).execute();
            if (response.code() == 200) {
                TokenModel tokenModel = new Gson().fromJson(response.body().string(), TokenModel.class);
                if (tokenModel.getErrcode() == 0) {
                    logger.debug(tokenModel.toString());
                    tokenModel.setLoseTime(System.currentTimeMillis() + tokenModel.getExpires_in() * 1000);
                    tokenModel.setCorpId(corpModel.getCorp());
                    tokenModel.setToken_type(CONTACT_TOKEN_TYPE);
                    setContactAcessToken(new TokenModel(corpModel.getCorp(),CONTACT_TOKEN_TYPE),tokenModel,tokenServer);
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }




}
