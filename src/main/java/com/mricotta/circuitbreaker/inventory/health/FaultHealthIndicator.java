package com.mricotta.circuitbreaker.inventory.health;

import com.mricotta.circuitbreaker.inventory.service.FaultService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.stereotype.Component;

// Bean name becomes the component key under /actuator/health, so it shows up as "fault".
@Component("fault")
@RequiredArgsConstructor
public class FaultHealthIndicator implements HealthIndicator {

    private final FaultService faultService;

    @Override
    public Health health() {
        var enabled = faultService.isFaultEnabled();
        var builder = enabled ? Health.down() : Health.up();
        return builder.withDetail("faultEnabled", enabled).build();
    }
}
