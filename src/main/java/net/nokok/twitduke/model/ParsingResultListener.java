package net.nokok.twitduke.model;

/**
 * MIT License. http://opensource.org/licenses/mit-license.php
 * Copyright (c) 2014 noko
 */
public interface ParsingResultListener {
    void success();

    void error(String errorMessage);
}
