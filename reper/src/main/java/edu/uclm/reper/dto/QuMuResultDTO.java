package edu.uclm.reper.dto;

import edu.uclm.reper.model.Result;

public class QuMuResultDTO {
    private int id;
    private Result result;

    public QuMuResultDTO() {
    }

    public QuMuResultDTO(int id, Result result) {
        this.id = id;
        this.result = result;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Result getResult() {
        return result;
    }

    public void setResult(Result result) {
        this.result = result;
    }
}
