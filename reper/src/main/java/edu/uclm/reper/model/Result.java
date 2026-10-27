package edu.uclm.reper.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Enumeration representing the result of a mutant execution
 */
public enum Result {
    @JsonProperty("Killed")
    KILLED,
    @JsonProperty("Alive")
    ALIVE,
    @JsonProperty("Zombie")
    ZOMBIE
}
