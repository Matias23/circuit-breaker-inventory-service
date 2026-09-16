package com.mricotta.circuitbreaker.inventory.service;

public interface FaultService {

    boolean toggle();

    boolean isFaultEnabled();
}
