package com.makers.loans.infrastructure.security;

import com.makers.loans.application.port.out.PasswordPort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class BCryptPasswordAdapter implements PasswordPort {
    private final PasswordEncoder encoder;
    public BCryptPasswordAdapter(PasswordEncoder encoder){this.encoder=encoder;}
    @Override public String encode(String rawPassword){return encoder.encode(rawPassword);}
    @Override public boolean matches(String rawPassword,String encodedPassword){return encoder.matches(rawPassword,encodedPassword);}
}
