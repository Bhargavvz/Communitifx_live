package com.cofix.cofixBackend.Controllers;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;

@Slf4j
@CrossOrigin
public class CofixBaseController {

    private static final Logger logger = LoggerFactory.getLogger(CofixBaseController.class);

    @GetMapping("/")
    public String home() {
        logger.info("Redirect to main page");
        return "redirect:/login.html";
    }
}
