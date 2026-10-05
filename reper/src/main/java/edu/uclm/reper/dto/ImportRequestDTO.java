package edu.uclm.reper.dto;

import java.util.List;

public class ImportRequestDTO {
    private String email;
    private List<ImportItemDTO> items;

    public ImportRequestDTO() {}

    public ImportRequestDTO(String email, List<ImportItemDTO> items) {
        this.email = email;
        this.items = items;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<ImportItemDTO> getItems() {
        return items;
    }

    public void setItems(List<ImportItemDTO> items) {
        this.items = items;
    }
}
