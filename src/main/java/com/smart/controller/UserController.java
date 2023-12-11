package com.smart.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.smart.dao.ContactRepository;
import com.smart.dao.UserRepository;
import com.smart.entities.Contact;
import com.smart.entities.User;
import com.smart.helper.Message;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/user")
public class UserController {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ContactRepository contactRepository;
    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @ModelAttribute
    public void addCommonData(Model model, Principal principal) {
        String userName = principal.getName();
        System.out.println(userName);
        // Use userepository
        User user = this.userRepository.getUserByUserName(userName);
        System.out.println(user);
        model.addAttribute("user", user);
    }

    @RequestMapping("/index")
    public String dashboard(org.springframework.ui.Model model, Principal principal) {
        model.addAttribute("title", "Dashboard");
        return "normal/user_dashboard";
    }

    // Open Add contact handler

    @GetMapping(value = { "/add-contact" })
    public String openAddContactFor(Model model) {
        model.addAttribute("title", "Add Contact");
        model.addAttribute("contact", new Contact());
        return "normal/add_contact";
    }

    @PostMapping("/process-contact")
    public String processContact(@ModelAttribute Contact contact, @RequestParam("profileImage") MultipartFile file,
            HttpSession session,
            Principal principal) {
        try {

            String name = principal.getName();
            User user = this.userRepository.getUserByUserName(name);

            contact.setUser(user);
            // Processing and Uploading file
            if (file.isEmpty()) {
                session.setAttribute("message", new Message("Something went wrong!!Retry", "danger"));
                System.out.println("File EMPTY");
                contact.setImage("contact.png");
            } else {
                // upload file to folder and update name to contact
                contact.setImage(file.getOriginalFilename());
                File saveFile = new ClassPathResource("static/image").getFile();
                java.nio.file.Path path = Paths
                        .get(saveFile.getAbsolutePath() + File.separator + file.getOriginalFilename());
                Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

                System.out.println("Image uploaded");
            }
            user.getContacts().add(contact);
            this.userRepository.save(user);
            session.setAttribute("message", new Message("Your contact added successfully", "success"));
            System.out.println("Added to DB");

        } catch (Exception e) {
            System.out.println("ERROR" + e.getMessage());
        }
        return "normal/add_contact";
    }

    // View Contact Handler
    @GetMapping("/show_contact/{page}")
    public String showContacts(@PathVariable("page") Integer page, Model m, Principal principal) {
        m.addAttribute("title", "Show Contact");
        String userName = principal.getName();
        User user = this.userRepository.getUserByUserName(userName);
        Pageable pageable = PageRequest.of(page, 5);
        Page<Contact> contacts = this.contactRepository.findConatactByUser(user.getId(), pageable);
        m.addAttribute("contacts", contacts);
        m.addAttribute("currentPage", page);
        m.addAttribute("totalPages", contacts.getTotalPages());
        return "normal/show_contact";
    }

    // showing specific contact details
    @GetMapping("/contact/{cid}")
    public String showContactDetail(@PathVariable("cid") Integer cid, Model m, Principal principal) {
        String userName = principal.getName();
        User user = this.userRepository.getUserByUserName(userName);
        Optional<Contact> contactOptional = this.contactRepository.findById(cid);
        Contact contact = contactOptional.get();
        if (user.getId() == contact.getUser().getId()) {
            m.addAttribute("contact", contact);
        }
        return "normal/contact_detail";
    }

    // Delete Contact Handler
    // delete specific contact with image

    @GetMapping("/delete/{cId}")
    public String deleteContact(@PathVariable("cId") Integer cId, Model model, HttpSession session,
            Principal principal) {

        Contact contact = contactRepository.findById(cId).get();

        // checking valid signin user
        User user = this.userRepository.getUserByUserName(principal.getName());
        user.getContacts().remove(contact);
        if (user.getId() == contact.getUser().getId()) {
            this.userRepository.save(user);

            session.setAttribute("message", new Message("Something went wrong !!", "danger"));
        }
        session.setAttribute("message", new Message("Contact Delete Successfully !!", "success"));

        return "redirect:/user/show_contact/0";
    }

    // Updating Form
    @PostMapping("/update-contact/{cid}")
    public String updateForm(@PathVariable("cid") Integer cid, Model m) {
        m.addAttribute("title", "Update Contact");
        Contact contact = this.contactRepository.findById(cid).get();
        m.addAttribute("contact", contact);
        return "normal/update_form";
    }

    // update contact Handler
    @RequestMapping(value = "/process-update", method = RequestMethod.POST)
    public String updateHandler(Principal principal, Model m, HttpSession session, @ModelAttribute Contact contact,
            @RequestParam("profileImage") MultipartFile file) {
        try {
            // Old contact detail
            Contact oldContactDeatils = this.contactRepository.findById(contact.getCid()).get();

            if (!file.isEmpty()) {
                // Delete old photo
                File deleFile = new ClassPathResource("static/image").getFile();
                File file1 = new File(deleFile, oldContactDeatils.getImage());
                file1.delete();
                // Upload new file
                File saveFile = new ClassPathResource("static/image").getFile();
                java.nio.file.Path path = Paths
                        .get(saveFile.getAbsolutePath() + File.separator + file.getOriginalFilename());
                Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
                contact.setImage(file.getOriginalFilename());
            } else {
                contact.setImage(oldContactDeatils.getImage());
            }
            User user = this.userRepository.getUserByUserName(principal.getName());
            contact.setUser(user);
            this.contactRepository.save(contact);
            session.setAttribute("message", new Message("Your contact updated", "success"));
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
        return "redirect:/user/contact/" + contact.getCid();
    }

    // my profile Handler
    @GetMapping("/profile")
    public String yourProfile(Model m) {
        m.addAttribute("title", "profile");
        return "normal/profile";
    }

    // Open Setting Handler
    @GetMapping("/setting")
    public String openSettings() {
        return "normal/setting";
    }

    // Change password Handler
    @PostMapping("/change-password")
    public String changepassword(HttpSession session, Principal principal,
            @RequestParam("oldPassword") String oldPassword,
            @RequestParam("newPassword") String newPassword) {
        System.out.println("Old password" + oldPassword);
        System.out.println("New password" + newPassword);
        String userName = principal.getName();
        User currentUser = this.userRepository.getUserByUserName(userName);
        if (this.bCryptPasswordEncoder.matches(oldPassword, currentUser.getPassword())) {
            // change password
            currentUser.setPassword(this.bCryptPasswordEncoder.encode(newPassword));
            this.userRepository.save(currentUser);
            session.setAttribute("message", new Message("Your password successfully changed", "success"));
        } else {
            // Error
            session.setAttribute("message", new Message("Old password incorrect!!Please re-enter", "danger"));
            return "redirect:/user/setting";
        }
        return "redirect:/user/index";
    }
}
