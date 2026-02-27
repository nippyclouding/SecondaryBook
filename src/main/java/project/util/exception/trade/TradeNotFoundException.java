package project.util.exception.trade;

import project.util.exception.NotFoundException;

public class TradeNotFoundException extends NotFoundException {

    public TradeNotFoundException(String message) {
        super(message);
    }
}
