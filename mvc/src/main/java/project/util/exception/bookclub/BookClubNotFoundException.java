package project.util.exception.bookclub;

import project.util.exception.NotFoundException;

public class BookClubNotFoundException extends NotFoundException {

    public BookClubNotFoundException(String message) {
        super(message);
    }
}
