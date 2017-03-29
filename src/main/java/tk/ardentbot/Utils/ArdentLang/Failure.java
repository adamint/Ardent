package tk.ardentbot.Utils.ArdentLang;

public final class Failure {
    public enum PermissionFailure {
        PROHIBITED_CODE_ACCESS, NO_PERMISSION
    }

    public enum CollectionsFailure {
        INVALID_INDEX, NOT_FOUND
    }

    public enum SyntaxFailure {
        SYNTAX, CASTING
    }

    public enum InputFailure {
        INVALID_SOURCE_TYPE
    }

    public enum ClassFailure {
        NO_METHOD_FOUND
    }
}
