package com.hty.forum.controller.web;

import com.hty.forum.dto.FileDto;
import com.hty.forum.provider.OssProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartRequest;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * create by Semineces on 2020-10-08
 */
@Controller
public class FileController {

    @Autowired
    private OssProvider ossProvider;

    @ResponseBody
    @PostMapping("/question/fileupload")
    public FileDto upload(HttpServletRequest request) throws IOException {
        MultipartRequest multipartRequest = (MultipartRequest) request;
        //从editor中获取要上传的图片
        MultipartFile file = multipartRequest.getFile("editormd-image-file");
        FileDto fileDto = new FileDto();
        if (file != null) {
            String url = ossProvider.uploadFile(file);
            fileDto.setMessage("上传成功");
            fileDto.setSuccess(1);
            fileDto.setUrl(url);
        }
        return fileDto;
    }
}
