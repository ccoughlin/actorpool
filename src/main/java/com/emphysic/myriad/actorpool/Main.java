/*
 * com.emphysic.myriad.actorpool.Main
 *
 * Copyright (c) 2017 Emphysic LLC.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.emphysic.myriad.actorpool;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * Main entry point for a "run forever" Myriad worker pool.  Reads a config file and starts an ActorPool.
 */
@Slf4j
public class Main {
    public static void main(String[] args) {
        if (args.length == 0) {
            System.err.println("Usage: <appname> /path/to/config/file");
            System.err.println("Where the single argument is the full path to a Typesafe config file.");
            System.exit(1);
        }
        File cfgFile = new File(args[0]);
        if (!cfgFile.canRead()) {
            log.error("Unable to read configuration file '" + cfgFile + "', exiting");
            System.exit(1);
        } else {
            log.info("Using configuration file '" + cfgFile + "'");
        }
        Config cfg = ConfigFactory.parseFile(cfgFile);
        String mode = cfg.getString("actorpool.mode");
        String clsPath = mode + ".cls";
        try {
            Class<?> clazz = Class.forName(cfg.getString(clsPath));
            Constructor<?> constructor = clazz.getConstructor(Config.class);
            Object pool = constructor.newInstance(cfg);
            log.info("Successfully instantiated pool " + pool.toString() + ", waiting for connections.");
        } catch (ClassNotFoundException cnfe) {
            log.error("Unable to find class '" + clsPath + "': " + cnfe.getLocalizedMessage());
            System.exit(1);
        } catch (NoSuchMethodException nsme) {
            log.error("Encountered error retrieving constructor: " + nsme.getLocalizedMessage());
            System.exit(1);
        } catch (IllegalAccessException | InstantiationException | InvocationTargetException e) {
            log.error("Error encountered instantiating pool: " + e.getLocalizedMessage());
            System.exit(1);
        }
    }
}
