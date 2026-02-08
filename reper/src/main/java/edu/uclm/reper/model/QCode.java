package edu.uclm.reper.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "QCode")
//@JsonIgnoreProperties(ignoreUnknown = true)
public class QCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "platform")
    private String platform;

    @Column(name = "code", columnDefinition = "TEXT")
    private String code;

    public QCode() {
    }

    public QCode(String platform, String code) {
        this.platform = platform;
        this.code = code;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
