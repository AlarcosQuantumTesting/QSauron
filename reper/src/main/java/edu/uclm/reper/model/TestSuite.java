package edu.uclm.reper.model;

import jakarta.persistence.*;

import java.util.Date;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonManagedReference;

@Entity
@Table(name = "test_suites")
public class TestSuite {
    @Id
    @Column(name = "id")
    private String id;

    @OneToMany(mappedBy = "testSuite", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference("testSuite-testCases")
    private List<TestCase> testCases;

    @Column(name = "mode")
    private String mode;

    @Column(name = "test_file_content")
    private String test_file_content;

    @Column(name = "error_range")
    private int error_range;

    @Column(name = "cut_image_base64")
    private String cut_image_base64;

    @Column(name = "qtcc_image_base64")
    private String qtcc_image_base64;

    @Column(name = "image_need_updated")
    private boolean image_need_updated;

    @Column(name = "images_updated_at")
    private Date images_updated_at;

    // Constructors
    public TestSuite() {
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<TestCase> getTestCases() {
        return testCases;
    }

    public void setTestCases(List<TestCase> testCases) {
        this.testCases = testCases;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public String getTest_file_content() {
        return test_file_content;
    }

    public void setTest_file_content(String test_file_content) {
        this.test_file_content = test_file_content;
    }

    public int getError_range() {
        return error_range;
    }

    public void setError_range(int error_range) {
        this.error_range = error_range;
    }

    public String getCut_image_base64() {
        return cut_image_base64;
    }

    public void setCut_image_base64(String cut_image_base64) {
        this.cut_image_base64 = cut_image_base64;
    }

    public String getQtcc_image_base64() {
        return qtcc_image_base64;
    }

    public void setQtcc_image_base64(String qtcc_image_base64) {
        this.qtcc_image_base64 = qtcc_image_base64;
    }

    public boolean isImage_need_updated() {
        return image_need_updated;
    }

    public void setImage_need_updated(boolean image_need_updated) {
        this.image_need_updated = image_need_updated;
    }

    public Date getImages_updated_at() {
        return images_updated_at;
    }

    public void setImages_updated_at(Date images_updated_at) {
        this.images_updated_at = images_updated_at;
    }

}
