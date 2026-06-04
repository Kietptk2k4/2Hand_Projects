package com.twohands.admin_service.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "admin.kafka.producer")
public class AdminKafkaProducerProperties {

	private boolean enabled = false;
	private String bootstrapServers = "localhost:9092";
	private long sendTimeoutMs = 10_000L;

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public String getBootstrapServers() {
		return bootstrapServers;
	}

	public void setBootstrapServers(String bootstrapServers) {
		this.bootstrapServers = bootstrapServers;
	}

	public long getSendTimeoutMs() {
		return sendTimeoutMs;
	}

	public void setSendTimeoutMs(long sendTimeoutMs) {
		this.sendTimeoutMs = sendTimeoutMs;
	}
}
