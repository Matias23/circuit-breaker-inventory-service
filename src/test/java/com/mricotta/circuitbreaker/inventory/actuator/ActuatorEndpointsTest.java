package com.mricotta.circuitbreaker.inventory.actuator;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.mricotta.circuitbreaker.inventory.service.FaultService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class ActuatorEndpointsTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private FaultService faultService;

    // The fault flag is a singleton shared by the cached context, so leave it off for other tests.
    @AfterEach
    void resetFault() {
        if (faultService.isFaultEnabled()) {
            faultService.toggle();
        }
    }

    @Test
    void health_withoutFault_isUpWithComponents() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.components.fault.status").value("UP"))
                .andExpect(jsonPath("$.components.fault.details.faultEnabled").value(false))
                .andExpect(jsonPath("$.components.db.status").value("UP"));
    }

    @Test
    void health_afterTogglingFault_isDownWith503() throws Exception {
        mockMvc.perform(post("/v1/inventory/toggle-fault")).andExpect(status().isOk());

        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.status").value("DOWN"))
                .andExpect(jsonPath("$.components.fault.status").value("DOWN"))
                .andExpect(jsonPath("$.components.fault.details.faultEnabled").value(true));
    }

    @Test
    void livenessProbe_isUp() throws Exception {
        mockMvc.perform(get("/actuator/health/liveness"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    void info_exposesAppProperties() throws Exception {
        mockMvc.perform(get("/actuator/info"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.app.name").value("circuit-breaker-inventory-service"));
    }
}
