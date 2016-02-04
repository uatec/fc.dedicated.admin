package com.hidef.fc.dedicated.admin;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SecuredPingController {

    @RequestMapping(value = "/secured/ping")
    @ResponseBody
    public String securedPing() {
        return "All good. You only get this message if you're authenticated";
    }
}
