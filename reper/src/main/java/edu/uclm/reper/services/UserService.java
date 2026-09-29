package edu.uclm.reper.services;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import edu.uclm.reper.http.AlarideProxy;
import edu.uclm.reper.model.Role;
import edu.uclm.reper.model.User;
import edu.uclm.reper.repository.UserRepository;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AlarideProxy alarideProxy;

    @Autowired
    private ProjectService projectService;

    public void create(Map<String, String> user) throws Exception {
        try {
            alarideProxy.create(user.get("email"), user.get("pwd"));
        } catch (Exception e) {
            throw new Exception("Error creating user: " + e.getMessage(), e);
        }

        User u = new User(user.get("email"), Role.USUARIO);
        try {
            userRepository.save(u);
        } catch (Exception e) {
            throw new Exception("Error saving user to repository: " + e.getMessage(), e);
        }

        try {
            projectService.createDemoProjectForUser(u);
        } catch (Exception e) {
            throw new Exception("Error provisioning demo project for new user: " + e.getMessage(), e);
        }
    }

    public String login(Map<String, String> user) throws Exception {
        return alarideProxy.login(user);
    }

    public String getUserEmail(Map<String, String> token) throws Exception {
        try {
            return alarideProxy.getUserId(token);
        } catch (Exception e) {
            throw new Exception("Error retrieving user email: " + e.getMessage(), e);
        }

    }

    public void validateToken(String token, String email) throws Exception {
        try {
            alarideProxy.validateToken(Map.of("token", token, "email", email));
        } catch (Exception e) {
            throw new Exception("Invalid token: " + e.getMessage(), e);
        }
    }

    public String refreshToken(Map<String, String> request) throws Exception {
        return alarideProxy.refreshToken(request);
    }

    public String recoveryRequest(String email) throws Exception {
        return alarideProxy.recoveryRequest(email);
    }

    public String recoveryValidate(String tokenId, String email, String code) throws Exception {
        Map<String, String> data = Map.of(
                "tokenId", tokenId,
                "email", email,
                "code", code);
        return alarideProxy.recoveryValidate(data);
    }

    public void recoveryReset(String email, String newPassword) throws Exception {
        Map<String, String> data = Map.of(
                "email", email,
                "newPassword", newPassword);
        alarideProxy.recoveryReset(data);
    }

    public User getUserProfile(String email) {
        return userRepository.findById(email).orElseGet(() -> {
            User newUser = new User(email, Role.USUARIO);
            return userRepository.save(newUser);
        });
    }

}
