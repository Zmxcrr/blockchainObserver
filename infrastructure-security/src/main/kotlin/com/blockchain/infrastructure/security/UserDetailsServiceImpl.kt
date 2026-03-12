package com.blockchain.infrastructure.security

import com.blockchain.domain.port.UserRepositoryPort
import kotlinx.coroutines.reactor.mono
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.ReactiveUserDetailsService
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import java.util.UUID

@Service
class UserDetailsServiceImpl(
    private val userRepositoryPort: UserRepositoryPort
) : ReactiveUserDetailsService {

    override fun findByUsername(username: String): Mono<UserDetails> = mono {
        val userId = runCatching { UUID.fromString(username) }.getOrNull()
            ?: throw UsernameNotFoundException("Invalid user id: $username")

        val user = userRepositoryPort.findById(userId)
            ?: throw UsernameNotFoundException("User not found: $username")

        User(
            user.id.toString(),
            user.passwordHash,
            listOf(SimpleGrantedAuthority("ROLE_USER"))
        )
    }
}