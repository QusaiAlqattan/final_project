package org.example.final_project.service;
//
//import org.example.final_project.model.SystemUser;
//import org.example.final_project.repository.SystemUserRepository;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.security.core.userdetails.User;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.security.core.userdetails.UserDetailsService;
//import org.springframework.security.core.userdetails.UsernameNotFoundException;
//
//import java.util.Collections;
//
//import org.springframework.security.core.GrantedAuthority;
//import org.springframework.security.core.authority.SimpleGrantedAuthority;
//import org.springframework.stereotype.Service;
//
//@Service
//public class SystemUserDetailService implements UserDetailsService{
//
//    @Autowired
//    SystemUserRepository systemUserRepository;
//
//    // Correct constructor injection, Spring will inject the repository bean
//    public SystemUserDetailService(SystemUserRepository systemUserRepository) {
//        this.systemUserRepository = systemUserRepository; // Initialize the final field
//    }
//
//    @Override
//    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
//
//        SystemUser user = systemUserRepository.findByUsername(username);
//        if(user == null)
//            throw new UsernameNotFoundException("User Not Found!");
//
//        GrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + user.getRole().getName());
//        return new User(user.getUsername(), user.getPassword(), Collections.singleton(authority));
//    }
//
//}


import org.example.final_project.model.SystemUser;
import org.example.final_project.repository.SystemUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class SystemUserDetailService implements UserDetailsService {

    private final SystemUserRepository systemUserRepository;

    @Autowired
    public SystemUserDetailService(SystemUserRepository systemUserRepository) {
        this.systemUserRepository = systemUserRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Fetch the user from the repository
        SystemUser user = systemUserRepository.findByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("User not found");
        }

        List<GrantedAuthority> authorities = new ArrayList<>();

        // Check if the user has a role and convert it to GrantedAuthority
        if (user.getRole() != null) {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + user.getRole().getName()));
        }

        // Convert SystemUser to UserDetails
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                authorities // Pass the authorities list here
        );
    }

}
