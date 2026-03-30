package com.a.user.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Thymeleaf HTML 페이지 라우팅
 */
@Controller
public class PageController {

    @GetMapping("/")
    public String root() { return "redirect:/main"; }

    @GetMapping("/main")
    public String main() { return "main"; }

    @GetMapping("/login")
    public String login() { return "auth/login"; }

    @GetMapping("/signup")
    public String signup() { return "auth/signup"; }

    @GetMapping("/my-page")
    public String myPage() { return "member/my-page"; }
}
