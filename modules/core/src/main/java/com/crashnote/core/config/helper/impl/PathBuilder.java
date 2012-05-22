/**
 *   Copyright (C) 2011-2012 Typesafe Inc. <http://typesafe.com>
 */
package com.crashnote.core.config.helper.impl;

import java.util.Stack;

import com.crashnote.core.config.helper.ConfigException;

final class PathBuilder {
    // the keys are kept "backward" (top of stack is end of path)
    final private Stack<String> keys;
    private Path result;

    PathBuilder() {
        keys = new Stack<String>();
    }

    private void checkCanAppend() {
        if (result != null)
            throw new ConfigException.BugOrBroken(
                    "Adding to PathBuilder after getting result");
    }

    void appendKey(final String key) {
        checkCanAppend();

        keys.push(key);
    }

    void appendPath(final Path path) {
        checkCanAppend();

        String first = path.first();
        Path remainder = path.remainder();
        while (true) {
            keys.push(first);
            if (remainder != null) {
                first = remainder.first();
                remainder = remainder.remainder();
            } else {
                break;
            }
        }
    }

    Path result() {
        // note: if keys is empty, we want to return null, which is a valid
        // empty path
        if (result == null) {
            Path remainder = null;
            while (!keys.isEmpty()) {
                final String key = keys.pop();
                remainder = new Path(key, remainder);
            }
            result = remainder;
        }
        return result;
    }
}
