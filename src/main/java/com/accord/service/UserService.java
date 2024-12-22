package com.accord.service;

import java.io.IOException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.accord.Entity.User;
import com.accord.repository.EmailNotificationRepository;
import com.accord.repository.UserRepository;
import javax.servlet.http.HttpServletRequest;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class UserService {

    private final UserRepository userRepository;

    @Autowired
    private EmailNotificationRepository emailNotificationRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    private JavaMailSender mailSender;
    
    public interface UserDetailsService {
        void save(User user);
    }
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User createUser(User user) {
        return userRepository.save(user);
    }

   public Optional<User> getUserById(Long id) {
       return findById(id);
   }

   public User save(User user) {
       return userRepository.save(user);
   }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User updateUser(Long id, User updatedUser, MultipartFile photo) {
        try {
            updatedUser.setProfile_name(photo.getOriginalFilename());
            updatedUser.setProfile_type(photo.getContentType());
            updatedUser.setProfile_picture(photo.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return userRepository.save(updatedUser);
    }

    public void update2(Long id, MultipartFile photo) {
        Optional<User> existingUserOptional = userRepository.findById(id);
        User existingUser = existingUserOptional.get();
        try {
            if(!photo.isEmpty()) {
                existingUser.setProfile_name(photo.getOriginalFilename());
                existingUser.setProfile_type(photo.getContentType());
                existingUser.setProfile_picture(photo.getBytes());
            }
            if(existingUser.getName() != null) {
                existingUser.setName(existingUser.getName());
            }
            if(existingUser.getEmail() != null) {
                existingUser.setEmail(existingUser.getEmail());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        userRepository.save(existingUser);
    }

    public void updatePassword(Long id, String password) {
        Optional<User> existingUserOptional = userRepository.findById(id);
        User existingUser = existingUserOptional.get();
        existingUser.setPassword(passwordEncoder.encode(password));
        userRepository.save(existingUser);
    }

    public void updatename(Long id, String name) {
        Optional<User> existingUserOptional = userRepository.findById(id);
        User existingUser = existingUserOptional.get();
        existingUser.setName(name);
        userRepository.save(existingUser);
    }
    public void updateemail(Long id, String email) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Invalid user ID");
        }
        
        Optional<User> userOptional = userRepository.findById(id);
        if (!userOptional.isPresent()) {
            throw new NoSuchElementException("User not found");
        }
        
        User user = userOptional.get();
        user.setEmail(email);
        userRepository.save(user);
    }
    public String update4(Long id, String name, String email) {
        Optional<User> existingUserOptional = userRepository.findById(id);
        User existingUser = existingUserOptional.get();
        User user = userRepository.findByEmail(email);
        if(user.getEmail().equals(email) && user.getId().equals(id)) {
            return "1";
        }
        existingUser.setName(name);
        existingUser.setEmail(email);
        userRepository.save(existingUser);
        return "2";
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);  
    }

    public void deleteArea(Long id) {
        userRepository.deleteById(id);  
    }

    public boolean sendPasswordResetEmail(User user) {
        String token = UUID.randomUUID().toString();  // Generate unique token

        String resetLink = "http://localhost:8081/forgotPassword_setPass?token=" + token;
        user.setResetToken(token);  
        userRepository.save(user);  
        MimeMessage message = mailSender.createMimeMessage();

        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setFrom("extrahamham@gmail.com");
            helper.setTo(user.getEmail());
            helper.setSubject("Password Reset Request");

            String emailContent = "<p>Hi " + user.getName() + ",</p>" +
                    "<p>To reset your password, click the link below:</p>" +
                    "<a href=\"" + resetLink + "\">Reset Password</a>" +
                    "<p>If you didn't request a password reset, please ignore this email.</p>";

            helper.setText(emailContent, true);

            
            mailSender.send(message);

            return true;  
        } catch (MessagingException e) {
            e.printStackTrace();
            return false;  
        }
    }

   public Optional<User> findUserById(Long id) {
       return userRepository.findById(id);
   }
    
    public Optional<User> findByEmail(String email) {
        return userRepository.findFirstByEmail(email);
    }

    public boolean resetPassword(String token, String newPassword) {
        Optional<User> userOptional = userRepository.findByResetToken(token);
        if (userOptional.isPresent()) {
            User user = userOptional.get();

            user.setPassword(passwordEncoder.encode(newPassword));
            user.setResetToken(null); // Clear the reset token after use
            userRepository.save(user); 
            return true; 
        }
        return false; 
    }

    public Optional<User> findByResetToken(String resetToken) {
        return userRepository.findByResetToken(resetToken);
    }

    public boolean checkPassword(User user, String password) {
        return passwordEncoder.matches(password, user.getPassword());
    }

    public void updatePassword(User user, String newPassword) {
        user.setPassword(passwordEncoder.encode(newPassword));
        // Save the user with the new password
        // Assuming you have a UserRepository to save the updated user
        userRepository.save(user);
    }

    public User registerUser(String name, String password, String email, String contactnumber, int block_num, int lot_num, String property_status,
            MultipartFile tenancy, MultipartFile valid, String role) {
        if (email != null && password != null) {
            if (userRepository.findFirstByEmail(email).isPresent()) {
                return null;
            }
            if (userRepository.findByContactnumber(contactnumber).isPresent()) {
                return null;
            }
        }

        User user = new User();
        user.setName(name);
        user.setPassword(passwordEncoder.encode(password));
        user.setEmail(email);
        user.setContactnumber(contactnumber);
        user.setBlock_num(block_num);
        user.setLot_num(lot_num);
        user.setProperty_status(property_status);
        user.setConfirmation_email(null);  
        user.setConfirmationAccount(null);  

        try {
            user.setTenancy_name(tenancy.getOriginalFilename());
            user.setTenancy_type(tenancy.getContentType());
            user.setTenancy_document(tenancy.getBytes());
            user.setId_name(valid.getOriginalFilename());
            user.setId_type(valid.getContentType());
            user.setId_document(valid.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
        user.setRole(role);
        User registeredUser = userRepository.save(user);

        // Send an email to the admin for approval
        sendApprovalRequestToAdmin(registeredUser);

        return registeredUser;
    }

    public User registerAdmin(String email, String password, String role) {
        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setConfirmation_email(true);
        user.setConfirmationAccount(true);
        user.setRole(role);
        return userRepository.save(user);
    }

    public Boolean authenticateLogin(String email, String password) {
        User user = userRepository.findByEmail(email);
        if (user != null) {
            return passwordEncoder.matches(password, user.getPassword());
        }
        return false;
    }

    public static String getCurrentEmail() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            Object principal = auth.getPrincipal();
            if (principal instanceof UserDetails) {
                return ((UserDetails) principal).getUsername();
            } else {
                return principal.toString();
            }
        }
        return null;
    }

    public Boolean checkPhone(String contactnumber) {
        return !userRepository.findByContactnumber(contactnumber).isPresent();
    }

    public Boolean checkEmail(String email) {
        return !userRepository.findFirstByEmail(email).isPresent();
    }

    public List<User> getAllUser() {
        return userRepository.findAll();
    }

    private void sendApprovalRequestToAdmin(User user) {
        MimeMessage message = mailSender.createMimeMessage();

        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true);  // true indicates multipart

            helper.setFrom("extrahamham@gmail.com");  // Your app's email
            helper.setTo("klayam12x@gmail.com");  // Admin's email
            helper.setSubject("Approval Request for New User Registration");

            // Email content with user details
            helper.setText("A new user has registered with the following details:\n\n" +
                    "Name: " + user.getName() + "\n" +
                    "Email: " + user.getEmail() + "\n" +
                    "Contact: " + user.getContactnumber() + "\n" +
                    "Block Number: " + user.getBlock_num() + "\n" +
                    "Lot Number: " + user.getLot_num() + "\n" +
                    "Property Status: " + user.getProperty_status() + "\n\n" +
                    "Please approve or reject this registration in the admin panel.");

            // Add the tenancy agreement as an attachment
            if (user.getTenancy_name() != null) {
                helper.addAttachment(user.getTenancy_name(), new ByteArrayResource(user.getTenancy_document()));
            }

            // Add the valid ID as an attachment
            if (user.getId_name() != null) {
                helper.addAttachment(user.getId_name(), new ByteArrayResource(user.getId_document()));
            }

            // Send the email
            mailSender.send(message);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    public User updateUser(Long userId, Optional<User> optionalUser, MultipartFile prof) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'updateUser'");
    }

    public List<User> pendingAccountConfirmation() {
        return userRepository.findByConfirmationAccount(null);
    }

    public void confirmAccount(Long id) {
        User user = findById(id).orElse(null);
        user.setConfirmationAccount(true);
        userRepository.save(user);
        sendAccountConfirmationEmail(user);
    }
    public void sendAccountConfirmationEmail(User user) {
        MimeMessage message = mailSender.createMimeMessage();
    
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
    
            helper.setFrom("extrahamham@gmail.com"); 
            helper.setTo(user.getEmail()); 
            helper.setSubject("Account Confirmation - Welcome to Accord"); 
    
            // Construct the email body
            String emailContent = String.format(
                    "<p>Dear %s,</p>" +
                    "<p>We are pleased to inform you that your account on Accord has been successfully confirmed by our superadmin. You can now log in and start using the system to manage your amenities reservations.</p>" +
                    "<p><strong>Account Details:</strong></p>" +
                    "<ul>" +
                    "<li>Name: %s</li>" +
                    "<li>Email: %s</li>" +
                    "</ul>" +
                    "<p>If you experience any issues or have questions, please don't hesitate to reach out to us at <a href='mailto:extrahamham@gmail.com'>extrahamham@gmail.com</a>.</p>" +
                    "<p>Thank you for joining Accord!</p>" +
                    "<p>Best regards,</p>" +
                    "<p>The Accord Team</p>",
                    user.getName().split(" ")[0], 
                    user.getName(), 
                    user.getEmail() 
            );
    
            helper.setText(emailContent, true); 
    
           
            mailSender.send(message);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }
}
