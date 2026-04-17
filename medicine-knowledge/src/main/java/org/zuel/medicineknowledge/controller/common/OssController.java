package org.zuel.medicineknowledge.controller.common;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.zuel.medicineknowledge.common.BaseResponse;
import org.zuel.medicineknowledge.common.ResultUtils;
import org.zuel.medicineknowledge.service.OssService;

import javax.annotation.Resource;

@RestController
@Api(tags = "阿里云文件管理", description = "提供文件上传的接口")
@RequestMapping("/fileOss")
public class OssController {

    @Resource
    private OssService ossService;

    @ApiOperation(value = "文件上传")
    @PostMapping("/upload")
    public BaseResponse<String> uploadOssFile(@RequestParam(required = true) MultipartFile file) {
        //获取上传的文件
        if (file.isEmpty()) {
            return null;
        }
        //返回上传到oss的路径
        String url = ossService.uploadFileAvatar(file);
        //返回r对象
        return ResultUtils.success(url);
    }

}
