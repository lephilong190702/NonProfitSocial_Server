package com.csn.charity.controller.api;

import java.security.Principal;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.csn.charity.configs.JwtService;
import com.csn.charity.dto.AuthRequest;
import com.csn.charity.dto.ProfileDTO;
import com.csn.charity.dto.UserDTO;
import com.csn.charity.firebase.FirebaseService;
import com.csn.charity.model.User;
import com.csn.charity.model.UserDoc;
import com.csn.charity.service.interfaces.ProfileService;
import com.csn.charity.service.interfaces.UserService;

@RestController
@RequestMapping("/api")
public class AuthRestController {
    @Autowired
    private UserService userService;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private ProfileService profileService;

    @Autowired
    private FirebaseService firebaseService;

    @PostMapping("/register/")
    @CrossOrigin
    public ResponseEntity<?> registerAccount(@RequestBody UserDTO userDto) {
        try {
            Long userId = this.userService.addUser(userDto);
            UserDoc userDoc = new UserDoc();
            userDoc.setId(userId);
            userDoc.setDisplayName(userDto.getUsername());
            String firestoreUpdateTime = firebaseService.saveOrUpdateUser(userDoc);
            return new ResponseEntity<>("User registered successfully. Firestore update time: " + firestoreUpdateTime,
                    HttpStatus.CREATED);
        } catch (Exception e) {
            String errorMessage = "Failed to add new user: " + e.getMessage();
            return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/login/")
    @CrossOrigin
    public ResponseEntity<String> authenticateAndGetToken(@RequestBody AuthRequest authRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(authRequest.getUsername(), authRequest.getPassword()));
        if (authentication.isAuthenticated()) {
            String token = this.jwtService.generateToken(authRequest.getUsername());
            return new ResponseEntity<>(token, HttpStatus.OK);
        }
        return new ResponseEntity<>("error", HttpStatus.BAD_REQUEST);
    }

    @GetMapping("/users/")
    @CrossOrigin
    public ResponseEntity<List<User>> getUser() {
        List<User> list = this.userService.findAllUsers();
        return new ResponseEntity<>(list, HttpStatus.OK);
    }

    @GetMapping("/users/{id}")
    @CrossOrigin
    public ResponseEntity<User> getUser(@PathVariable Long id) {
        return new ResponseEntity<>(this.userService.get(id), HttpStatus.OK);
    }

    @GetMapping("/user-docs/{id}")
    @CrossOrigin
    public ResponseEntity<UserDoc> getUserDoc(@PathVariable Long id) throws InterruptedException, ExecutionException {
        UserDoc userDoc = this.firebaseService.getUser(id.toString());
        return new ResponseEntity<>(userDoc, HttpStatus.OK);
    }

    @GetMapping("/facebook/")
    @CrossOrigin
    public ResponseEntity<String> loginWithFacebook() {
        return new ResponseEntity<>("redirect:/oauth2/authorization/facebook", HttpStatus.OK);
    }

    @GetMapping("/google/")
    @CrossOrigin
    public String loginWithGoogle() {
        return "redirect:/oauth2/authorization/google";
    }

    @GetMapping("/current-user/")
    @CrossOrigin
    public ResponseEntity<User> getUser(Principal user) {
        User u = this.userService.findUserByUsername(user.getName());
        return new ResponseEntity<>(u, HttpStatus.OK);
    }

    @PutMapping("/profile/")
    @CrossOrigin
    public ResponseEntity<String> updateProfile(@RequestPart(value = "avatar") MultipartFile avatar,
            @RequestPart(value = "firstName") String firstName,
            @RequestPart(value = "lastName") String lastName,
            @RequestPart(value = "phone") String phone) throws InterruptedException, ExecutionException {

        ProfileDTO profileDTO = new ProfileDTO();
        profileDTO.setFirstName(firstName);
        profileDTO.setLastName(lastName);
        profileDTO.setPhone(phone);
        profileDTO.setFile(avatar);
        this.profileService.update(profileDTO);
        return ResponseEntity.ok("Hồ sơ đã được cập nhật thành công.");
    }

    @GetMapping("/user/userProfile")
    public String userProfile() {
        return "Welcome to User Profile";
    }

    @GetMapping("/admin/adminProfile")
    public String adminProfile() {
        return "Welcome to Admin Profile";
    }

    @PutMapping("/forgot-password/")
    @CrossOrigin
    public ResponseEntity<String> forgotPassword(@RequestParam String email) {
        return new ResponseEntity<>(userService.forgotPassword(email), HttpStatus.OK);
    }

    @PutMapping("/set-password/")
    @CrossOrigin
    public ResponseEntity<String> setPassword(@RequestParam String email, @RequestParam String newPassword) {
        return new ResponseEntity<>(userService.setPassword(email, newPassword), HttpStatus.OK);
    }
}