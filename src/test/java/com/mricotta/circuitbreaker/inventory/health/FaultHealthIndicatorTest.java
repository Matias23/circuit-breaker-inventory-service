package com.mricotta.circuitbreaker.inventory.health;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import com.mricotta.circuitbreaker.inventory.service.FaultService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.health.contributor.Status;

@ExtendWith(MockitoExtension.class)
class FaultHealthIndicatorTest {

    @Mock
    private FaultService faultService;

    @InjectMocks
    private FaultHealthIndicator indicator;

    @Test
    void health_whenFaultDisabled_isUp() {
        given(faultService.isFaultEnabled()).willReturn(false);

        var health = indicator.health();

        assertThat(health.getStatus()).isEqualTo(Status.UP);
        assertThat(health.getDetails()).containsEntry("faultEnabled", false);
    }

    @Test
    void health_whenFaultEnabled_isDown() {
        given(faultService.isFaultEnabled()).willReturn(true);

        var health = indicator.health();

        assertThat(health.getStatus()).isEqualTo(Status.DOWN);
        assertThat(health.getDetails()).containsEntry("faultEnabled", true);
    }
}
