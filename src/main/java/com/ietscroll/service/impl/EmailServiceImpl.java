package com.ietscroll.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.ietscroll.service.EmailService;

@Service
public class EmailServiceImpl implements EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendEmail(String toEmail, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("iet.scroll.in@gmail.com");
        message.setTo(toEmail);
        message.setSubject("IET Scroll - Verification Code");
        message.setText("Your verification code is: " + otp + "\n\nThis code expires in 10 minutes.");
        mailSender.send(message);
    }
}