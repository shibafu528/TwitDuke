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
package net.nokok.twitduke.pluginsupport.util;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Optional;
import java.util.Properties;
import net.nokok.twitduke.pluginsupport.plugin.Plugin;
import net.nokok.twitduke.pluginsupport.plugin.PluginInfo;
import net.nokok.twitduke.pluginsupport.plugin.PluginPermission;

public class FileToPlugin {

    public static Optional<Plugin> encode(File file) {
        String propertyPath = file.getAbsolutePath().replace(".js", ".properties");
        final Properties properties = new Properties();
        Plugin plugin;
        try {
            properties.load(new FileReader(propertyPath));
            plugin = new Plugin(file.getAbsolutePath(), new PluginInfo() {

                @Override
                public String author() {
                    return properties.getProperty("author");
                }

                @Override
                public String description() {
                    return properties.getProperty("description");
                }

                @Override
                public String name() {
                    return properties.getProperty("name");
                }

                @Override
                public PluginPermission permission() {
                    return PluginPermission.parsePermission(properties.getProperty("permission"));
                }

                @Override
                public String version() {
                    return properties.getProperty("version");
                }
            });
        } catch (IOException e) {
            return Optional.empty();
        }
        return Optional.of(plugin);
    }
}
