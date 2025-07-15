package edu.uclm.reper.http;

import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import edu.uclm.reper.model.User;
import edu.uclm.reper.repository.UserRepository;
import edu.uclm.reper.services.UserService;


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
    		if (existingUser.isPresent())
        		throw new ResponseStatusException(HttpStatus.CONFLICT, user.get("email") + " already exists");
				
    	this.userService.create(user);
	}
	
	@PostMapping("/login")
	public String login(@RequestBody User user) {
		String token = this.userService.login(user);
		if (token==null)
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Invalid credentials");
		return token;
	}
}
