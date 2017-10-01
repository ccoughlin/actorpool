/*
 * com.emphysic.myriad.actorpool.ROIPool
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
import com.emphysic.myriad.core.data.roi.ROIBundle;
import com.emphysic.myriad.network.ROIFinderPool;
import com.typesafe.config.Config;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;

/**
 *ROIPool - a pool of Region Of Interest (ROI) detectors.
 */
@Slf4j
public class ROIPool extends ActorPool {
    /**
     * ROI detector and its (optional) preprocessor
     */
    private ROIBundle roiBundle;

    public ROIPool(Config config) {
        super(config, "MyriadROIPool");
        log.info("Initiating ROIPool");
    }

    public boolean startup() {
        String roiPath = config.getString("roi.bundle");
        File roiFile = new File(roiPath);
        if (!roiFile.canRead()) {
            // Check the resources folder
            log.info("Didn't find ROIBundle, checking resources...");
            URL resource = ROIPool.class.getResource(roiPath);
            try {
                roiFile = Paths.get(resource.toURI()).toFile();
            } catch (URISyntaxException use) {
                log.error("Unable to construct URL from '" + resource + "'");
                return false;
            }
            if (!roiFile.canRead()) {
                log.error("Unable to read file '" + roiPath + "'");
                return false;
            }
        }
        int numWorkers = config.getInt("roi.num_workers");
        log.info("Using " + numWorkers + " workers");
        try {
            roiBundle = new ROIBundle();
            roiBundle.load(roiFile);
            log.info("Successfully loaded ROIBundle from '" + roiFile.getAbsolutePath() + "', starting pool");
            pool = system.actorOf(Props.create(ROIFinderPool.class, numWorkers, roiBundle));
            log.info("Pool listening at " + getPoolAddress());
            return true;
        } catch (IOException e) {
            log.error("Error encountered loading ROIBundle from '" + roiPath + "': " + e);
        }
        return false;
    }
}
