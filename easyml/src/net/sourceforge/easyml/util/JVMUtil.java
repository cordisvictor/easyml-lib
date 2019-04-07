/*
 * Copyright 2012 Victor Cordis
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Please contact the author ( cordis.victor@gmail.com ) if you need additional
 * information or have any questions.
 */
package net.sourceforge.easyml.util;

import java.util.Optional;

/**
 * JVMUtil utility class used to detect JVM settings.
 *
 * @author Victor Cordis ( cordis.victor at gmail.com)
 * @version 1.5.0
 * @since 1.5.0
 */
public final class JVMUtil {

    private static final int javaMajorVersion = parseJavaVersion();

    private static int parseJavaVersion() {
        final String javaVersion = System.getProperty("java.version");
        return parseOracleOldOrNew(javaVersion)
                .orElseGet(() -> parseNonOracle(javaVersion).orElse(8));
    }

    private static Optional<Integer> parseOracleOldOrNew(String javaVersion) {
        String[] numbers = javaVersion.split("[._]");
        try {
            int number0 = Integer.parseInt(numbers[0]);
            if (numbers.length > 1 && number0 == 1) {
                return Optional.of(Integer.parseInt(numbers[1]));
            }
            return Optional.of(number0);
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    private static Optional<Integer> parseNonOracle(String javaVersion) {
        StringBuilder number0 = new StringBuilder();
        int len = javaVersion.length();
        int i = 0;
        while (i < len && Character.isDigit(javaVersion.charAt(i))) {
            number0.append(javaVersion.charAt(i));
            i++;
        }
        try {
            return Optional.of(Integer.parseInt(number0.toString()));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    public static int getJavaMajorVersion() {
        return javaMajorVersion;
    }

    private JVMUtil() {
    }
}
