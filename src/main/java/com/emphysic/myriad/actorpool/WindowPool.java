/*
 * com.emphysic.myriad.actorpool.WindowPool
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

import akka.actor.Props;
import com.emphysic.myriad.network.SlidingWindowPool;
import com.typesafe.config.Config;
import lombok.extern.slf4j.Slf4j;

/**
 * WindowPool - a pool of "sliding window" operators.
 */
@Slf4j
public class WindowPool extends ActorPool {

    /**
     * Step size of the window in elements.
     */
    private int stepSize;
    /**
     * Width of the window.
     */
    private int windowWidth;
    /**
     * Height of the window.
     */
    private int windowHeight;

    public WindowPool(Config config) {
        super(config, "MyriadWindowPool");
        log.info("Instantiating WindowPool");
    }

    public boolean startup() {
        int numWorkers = config.getInt("window.num_workers");
        log.info("Using " + numWorkers + " workers");
        windowWidth = config.getInt("window.width");
        windowHeight = config.getInt("window.height");
        stepSize = config.getInt("window.step");
        log.info("Configuring sliding window " + windowWidth + "x" + windowHeight + " step size " + stepSize);
        try {
            pool = system.actorOf(Props.create(SlidingWindowPool.class, numWorkers, stepSize, windowWidth, windowHeight));
            log.info("Pool listening at " + getPoolAddress());
            return true;
        } catch (Exception e) {
            log.error("Error encountered creating sliding window pool : " + e);
        }
        return false;
    }

    /**
     * Returns the current step size of the window
     * @return step size of window in elements/pixels
     */
    public int getStepSize() {
        return stepSize;
    }

    /**
     * Returns the width of the sliding window
     * @return width of the window in elements/pixels
     */
    public int getWindowWidth() {
        return windowWidth;
    }

    /**
     * Returns the height of the sliding window
     * @return height of the window in elements/pixels
     */
    public int getWindowHeight() {
        return windowHeight;
    }
}
