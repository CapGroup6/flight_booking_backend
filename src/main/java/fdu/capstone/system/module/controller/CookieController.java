package fdu.capstone.system.module.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CookieController {
    @GetMapping("/set-cookie")
    public String setCookie(HttpServletResponse response, HttpSession session) {
        Cookie cookie = new Cookie("myCookie", session.getId());
        cookie.setDomain("localhost"); // Use localhost to allow both frontend and backend
        cookie.setPath("/"); // Set the path to allow access from all paths
        response.addCookie(cookie);
        return "Cookie set successfully!";
    }
}
