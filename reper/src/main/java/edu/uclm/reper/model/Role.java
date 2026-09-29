package edu.uclm.reper.model;

public enum Role {
    ADMINISTRADOR,
    USUARIO;

    public boolean isAdmin() {
        return this == ADMINISTRADOR;
    }
}
