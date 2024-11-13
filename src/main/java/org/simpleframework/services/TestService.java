package org.simpleframework.services;

import org.simpleframework.annotations.Autowired;
import org.simpleframework.annotations.Service;
import org.simpleframework.database.JpaRepository;
import org.simpleframework.models.User;

@Service
public class TestService {

    private final OtherService otherService;
    private final JpaRepository<User, Integer> userRepository;

    @Autowired
    public TestService(
            OtherService otherService,
            JpaRepository<User, Integer> userRepository
    ) {
        this.userRepository = userRepository;
        System.out.println("TestService created");
        this.otherService = otherService;
    }

    public String hello() {

        userRepository.findById(1);
        otherService.printHello();


        return "Hello World";
    }
}
