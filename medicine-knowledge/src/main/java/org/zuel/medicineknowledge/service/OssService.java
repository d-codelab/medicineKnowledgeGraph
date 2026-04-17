package org.zuel.medicineknowledge.service;

import org.springframework.web.multipart.MultipartFile;

public interface OssService {
    //上传图片到OSS
    String uploadFileAvatar(MultipartFile file);

}