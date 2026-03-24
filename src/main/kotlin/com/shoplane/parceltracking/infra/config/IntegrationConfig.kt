package com.shoplane.parceltracking.infra.config

import com.shoplane.parceltracking.infra.carrierregistry.CarrierRegistryClient
import com.shoplane.parceltracking.infra.outbox.OutboxTopics
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.cache.annotation.EnableCaching
import org.springframework.cloud.openfeign.EnableFeignClients
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableScheduling

@Configuration
@EnableCaching
@EnableScheduling
@EnableFeignClients(clients = [CarrierRegistryClient::class])
@EnableConfigurationProperties(OutboxTopics::class)
class IntegrationConfig
