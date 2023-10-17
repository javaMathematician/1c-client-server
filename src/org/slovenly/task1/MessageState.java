package org.slovenly.task1;

public enum MessageState {
    SENT, RECEIVED;

    @Override
    public String toString() {
        if (this == RECEIVED) {
            return "________!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!RECEIVED))))))))))))))))))))))))";
        }

        return super.toString();
    }
}
