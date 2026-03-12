package com.blockchain.presentation.controller

import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import reactor.core.publisher.Mono

@RestControllerAdvice
class GlobalExceptionHandler {

    private val log = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgument(ex: IllegalArgumentException): Mono<ResponseEntity<ErrorResponse>> =
        Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ErrorResponse(ex.message ?: "Bad request")))

    @ExceptionHandler(NoSuchElementException::class)
    fun handleNotFound(ex: NoSuchElementException): Mono<ResponseEntity<ErrorResponse>> =
        Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorResponse(ex.message ?: "Not found")))

    @ExceptionHandler(Exception::class)
    fun handleGeneral(ex: Exception): Mono<ResponseEntity<ErrorResponse>> {
        log.error("Unexpected error", ex)
        return Mono.just(
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse("Internal server error"))
        )
    }
}

data class ErrorResponse(val message: String)