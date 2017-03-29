package tk.ardentbot.Utils.JLAdditions;

import tk.ardentbot.Core.LoggingUtils.BotException;
import tk.ardentbot.Utils.ArdentLang.Failure;
import tk.ardentbot.Utils.ArdentLang.ReturnWrapper;
import tk.ardentbot.Utils.ArdentLang.SafeType;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;

public class FieldableList<E> extends ArrayList<E> implements SafeType {
    public FieldableList() {
        super();
    }

    @SafeVarargs
    public FieldableList(E... originals) {
        super(Arrays.asList(originals));
    }

    public ReturnWrapper contains(String fieldName, String value) {
        if (size() == 0) return new ReturnWrapper<>(Failure.CollectionsFailure.NOT_FOUND, null);
        else {
            final boolean[] toReturn = {false};
            forEach(e -> {
                for (Field f : e.getClass().getDeclaredFields()) {
                    try {
                        Object o = f.get(e);
                        if (f.getName().equalsIgnoreCase(fieldName) && o instanceof String && ((String) o).equalsIgnoreCase(value))
                            toReturn[0] = true;
                    }
                    catch (IllegalAccessException e1) {
                        new BotException(e1);
                    }
                }
            });
            return toReturn[0] ? new ReturnWrapper<>(null, true) : new ReturnWrapper<>(Failure.CollectionsFailure.NOT_FOUND, false);
        }
    }

    public ReturnWrapper containsGet(String fieldName, String value) {
        if (size() == 0) return new ReturnWrapper<>(Failure.CollectionsFailure.NOT_FOUND, null);
        else {
            final boolean[] toReturn = {false};
            final Object[] returnObject = {null};
            forEach(e -> {
                for (Field f : e.getClass().getDeclaredFields()) {
                    try {
                        Object o = f.get(e);
                        if (f.getName().equalsIgnoreCase(fieldName) && o instanceof String && ((String) o).equalsIgnoreCase(value))
                            toReturn[0] = true;
                        returnObject[0] = e;
                    }
                    catch (IllegalAccessException e1) {
                        new BotException(e1);
                    }
                }
            });
            return (returnObject[0] != null && toReturn[0]) ? new ReturnWrapper<>(null, returnObject) : new ReturnWrapper<>(Failure
                    .CollectionsFailure
                    .NOT_FOUND, null);
        }
    }
}
