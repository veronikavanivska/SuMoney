package org.example.sumoney.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/app")
public class cont {

    @GetMapping("/ap")
    public String healt() {
        return "I am ok";
    }
}