package com.accord.controller;

import com.accord.Entity.User;
import com.accord.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.accord.config.SecurityConfig;
import com.accord.repository.UserRepository;

import jakarta.servlet.http.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Collections;
import java.util.Map;
import java.io.File;

import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    

    // Serve HTML view for the user accounts page
    @GetMapping
    public String userAccountsPage(Model model) {
        return "UserAccounts_page";  // Ensure this matches your actual Thymeleaf template file name
    }

    // List all users as JSON (for JavaScript/AJAX requests)
    @GetMapping("/list")
    @ResponseBody
    public List<User> listUsersJson() {
        return userService.getAllUsers();
    }

    // Show create user form
    @GetMapping("/create")
    public String showCreateUserForm(Model model) {
        model.addAttribute("user", new User());
        return "user-form";
    }

    // Save new user
    @PostMapping("/create")
    public String createUser(@ModelAttribute("user") User user, RedirectAttributes redirectAttributes) {
        userService.createUser(user);
        redirectAttributes.addFlashAttribute("message", "User created successfully!");
        return "redirect:/users";
    }

    // Show update user form
    @GetMapping("/edit/{id}")
    public String showEditUserForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<User> userOptional = userService.getUserById(id);
        if (userOptional.isPresent()) {
            model.addAttribute("user", userOptional.get());
            return "user-form";
        } else {
            redirectAttributes.addFlashAttribute("error", "User not found!");
            return "redirect:/users";
        }
    }

    // Update user
    @PostMapping("/edit/{id}")
    public String updateUser(@PathVariable Long id, @ModelAttribute("user") User user, @RequestParam("file") MultipartFile prof, RedirectAttributes redirectAttributes) {
        User updatedUser = userService.updateUser(id, user, prof);
        if (updatedUser != null) {
            redirectAttributes.addFlashAttribute("message", "User updated successfully!");
        } else {
            redirectAttributes.addFlashAttribute("error", "User not found!");
        }
        return "redirect:/users";
    }

    // Delete user
    @GetMapping("/delete/{id}")
    public String deleteUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        userService.deleteUser(id);
        redirectAttributes.addFlashAttribute("message", "User deleted successfully!");
        return "redirect:/users";
    }

    

     // Delete area
     /*  @GetMapping("/delete/{id}")
     public String deleteArea(@PathVariable Long id, RedirectAttributes redirectAttributes) {
         userService.deleteArea(id);
         redirectAttributes.addFlashAttribute("message", "Area deleted successfully!");
         return "redirect:/area";
     }*/

    // View user details
    // View user details
    @GetMapping("/view/{id}")
    public String viewUserDetails(@PathVariable Long id, Model model) {
        Optional<User> userOptional = userService.getUserById(id);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            model.addAttribute("user", user);
    
            // Format file size manually
            long fileSize = user.getTenancy_document().length;
            String formattedSize = formatBytes(fileSize);
            model.addAttribute("formattedSize", formattedSize);
            
            return "UserAccounts_viewDetails";
        } else {
            throw new RuntimeException("User not found");
        }
    }

    






   






private void saveProfilePicture(MultipartFile file) {
    /*try {
        String uploadDir = "/path/to/uploads"; // Define your upload directory
        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        File uploadFile = new File(uploadDir, fileName);
        file.transferTo(uploadFile);
        return "/uploads/" + fileName; // Adjust the URL based on your application’s context
    } catch (IOException e) {
        e.printStackTrace();
        throw new RuntimeException("Could not save profile picture: " + e.getMessage());
    }*/
    User user = new User();
    try {
        user.setProfile_name(file.getOriginalFilename());
        user.setProfile_type(file.getContentType());
        user.setProfile_picture(file.getBytes());
        //return userRepository.save(user).toString();
        userRepository.save(user);
    } catch(IOException e) {
        e.printStackTrace();
    }
    //return userRepository.save(user).toString();
}



    
    private String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        char pre = "KMGTPE".charAt(exp - 1);
        return String.format("%.1f %sB", bytes / Math.pow(1024, exp), pre);
    }
    

    
}
