package tk.ardentbot.Utils.ArdentLang;

import lombok.Getter;

public class ReturnWrapper<FailureReason, Object> {
    @Getter
    private FailureReason failureReason;
    @Getter
    private Object returnValue;

    public ReturnWrapper(FailureReason failureReason, Object returnValue) {
        this.failureReason = failureReason;
        this.returnValue = returnValue;
    }
}
