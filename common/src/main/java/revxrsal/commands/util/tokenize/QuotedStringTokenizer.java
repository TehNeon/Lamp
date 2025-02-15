/*
 * This file is part of SpongeAPI, licensed under the MIT License (MIT).
 *
 * Copyright (c) SpongePowered <https://www.spongepowered.org>
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package revxrsal.commands.util.tokenize;

import revxrsal.commands.exception.ArgumentParseException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Parser for converting a quoted string into a list of arguments.
 *
 * <p>Grammar is roughly (yeah, this is not really a proper grammar but it gives
 * you an idea of what's happening:</p>
 *
 * <blockquote><pre> WHITESPACE = Character.isWhiteSpace(codePoint)
 * CHAR := (all unicode)
 * ESCAPE := '\' CHAR
 * QUOTE = ' | "
 * UNQUOTED_ARG := (CHAR | ESCAPE)+ WHITESPACE
 * QUOTED_ARG := QUOTE (CHAR | ESCAPE)+ QUOTE
 * ARGS := ((UNQUOTED_ARG | QUOTED_ARG) WHITESPACE+)+</pre></blockquote>
 */
public final class QuotedStringTokenizer {

    public static final List<String> EMPTY_TEXT = Collections.singletonList("");

    private QuotedStringTokenizer() {}

    private static final int CHAR_BACKSLASH = '\\';
    private static final int CHAR_SINGLE_QUOTE = '\'';
    private static final int CHAR_DOUBLE_QUOTE = '"';

    /**
     * Returns a list of tokens from parsing the given input,
     * respecting quotes and breaks.
     *
     * @param arguments Argument string to parse
     * @return A list of tokens.
     */
    public static List<String> tokenize(String arguments) {
        if (arguments.length() == 0) {
            return Collections.emptyList();
        }

        final TokenizerState state = new TokenizerState(arguments);
        List<String> returnedArgs = new ArrayList<>(arguments.length() / 4);
        while (state.hasMore()) {
            skipWhiteSpace(state);
            String arg = nextArg(state);
            returnedArgs.add(arg);
        }
        return returnedArgs;
    }

    /**
     * Returns a list of tokens from parsing the given input,
     * respecting quotes and breaks.
     *
     * @param arguments Argument string to parse
     * @return A list of tokens.
     */
    public static List<String> tokenizeUnsafe(String arguments) {
        if (arguments.length() == 0) {
            return EMPTY_TEXT;
        }
        return tokenize(arguments);
    }

    // Parsing methods

    private static void skipWhiteSpace(TokenizerState state) throws ArgumentParseException {
        if (!state.hasMore()) {
            return;
        }
        while (state.hasMore() && Character.isWhitespace(state.peek())) {
            state.next();
        }
    }

    private static String nextArg(TokenizerState state) throws ArgumentParseException {
        StringBuilder argBuilder = new StringBuilder();
        if (state.hasMore()) {
            int codePoint = state.peek();
            if (codePoint == CHAR_DOUBLE_QUOTE || codePoint == CHAR_SINGLE_QUOTE) {
                // quoted string
                parseQuotedString(state, codePoint, argBuilder);
            } else {
                parseUnquotedString(state, argBuilder);
            }
        }
        return argBuilder.toString();
    }

    private static void parseQuotedString(TokenizerState state, int startQuotation, StringBuilder builder) throws ArgumentParseException {
        // Consume the start quotation character
        int nextCodePoint = state.next();
        if (nextCodePoint != startQuotation) {
            throw state.createException(String.format("Actual next character '%c' did not match expected quotation character '%c'",
                    nextCodePoint, startQuotation));
        }

        while (true) {
            if (!state.hasMore()) {
                return;
            }
            nextCodePoint = state.peek();
            if (nextCodePoint == startQuotation) {
                state.next();
                return;
            } else if (nextCodePoint == CHAR_BACKSLASH) {
                parseEscape(state, builder);
            } else {
                builder.appendCodePoint(state.next());
            }
        }
    }

    private static void parseUnquotedString(TokenizerState state, StringBuilder builder) throws ArgumentParseException {
        while (state.hasMore()) {
            int nextCodePoint = state.peek();
            if (Character.isWhitespace(nextCodePoint)) {
                return;
            } else if (nextCodePoint == CHAR_BACKSLASH) {
                parseEscape(state, builder);
            } else {
                builder.appendCodePoint(state.next());
            }
        }
    }

    private static void parseEscape(TokenizerState state, StringBuilder builder) throws ArgumentParseException {
        state.next(); // Consume \
        builder.appendCodePoint(state.next());
    }

    private static class TokenizerState {

        private final String buffer;
        private int index = -1;

        TokenizerState(String buffer) {
            this.buffer = buffer;
        }

        // Utility methods
        public boolean hasMore() {
            return index + 1 < buffer.length();
        }

        public int peek() throws ArgumentParseException {
            if (!hasMore()) {
                throw createException("Buffer overrun while parsing args");
            }
            return buffer.codePointAt(index + 1);
        }

        public int next() throws ArgumentParseException {
            if (!hasMore()) {
                throw createException("Buffer overrun while parsing args");
            }
            return buffer.codePointAt(++index);
        }

        public ArgumentParseException createException(String message) {
            return new ArgumentParseException(message, buffer, index);
        }

        public int getIndex() {
            return index;
        }
    }

}
