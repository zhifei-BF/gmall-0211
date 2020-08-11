package com.atguigu.thymeleafdemo.controller;

import com.atguigu.thymeleafdemo.pojo.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HelloController {
    @GetMapping("test")
    public String test(Model model){
        model.addAttribute("test","<p>喝了，长生不老。</p>");
        return "hello";
    }

    @GetMapping("test1")
    public String test1(Model model){
        model.addAttribute("test","<p>喝了，长生不老。</p>");
        User user = new User(null, 25, new User("猪八戒", 500, null));
        model.addAttribute("user",user);
        return "hello";
    }
}
