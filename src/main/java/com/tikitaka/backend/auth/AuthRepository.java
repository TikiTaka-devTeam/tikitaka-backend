package com.tikitaka.backend.auth;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tikitaka.backend.auth.entity.Auth;
import com.tikitaka.backend.auth.entity.Provider;

public interface AuthRepository extends JpaRepository<Auth, UUID> {
    Optional<Auth> findByProviderAndProviderUserId(
        Provider provider, 
        String providerUserId
    );
}
