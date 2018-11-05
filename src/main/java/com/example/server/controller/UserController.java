package com.example.server.controller;

import com.example.server.model.User;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {
    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public ResponseEntity<User> loginUserloginUser(@RequestHeader(value = "Authorization") String basicAuthHeader) {
        String decodedUserName = new String(Base64.decodeBase64(basicAuthHeader.split(" ")[1])).split(":")[0];
        User user = new User(decodedUserName);
        return new ResponseEntity<User>(user, HttpStatus.OK);
    }
}