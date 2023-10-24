package com.csn.charity.service.interfaces;

import java.util.List;

import org.springframework.security.core.userdetails.UserDetailsService;

import com.csn.charity.dto.UserDTO;
import com.csn.charity.model.AuthenticationType;
import com.csn.charity.model.User;

public interface UserService extends UserDetailsService {
    Long addUser(UserDTO userDto);

    User findUserByUsername(String username);

    User findUserByEmail(String email);

    void registerOAuthUser(String email, AuthenticationType type);

    User get(Long id);

    List<User> findAllUsers();

    void activateAccount(Long id);

    void disableAccount(Long id);

    void delete(Long id);

    String forgotPassword(String email);
    
    String setPassword(String email, String newPassword);
}
