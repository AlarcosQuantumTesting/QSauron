package edu.uclm.reper.services;

import java.util.Map;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import edu.uclm.reper.http.AlarideProxy;
import edu.uclm.reper.model.User;
import edu.uclm.reper.repository.UserRepository;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AlarideProxy alarideProxy;

    public void create (Map<String, String> user) throws Exception {
        try {
            alarideProxy.create(user.get("email"), user.get("pwd"));
        } catch (Exception e) {
            throw new Exception("Error creating user: " + e.getMessage(), e);
        }

        User u = new User(user.get("email"));
        try{
            userRepository.save(u);
        }catch (Exception e) {
            throw new Exception("Error saving user to repository: " + e.getMessage(), e);
        }
        
    }

    public String login(Map<String,String> user) throws Exception {
        return alarideProxy.login(user);
    }
    
}
