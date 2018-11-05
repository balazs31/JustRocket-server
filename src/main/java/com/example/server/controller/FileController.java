package com.example.server.controller;

import com.example.server.model.FileData;
import com.example.server.model.User;
import com.example.server.payload.UploadFileResponse;
import com.example.server.service.FileStorageService;
import org.apache.tomcat.util.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;

@RestController
public class FileController {

    private static final Logger logger = LoggerFactory.getLogger(FileController.class);

    @Autowired
    private FileStorageService fileStorageService;

    @CrossOrigin("http://localhost:4200")
    @RequestMapping(value = "/uploadFile", method = RequestMethod.POST)
    public UploadFileResponse uploadFile(@RequestHeader(value = "Authorization") String basicAuthHeader, @RequestParam("file") MultipartFile file) {
        String decodedUserName = new String(Base64.decodeBase64(basicAuthHeader.split(" ")[1])).split(":")[0];
        User user = new User(decodedUserName);
        String fileName = fileStorageService.storeFile(file, user);

        String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/downloadFile/")
                .path(decodedUserName + "/")
                .path(fileName)
                .toUriString();

        return new UploadFileResponse(fileName, fileDownloadUri,
                file.getContentType(), file.getSize());
    }

    @CrossOrigin("http://localhost:4200")
    @GetMapping("/downloadFile/{fileName:.+}")
    public ResponseEntity<Resource> downloadFile(@RequestHeader(value = "Authorization") String basicAuthHeader, @PathVariable String fileName, HttpServletRequest request) {
        String decodedUserName = new String(Base64.decodeBase64(basicAuthHeader.split(" ")[1])).split(":")[0];
        User user = new User(decodedUserName);

        Resource resource = fileStorageService.loadFileAsResource(fileName, user);

        String contentType = null;
        try {
            contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
        } catch (IOException ex) {
            logger.info("Could not determine file type.");
        }

        if(contentType == null) {
            contentType = "application/octet-stream";
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

    @CrossOrigin("http://localhost:4200")
    @RequestMapping(value = "/getFiles", method = RequestMethod.GET)
    public ResponseEntity<List<FileData>> uploadFile(@RequestHeader(value = "Authorization") String basicAuthHeader) {
        String decodedUserName = new String(Base64.decodeBase64(basicAuthHeader.split(" ")[1])).split(":")[0];
        User user = new User(decodedUserName);
        System.out.println("Getting files for user: " + user.getUserName());
        List<FileData> fileList = fileStorageService.getUsersFiles(user);
        return new ResponseEntity<List<FileData>>(fileList, HttpStatus.OK);
    }

}