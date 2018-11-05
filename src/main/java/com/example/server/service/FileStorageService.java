package com.example.server.service;

import com.example.server.exception.FileStorageException;
import com.example.server.exception.MyFileNotFoundException;
import com.example.server.model.FileData;
import com.example.server.model.User;
import com.example.server.property.FileStorageProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

@Service
public class FileStorageService {

    private Path fileStorageLocation;
    private FileStorageProperties fileStorageProperties;
    @Autowired
    public FileStorageService(FileStorageProperties fileStorageProperties) {
        this.fileStorageProperties = fileStorageProperties;
    }

    private void setFileStorageLocation(String userName) {
        System.out.println(this.fileStorageProperties.getUploadDir());
        this.fileStorageLocation = Paths.get(fileStorageProperties.getUploadDir() + "/" + userName)
                .toAbsolutePath().normalize();

        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new FileStorageException("Could not create the directory where the uploaded files will be stored.", ex);
        }
    }

    public String storeFile(MultipartFile file, User user) {
        String fileName = StringUtils.cleanPath(file.getOriginalFilename());

        try {
            if(fileName.contains("..")) {
                throw new FileStorageException("Sorry! Filename contains invalid path sequence " + fileName);
            }

            setFileStorageLocation(user.getUserName());
            System.out.println(user.getUserName());
            Path targetLocation = this.fileStorageLocation.resolve(fileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            return fileName;
        } catch (IOException ex) {
            throw new FileStorageException("Could not store file " + fileName + ". Please try again!", ex);
        }
    }


    public Resource loadFileAsResource(String fileName, User user) {
        try {
            setFileStorageLocation(user.getUserName());
            Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if(resource.exists()) {
                return resource;
            } else {
                throw new MyFileNotFoundException("File not found " + fileName);
            }
        } catch (MalformedURLException ex) {
            throw new MyFileNotFoundException("File not found " + fileName, ex);
        }
    }

    public List<FileData> getUsersFiles (User user) {
        List<FileData> fileList = new ArrayList<>();
        File[] files = new File(fileStorageProperties.getUploadDir() + "/" + user.getUserName()).listFiles();
        if(files != null) {
            for (File file : files) {
                fileList.add(new FileData(file.getName(), file.length()));
            }
        }

        return fileList;
    }
}