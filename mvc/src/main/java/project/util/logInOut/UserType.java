package project.util.logInOut;

public enum UserType {
    ADMIN(1),
    MEMBER(60);

    private final int timeoutSeconds;

    UserType(int timeoutSeconds) {
        this.timeoutSeconds = timeoutSeconds;
    }

    public int getTimeoutSeconds() {
        return timeoutSeconds;
    }
}
