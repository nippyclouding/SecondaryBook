package project.util.exception.file;

import project.util.exception.InvalidRequestException;

public class FileUploadException extends InvalidRequestException {

    public FileUploadException(String message) {
        super(message);
    }

    public FileUploadException(String message, Throwable cause) {
        super(message, cause);
    }
}
