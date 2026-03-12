package com.blockchain.infrastructure.db.config

import org.springframework.context.annotation.Configuration
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories

@Configuration
@EnableR2dbcRepositories(basePackages = ["com.blockchain.infrastructure.db.repository"])
class DatabaseConfig