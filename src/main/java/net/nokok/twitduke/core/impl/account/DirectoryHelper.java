/*
 * The MIT License
 *
 * Copyright 2014 noko
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
package net.nokok.twitduke.core.impl.account;

import java.io.File;
import net.nokok.twitduke.core.api.io.Paths;

public class DirectoryHelper {

    public static File createAccountDirectory(String accountName) {
        File dir = getAccountDirectory(accountName);
        dir.mkdir();
        new File(String.join(File.separator, dir.getAbsolutePath(), "plugins")).mkdir();
        return dir;
    }

    public static File getAccountDirectory(String accountName) {
        return new File(String.join(File.separator, Paths.TWITDUKE_HOME, accountName));
    }

    public static void createTwitDukeDirectories() {
        createDirectory(Paths.TWITDUKE_HOME);
        createDirectory(Paths.PLUGIN_DIR);
        createDirectory(Paths.LOG_DIR);
    }

    private static void createDirectory(String directoryPath) {
        File file = new File(directoryPath);
        boolean result = file.mkdir();
        if ( !result ) {
            throw new RuntimeException("ディレクトリの作成に失敗しました:" + directoryPath);
        }
    }
}