package org.zuel.medicineknowledge.controller;

import com.alibaba.dashscope.aigc.generation.Generation;
import com.alibaba.dashscope.aigc.generation.GenerationParam;
import com.alibaba.dashscope.aigc.generation.GenerationResult;
import com.alibaba.dashscope.common.Message;
import com.alibaba.dashscope.common.MessageManager;
import com.alibaba.dashscope.common.Role;
import com.alibaba.dashscope.exception.InputRequiredException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import com.alibaba.dashscope.utils.Constants;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.zuel.medicineknowledge.common.ErrorCode;
import org.zuel.medicineknowledge.exception.BusinessException;
import org.zuel.medicineknowledge.utils.Main;
import reactor.core.publisher.Flux;

import java.io.IOException;
import io.github.cdimascio.dotenv.Dotenv;


@RestController
public class SSEController {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    private String prompt = "作为一个训练有素的医疗顾问，请基于当前临床实践和研究，针对患者提出的特定健康问题，提供详细、准确且实用的医疗建议。请同时考虑可能的病因、诊断流程、治疗方案以及预防措施，并给出在不同情境下的应对策略。对于药物治疗，请特别指明适用的药品名称、剂量和疗程。如果需要进一步的检查或就医，也请明确指示。";

    @GetMapping(value = "/streamGeneration", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamGeneration(String question) {
        if (StringUtils.isBlank(question)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"问题不能为空");
        }
        Message userMsg = null;
        Constants.apiKey=Dotenv.load().get("DASHSCOPE_API_KEY");
        if (Constants.apiKey == null) {
            throw new RuntimeException("DASHSCOPE_API_KEY is not set");
        }
        Generation gen = new Generation();
        userMsg = Message.builder().role(Role.USER.getValue()).content(question).build();
        Message systemMsg =
                Message.builder().role(Role.SYSTEM.getValue()).content(prompt).build();
        SseEmitter emitter = new SseEmitter(); // 设置超时时间
        Flux<GenerationResult> streamFlux = null;
        try {
            streamFlux = streamCallWithFlux(gen,userMsg,systemMsg);
        } catch (NoApiKeyException e) {
            throw new RuntimeException(e);
        } catch (InputRequiredException e) {
            throw new RuntimeException(e);
        }

        // 订阅Flux并发送SSE事件
        streamFlux.subscribe(
                generationResult -> {
                    try {
                        String content = handleGenerationResult(generationResult);
                        emitter.send(SseEmitter.event().data(content));
                    } catch (IOException e) {
                        emitter.completeWithError(e);
                    }
                },
                emitter::completeWithError,
                () -> {
                    try {
                        emitter.complete();
                    } catch (Exception e) {
                        System.out.println(e);
                    }
                }
        );

        return emitter;
//        return null;
    }


    private static GenerationParam buildGenerationParam(Message userMsg,Message systemMsg) {
        MessageManager msgManager = new MessageManager(10);
        msgManager.add(systemMsg);
        msgManager.add(userMsg);
        return GenerationParam.builder()
                .model("qwen-turbo")
                .messages(msgManager.get())
                .resultFormat(GenerationParam.ResultFormat.MESSAGE)
                .topP(0.8)
                .incrementalOutput(true)
                .build();
    }

    private Flux<GenerationResult> streamCallWithFlux(Generation gen,Message userMsg,Message systemMsg) throws NoApiKeyException, InputRequiredException {
        GenerationParam param = buildGenerationParam(userMsg,systemMsg);
        // 假设gen.streamCall返回一个Flowable，我们需要将其转换为Flux  
        return Flux.from(gen.streamCall(param));
    }

    private String handleGenerationResult(GenerationResult message) {
        // 处理GenerationResult并返回要发送的内容
        System.out.println(message);
        return message.getOutput().getChoices().get(0).getMessage().getContent();
    }


}