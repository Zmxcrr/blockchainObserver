package com.blockchain.app

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.runApplication
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories

@SpringBootApplication(scanBasePackages = ["com.blockchain"])
@EnableR2dbcRepositories(basePackages = ["com.blockchain.infrastructure.db.repository"])
@EntityScan(basePackages = ["com.blockchain.infrastructure.db.entity"])
class BlockchainApplication

fun main(args: Array<String>) {
    runApplication<BlockchainApplication>(*args)
}