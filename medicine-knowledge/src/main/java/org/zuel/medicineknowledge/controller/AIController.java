package org.zuel.medicineknowledge.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.zuel.medicineknowledge.common.BaseResponse;
import org.zuel.medicineknowledge.common.ResultUtils;
import org.zuel.medicineknowledge.utils.AIUtils;

@RestController
@RequestMapping("/ai")
@Slf4j
@Api(tags = "大模型API", description = "提供大模型相关的接口")
public class AIController {

    @GetMapping("/question")
    @ApiOperation("获取大模型问答")
    public BaseResponse<String> getAnswer(@RequestParam(required = true) String question){
        String quicked;
        String prompt = "作为一个训练有素的医疗顾问，请基于当前临床实践和研究，针对患者提出的特定健康问题，提供详细、准确且实用的医疗建议。请同时考虑可能的病因、诊断流程、治疗方案以及预防措施，并给出在不同情境下的应对策略。对于药物治疗，请特别指明适用的药品名称、剂量和疗程。如果需要进一步的检查或就医，也请明确指示。";
        try {
            quicked = AIUtils.quickStart(question,prompt);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return ResultUtils.success(quicked);
    }

}
