package io.github.jpautils.jpaimporter.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Util {
    public static boolean isOdd(int i) {
        return i % 2 != 0;
    }

    public static List<String> splitByCharacter(String string, char splitCharacter) {
        List<String> immutableListResult = Arrays.asList(string.split(String.valueOf(splitCharacter), -1));
        List<String> mutableListResult = new ArrayList<>(immutableListResult);
        return mutableListResult;
    }

    public static boolean stringIsNotBlank(String string) {
        return string != null && !string.trim().isEmpty();
    }
}
