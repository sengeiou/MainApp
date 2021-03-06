package com.ubtechinc.alpha.im.msghandler;

import android.content.Intent;

import com.google.protobuf.ByteString;
import com.ubtech.utilcode.utils.LogUtils;
import com.ubtechinc.alpha.AlphaMessageOuterClass;
import com.ubtechinc.alpha.ReplaySpeechRecord;
import com.ubtechinc.alpha.app.AlphaApplication;
import com.ubtechinc.alpha.appmanager.model.StaticValue;
import com.ubtechinc.alpha.entity.ReplaySpeechRcord;
import com.ubtechinc.nets.im.service.RobotPhoneCommuniteProxy;
import com.ubtechinc.nets.phonerobotcommunite.ProtoBufferDispose;
import com.ubtrobot.action.ActionApi;
import com.ubtrobot.commons.Priority;
import com.ubtrobot.mini.voice.VoiceListener;
import com.ubtrobot.mini.voice.VoicePool;
import com.ubtrobot.speech.SpeechApi;

/**
 * Created by Administrator on 2017/6/3 0003.
 */

public class ReplaySpeechRecordMsgHandler implements IMsgHandler {
    private static final String TAG = "ReplaySpeechRecordMsgHandler";
    private static final int CHAT_RECORD_TYPE_MUSIC = 12;
    private static final int CHAT_RECORD_TYPE_ACTION = 9;
    private static final int CHAT_RECORD_TYPE_DANCE = 8;
    private static final int CHAT_RECORD_TYPE_STORY = 13;
    private static final int CHAT_RECORD_TYPE_TEXT = 16;

    @Override
    public void handleMsg(int requestCmdId, int responseCmdId, AlphaMessageOuterClass.AlphaMessage request, String peer) {
        ByteString requestBody = request.getBodyData();
        byte[] bodyBytes = requestBody.toByteArray();
        long requestSerial = request.getHeader().getSendSerial();

        ReplaySpeechRecord.ReplaySpeechRecordRequest speechRecordRequest = (ReplaySpeechRecord.ReplaySpeechRecordRequest) ProtoBufferDispose.unPackData(ReplaySpeechRecord.ReplaySpeechRecordRequest.class, bodyBytes);
        LogUtils.i("request body: contentLinks: " + speechRecordRequest.getContentLinks() + " ,content: " + speechRecordRequest.getContent() + " speechRecordRequest:" + speechRecordRequest.toString());

        ReplaySpeechRecord.ReplaySpeechRecordResponse.Builder responseBuilder = ReplaySpeechRecord.ReplaySpeechRecordResponse.newBuilder();

        ReplaySpeechRcord replaySpeechRcord = new ReplaySpeechRcord();
        replaySpeechRcord.setContent(speechRecordRequest.getContent());
        replaySpeechRcord.setContentLinks(speechRecordRequest.getContentLinks());
        replaySpeechRcord.setLabelId(speechRecordRequest.getLabelId());
        replaySpeechRcord.setMsgLanguage(speechRecordRequest.getMsgLanguage());
        replaySpeechRcord.setRecordId(String.valueOf(speechRecordRequest.getRecordId()));


        switch (speechRecordRequest.getLabelId()) {
            case CHAT_RECORD_TYPE_MUSIC:
                String url = speechRecordRequest.getContentLinks();
                if (url != null) {
                    //PlayerBusiness.getPlayer().play(speechRecordRequest.getContentLinks());
                    Intent intent = new Intent(StaticValue.ACTION_REPLAY_BUSINESS);
                    intent.putExtra(StaticValue.ACTION_REPLAY_BUSINESS, url);
                    AlphaApplication.getContext().sendBroadcast(intent);
                }
                break;
            case CHAT_RECORD_TYPE_ACTION:
            case CHAT_RECORD_TYPE_DANCE:
            case CHAT_RECORD_TYPE_STORY:
                String actionName = speechRecordRequest.getContent();
                if (actionName != null) {
                    ActionApi.get().playAction(actionName, null);
                }
                break;
            case CHAT_RECORD_TYPE_TEXT:
                String content = speechRecordRequest.getContent();
                if (content != null) {
                    VoicePool.get().playTTs(content, Priority.NORMAL, new VoiceListener() {
                        @Override
                        public void onCompleted() {

                        }

                        @Override
                        public void onError(int i, String s) {

                        }
                    });
                }
                break;
        }
        responseBuilder.setIsSuccess(true);


        ReplaySpeechRecord.ReplaySpeechRecordResponse responseBody = responseBuilder.build();
        RobotPhoneCommuniteProxy.getInstance().sendResponseMessage(responseCmdId, "1", requestSerial, responseBody, peer, null);
    }
}
