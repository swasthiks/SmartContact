package com.smart.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import com.smart.dao.UserRepository;
import com.smart.entities.User;
import com.smart.helper.Message;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
public class HomeController {
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;
    @Autowired
    private UserRepository repository;

    @GetMapping("/")
    public String Home(Model model) {
        model.addAttribute("title", "Home smart contact");
        return "home";
    }

    @GetMapping("/about")
    public String About(Model model) {
        model.addAttribute("title", "ABOUT smart contact");
        return "about";
    }

    @GetMapping("/signup")
    public String Signup(Model model) {
        model.addAttribute("title", "Register");
        model.addAttribute("user", new User());
        return "signup";
    }

    @GetMapping("/signin")
    public String customLogin(Model model)
    {
    return "login.html";
    }

    @PostMapping("/do_register")
    public String Register(@Valid @ModelAttribute("user") User user, BindingResult result1,
            @RequestParam(value = "agreement", defaultValue = "false") boolean agreement, Model model,
            HttpSession session) {
        // HttpSession session = ((HttpServletRequest)
        // RequestContextHolder.getRequestAttributes()).getSession();
        try {
            if (!agreement) {
                System.out.println("Terms and condition not agreed");
                throw new Exception("Terms and condition not agreed");
            }
            if (result1.hasErrors()) {
                System.out.println(result1.toString());
                // model.addAttribute("user", new User());
                return "signup";
            }
            user.setRole("ROLE_USER");
            user.setEnabled(true);
            user.setImageUrl("default.jpg");
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            this.repository.save(user);
            model.addAttribute("user", new User());
            session.setAttribute("message", new Message("Successfully signedup", "alert-success"));
            return "signup";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("user", user);
            session.setAttribute("message", new Message("Something went wrong" + e.getMessage(), "alert-danger"));
            return "signup";
        }

    }

}
