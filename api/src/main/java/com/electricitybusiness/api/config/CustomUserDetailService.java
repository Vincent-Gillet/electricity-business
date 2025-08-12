package com.electricitybusiness.api.config;

import com.electricitybusiness.api.repository.RepairerRepository;
import com.electricitybusiness.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailService implements UserDetailsService {
    private final UserRepository userRepository;
    private final RepairerRepository repairerRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByEmailUser(username)
                .map(user -> (UserDetails) user)
                .or(() -> repairerRepository.findByEmailRepairer(username).map(repairer -> (UserDetails) repairer))
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé : " + username));
    }
}
