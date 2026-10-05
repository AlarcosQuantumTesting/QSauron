package edu.uclm.reper.services;

public class ImportException extends RuntimeException {
    public static final String INVALID_FORMAT = "INVALID_FORMAT";
    public static final String INVALID_EXTENSION = "INVALID_EXTENSION";
    public static final String MISSING_NAME = "MISSING_NAME";
    public static final String EMPTY_FILE = "EMPTY_FILE";
    public static final String FILE_TOO_LARGE = "FILE_TOO_LARGE";
    public static final String NOT_TEXT = "NOT_TEXT";
    public static final String MALWARE_DETECTED = "MALWARE_DETECTED";
    public static final String INVALID_CONTENT = "INVALID_CONTENT";
    public static final String CONVERSION_FAILED = "CONVERSION_FAILED";
    public static final String INVALID_CIRCUIT = "INVALID_CIRCUIT";
    public static final String TOO_MANY_QUBITS = "TOO_MANY_QUBITS";
    public static final String INTERNAL_ERROR = "INTERNAL_ERROR";
    public static final String BAD_REQUEST = "BAD_REQUEST";
    public static final String TOO_MANY_FILES = "TOO_MANY_FILES";
    public static final String TOTAL_TOO_LARGE = "TOTAL_TOO_LARGE";
    public static final String ANTIVIRUS_UNAVAILABLE = "ANTIVIRUS_UNAVAILABLE";
    public static final String CONVERTER_UNAVAILABLE = "CONVERTER_UNAVAILABLE";

    private final String errorCode;
    private final int httpStatus;

    public ImportException(String errorCode, String message) {
        this(errorCode, message, 422);
    }

    public ImportException(String errorCode, String message, int httpStatus) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public int getHttpStatus() {
        return httpStatus;
    }
}
