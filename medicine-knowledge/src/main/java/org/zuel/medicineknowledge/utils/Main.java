package org.zuel.medicineknowledge.utils;// Copyright (c) Alibaba, Inc. and its affiliates.

import com.alibaba.dashscope.aigc.conversation.Conversation;
import com.alibaba.dashscope.aigc.conversation.ConversationParam;
import com.alibaba.dashscope.aigc.conversation.ConversationResult;
import com.alibaba.dashscope.exception.ApiException;
import com.alibaba.dashscope.exception.InputRequiredException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import com.alibaba.dashscope.utils.Constants;
import io.reactivex.Flowable;
import com.alibaba.dashscope.utils.JsonUtils;
import io.github.cdimascio.dotenv.Dotenv;

public class Main {
    public static void testStreamCall() throws ApiException, NoApiKeyException {
        Conversation conversation = new Conversation();
        String prompt = "用萝卜、土豆、茄子做饭，给我个菜谱。";
        ConversationParam param = ConversationParam
                .builder()
                .model(Conversation.Models.QWEN_TURBO)
                .prompt(prompt)
                .build();
        try{
            Flowable<ConversationResult> result = conversation.streamCall(param);
            result.blockingForEach(msg->{
                System.out.print(msg.getOutput());
//                Syst;
            });
        }catch(ApiException ex){
            System.out.println(ex.getMessage());
        } catch (InputRequiredException e) {
            throw new RuntimeException(e);
        }
    }
    public static void main(String[] args){
        Constants.apiKey=Dotenv.load().get("DASHSCOPE_API_KEY");

        try {
            testStreamCall();
        } catch (ApiException | NoApiKeyException e) {
            System.out.println(e.getMessage());
        }
        System.exit(0);
    }
}