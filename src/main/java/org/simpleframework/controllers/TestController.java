package org.simpleframework.controllers;

import org.simpleframework.annotations.*;
import org.simpleframework.database.JpaRepository;
import org.simpleframework.models.User;
import org.simpleframework.services.TestService;

import java.util.Optional;

@Controller
public class TestController {

    private final TestService testService;
    private final JpaRepository<User, Integer> userRepository;

    @Autowired
    public TestController(TestService testService, JpaRepository<User, Integer> userRepository) {
        this.testService = testService;
        this.userRepository = userRepository;
    }

    @GetMapping("/")
    public String home() {
        return "{\"route\": \"/home\"}";
    }

    @GetMapping("/hello")
    public String hello() {
        return testService.hello();
    }

    @GetMapping("/user/{id}")
    public String getUserById(
            @PathVariable("id") int id,
            @RequestParam(value = "name", required = false) String name
    ) {
        System.out.println(id + ": " + name);

        Optional<User> userOptional = userRepository.findById(id);

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            return "{\"name\": \"" + user.getName() + "\", \"id\": " + user.getId() + "}";
        }

        return "{\"message\": \"Not found user with id: " + id + "\"}";
    }

    @PostMapping("/user")
    public User createUser(@RequestBody User user) {

        return userRepository.save(user);
    }

}
