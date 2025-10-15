package iso25.g05.esi_media.controllers;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import iso25.g05.esi_media.model.Usuario;
import iso25.g05.esi_media.services.UserService;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("users")
public class UserController {
    
	@Autowired
	UserService userService;

    @PostMapping("/login")
	public Usuario login(@RequestBody Map<String, String> loginData) {
		Usuario loggedInUser = this.userService.login(loginData);
		if (loggedInUser == null)
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Invalid credentials");
		return loggedInUser;
	}

	@PostMapping("/login3Auth")
	public void login3Auth(@RequestBody Map<String, String> loginData) {
		this.userService.login3Auth(loginData);
	}

}
