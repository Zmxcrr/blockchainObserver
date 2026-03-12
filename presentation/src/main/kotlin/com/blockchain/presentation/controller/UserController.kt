package com.blockchain.presentation.controller

import com.blockchain.application.service.UserService
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono
import java.util.UUID

@RestController
@RequestMapping("/api/users")
class UserController(
    private val userService: UserService
) {
    @GetMapping("/me")
    fun getCurrentUser(
        @AuthenticationPrincipal userDetails: UserDetails
    ): Mono<ResponseEntity<UserProfileResponse>> {
        val userId = UUID.fromString(userDetails.username)
        return userService.getUserById(userId)
            .map { user ->
                ResponseEntity.ok(
                    UserProfileResponse(
                        id = user.id.toString(),
                        email = user.email,
                        username = user.username,
                        createdAt = user.createdAt.toString()
                    )
                )
            }
    }
}

data class UserProfileResponse(
    val id: String,
    val email: String,
    val username: String,
    val createdAt: String
)