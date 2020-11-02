package com.hty.forum.provider;

import com.aliyun.oss.OSSClientBuilder;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import com.aliyun.oss.OSS;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

/**
 * create by Semineces on 2020-10-08
 */
@Component
public class OssProvider {

    //从properties中读取相关配置
    @Value("${aliyun.oss.file.endpoint}")
    private String endpoint;

    @Value("${aliyun.oss.file.keyid}")
    private String accessKeyId;

    @Value("${aliyun.oss.file.keysecret}")
    private String accessKeySecret;

    @Value("${aliyun.oss.file.bucketname}")
    private String bucketName;

    public String uploadFile(MultipartFile file) throws IOException {
        //建立OSSClient
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
        InputStream inputStream = file.getInputStream();
        String fileName = file.getOriginalFilename();

        String uuid = UUID.randomUUID().toString().replaceAll("-", "");
        fileName = uuid + fileName;
        String datePath = new DateTime().toString("yyyy/MM/dd");
        fileName = datePath + "/" + fileName;

        //第一个参数是bucketName
        //第二个参数上传到oss文件路径和文件名称/aa/bb/1.png
        ossClient.putObject(bucketName, fileName, inputStream);
        //关闭OSSClient。
        ossClient.shutdown();

        //把上传之后的文件路径返回
        //需要把上传到阿里云oss的路径手动拼接
        return "https://" + bucketName + "." + endpoint + "/" + fileName;
    }
}

