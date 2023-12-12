package com.smart.controller;

import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.smart.dao.UserRepository;
import com.smart.entities.User;
import com.smart.service.EmailService;

import jakarta.servlet.http.HttpSession;

@Controller
public class ForgotController {

    Random random = new Random(1000);
    @Autowired
    private EmailService emailService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @RequestMapping("/forgot")
    public String openEmailForm() {
        return "forgot_email_form";
    }

    @PostMapping("/send_otp")
    public String sendOtp(HttpSession session, @RequestParam("email") String email) {
        System.out.println("EMAIL" + email);

        int otp = random.nextInt(9999);
        System.out.println("OTP" + otp);

        // Code for send OTP in email
        String subject = "OTP FROM SMARTCONTACT";
        String message = "<h1>OTP : " + otp + "</h2>";
        String to = email;
        boolean flag = this.emailService.sendEmail(subject, message, to);
        if (flag) {
            session.setAttribute("myotp", otp);
            session.setAttribute("email", email);
            return "verify_otp";
        } else {
            session.setAttribute("message", "Check email id!!");
            return "forgot_email_form";
        }
    }

    @PostMapping("/verify-otp")
    public String VerifyOtp(@RequestParam("otp") int otp, HttpSession session) {
        Integer myotp = (int) session.getAttribute("myotp");
        String email = (String) session.getAttribute("email");
        if (myotp == otp) {
            // password change form
            User user = this.userRepository.getUserByUserName(email);
            if (user == null) {
                // send error message
                session.setAttribute("message", "User doesnot exist with this email");
                return "forgot_email_form";
            } else {
                // send change password form
            }
            return "password_change_form";
        } else {
            session.setAttribute("message", "Enter correct otp");
            return "verify_otp";
        }
    }

    @PostMapping("/change-password")
    public String changepassword(HttpSession session, @RequestParam("newpassword") String newpassword) {
        String email = (String) session.getAttribute("email");
        User user = this.userRepository.getUserByUserName(email);
        user.setPassword(this.bCryptPasswordEncoder.encode(newpassword));
        this.userRepository.save(user);
        return "redirect:/signin?change=password changed successfully...";
    }
}
