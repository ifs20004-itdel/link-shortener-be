package com.developer.linkshortener.controller;

import com.developer.linkshortener.Constant.Constant;
import com.developer.linkshortener.Constant.MessageConstant;
import com.developer.linkshortener.model.Data;
import com.developer.linkshortener.response.ResponseHandler;
import com.developer.linkshortener.util.GenerateString;
import com.developer.linkshortener.util.QRCodeGenerator;
import com.google.common.hash.Hashing;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import org.apache.commons.validator.UrlValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/")
@CrossOrigin(origins = "*")
public class UrlResourceController {

    @Autowired
    StringRedisTemplate redisTemplate;

    @Value("${base.url}")
    private String baseUrl;

    // Not used
    @GetMapping("/links")
    public ResponseEntity<Object> getLinks(){
        Set<String> links = redisTemplate.keys("*");
        return ResponseHandler.responseBuilder(HttpStatus.OK,false, "Links fetch successfully !",links);
    }

    // Not Used
    @GetMapping("/link/{id}")
    public ResponseEntity<Object> getLongUrl(@PathVariable String id){
        String url = redisTemplate.opsForValue().get(id);
        if(url==null){
            return ResponseHandler.responseBuilder(HttpStatus.NOT_FOUND,true,"There is no shorter URL for: "+ id , new HashMap<>());
        }
        return ResponseHandler.responseBuilder(HttpStatus.OK,false,"Long url fetch successfully!",url);
    }

    // Used
    @GetMapping("/{id}")
    public ResponseEntity<Object> redirect(@PathVariable String id){
        String url = redisTemplate.opsForValue().get(id);

        if(url ==null){
            return ResponseHandler.responseBuilder(HttpStatus.NOT_FOUND,true,"There is no shorter URL for: "+ id , new HashMap<>());
        }
        return ResponseEntity.status(HttpStatus.FOUND).header("Location", url).build();
    }

    // Used
    @GetMapping("/qr-code/{id}")
    public ResponseEntity<Object> generateQRCode( @PathVariable String id){
        String key = baseUrl+id;

        byte[] image;
        try{
            image = QRCodeGenerator.generateQRCodeImage(key,250,250);
        }catch (WriterException | IOException e){
            e.printStackTrace();
            return ResponseHandler.responseBuilder(HttpStatus.BAD_REQUEST,true,MessageConstant.QRCODE_FAILED,e.getMessage());
        }
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.IMAGE_PNG);

        return ResponseEntity.ok().headers(httpHeaders).body(image);
    }

    @GetMapping ("/qr-code/download/{id}")
            public ResponseEntity<Object> downloadQRCode(@PathVariable String id) throws WriterException {

                String key = baseUrl+id;
                QRCodeWriter qrCodeWriter = new QRCodeWriter();
                BitMatrix bitMatrix = qrCodeWriter.encode(key, BarcodeFormat.QR_CODE, Constant.SIZE, Constant.SIZE);
                try{
                    File saveDir = new File(Constant.DOWNLOAD_PATH+ GenerateString.generate()+".png");
                    BufferedImage bufferedImage = new BufferedImage(Constant.SIZE, Constant.SIZE, BufferedImage.TYPE_INT_RGB);
                    for (int x = 0; x < Constant.SIZE; x++) {
                        for (int y = 0; y < Constant.SIZE; y++) {
                            bufferedImage.setRGB(x, y, bitMatrix.get(x, y) ? 0xFF000000 : 0xFFFFFFFF);
                        }
                    }
                    ImageIO.write(bufferedImage, "png", saveDir);
                }catch (IOException e){
                    e.printStackTrace();
                }
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.IMAGE_PNG);
                return ResponseHandler.responseBuilder(HttpStatus.CREATED,false,MessageConstant.DOWNLOAD_SUCCESS,"");
            }

    // Used
    @PostMapping("shorten")
    public ResponseEntity<Object> generateKey(@RequestBody Data formData){
        Map<String,Object> msg = new HashMap<>();
        UrlValidator urlValidator = new UrlValidator(
                new String[]{
                        "http",
                        "https"
                }
        );

        if(urlValidator.isValid(formData.getUrl())){
            String key = formData.getTitle().get();
            if(formData.getTitle() != null && !key.isEmpty()){
                if(redisTemplate.hasKey(key)){
                    msg.put("error",MessageConstant.ENTITY_EXIST);
                    return ResponseHandler.responseBuilder(HttpStatus.CONFLICT,true, MessageConstant.ENTITY_EXIST, msg);
                }
                redisTemplate.opsForValue().set(formData.getTitle().get(),formData.getUrl(), 1, TimeUnit.DAYS);
                String newUrl = baseUrl+formData.getTitle().get();
                msg.put("url",newUrl);
                return ResponseHandler.responseBuilder(HttpStatus.CREATED, false, MessageConstant.SHORTEN_SUCCESS,msg);
            }
            String id = Hashing.murmur3_32().hashString(formData.getUrl(), StandardCharsets.UTF_8).toString();
            redisTemplate.opsForValue().set(id,formData.getUrl(),1, TimeUnit.DAYS);
            String newUrl = baseUrl+id;
            msg.put("url",newUrl);
            return ResponseHandler.responseBuilder(HttpStatus.CREATED,false,MessageConstant.SHORTEN_SUCCESS,msg);
        }
        msg.put("error",MessageConstant.INVALID_URL);
        return ResponseHandler.responseBuilder(HttpStatus.BAD_REQUEST,true, MessageConstant.INVALID_URL,msg);
    }

    // Done?
    @PutMapping("/edit/{url}")
    public ResponseEntity<Object> editKey(@RequestBody Data newKey, @PathVariable(name = "url") String oldKey){
        Map<String, Object> msg = new HashMap<>();
        if(newKey.getUrl().isEmpty()){
            msg.put("error", MessageConstant.EMPTY_FIELD);
            return ResponseHandler.responseBuilder(HttpStatus.BAD_REQUEST,true, MessageConstant.EMPTY_FIELD, msg);
        }
        if(redisTemplate.hasKey(newKey.getUrl())){
            msg.put("error",MessageConstant.ENTITY_EXIST);
            return ResponseHandler.responseBuilder(HttpStatus.CONFLICT,true, MessageConstant.ENTITY_EXIST, msg);
        }
        redisTemplate.rename(oldKey, newKey.getUrl());

        String newShortenUrl = baseUrl+newKey.getUrl();
        msg.put("url",newShortenUrl);
        return ResponseHandler.responseBuilder(HttpStatus.CREATED,false,MessageConstant.EDIT_SUCCESS,msg);
    }
}