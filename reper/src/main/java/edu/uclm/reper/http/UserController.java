package edu.uclm.reper.http;

import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import edu.uclm.reper.model.User;
import edu.uclm.reper.repository.UserRepository;
import edu.uclm.reper.services.UserService;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("users")
public class UserController {
	
	@Autowired
	UserService userService;
	
	@Autowired
	UserRepository userRepository ;
	
	@PostMapping("/create")
	public void create(@RequestBody Map<String,String> user) {
		
		Optional<User> existingUser = this.userRepository.findById(user.get("email"));
    	
		if (existingUser.isPresent()){
        		throw new ResponseStatusException(HttpStatus.CONFLICT, user.get("email") + " already exists");
		}
		
		try {
    		this.userService.create(user);
		} catch (Exception e) {
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error creating user: " + e.getMessage(), e);
		}
	}
	
	@PostMapping("/login")
	public String login(@RequestBody Map<String,String> user) {
		try{
			String token = this.userService.login(user);
			return token;
			
		}catch (Exception e) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Invalid credentials");
		}
		
	}
}
