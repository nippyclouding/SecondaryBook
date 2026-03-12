package project.util.exception.settlement;

import project.util.exception.InvalidRequestException;

public class SettlementException extends InvalidRequestException {

    public SettlementException(String message) {
        super(message);
    }
}
