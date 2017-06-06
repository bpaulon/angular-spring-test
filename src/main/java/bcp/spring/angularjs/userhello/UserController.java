package bcp.spring.angularjs.userhello;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
public class UserController {

	@RequestMapping(value = "/user", method = RequestMethod.POST)
	@ResponseStatus(value = HttpStatus.CREATED)
	public User helloUser(@RequestBody User user) {
		log.debug("Received: {}", user);

		user.id("007");
		return user;
	}

}