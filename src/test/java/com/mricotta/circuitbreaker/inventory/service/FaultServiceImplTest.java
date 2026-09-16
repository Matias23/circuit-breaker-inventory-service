package com.mricotta.circuitbreaker.inventory.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class FaultServiceImplTest {

    private FaultServiceImpl faultService;

    @BeforeEach
    void setUp() {
        faultService = new FaultServiceImpl();
    }

    @Test
    void isFaultEnabled_byDefault_isFalse() {
        assertThat(faultService.isFaultEnabled()).isFalse();
    }

    @Test
    void toggle_flipsAndReturnsTheNewState() {
        assertThat(faultService.toggle()).isTrue();
        assertThat(faultService.isFaultEnabled()).isTrue();

        assertThat(faultService.toggle()).isFalse();
        assertThat(faultService.isFaultEnabled()).isFalse();
    }
}
