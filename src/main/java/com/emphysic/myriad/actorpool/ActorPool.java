/*
 * com.emphysic.myriad.actorpool.ActorPool
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

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import com.typesafe.config.Config;
import lombok.extern.slf4j.Slf4j;

/**
 * ActorPool - a LinkedWorkerPool designed to run forever on a server.
 */
@Slf4j
public abstract class ActorPool {
    protected ActorSystem system;
    protected Config config;
    protected ActorRef pool;
    protected ActorRef next;
    protected  int numWorkers = 16;
    protected  String poolName;

    /**
     * Constructor.
     * @param config Typesafe Config object to use as basis of configuration
     * @param poolName name to assign this pool
     */
    public ActorPool(Config config, String poolName) {
        this.config = config;
        system = ActorSystem.create(poolName, config);
        if (!startup()) {
            log.error("Pool instantiation failed!");
            throw new RuntimeException("Pool instantiation failed");
        }
    }

    public ActorPool(Config config) {
        this(config, "ActorPool");
    }

    /**
     * Initialize and start the pool.
     * @return true if initialization was successful, false otherwise
     */
    public abstract boolean startup();

    /**
     * Retrieves the address of the worker pool.
     * @return String of the form akka.tcp://[PoolName]@[System URL]:[System Port]/user/$a,
     * if both system and pool are configured.  Otherwise returns just the system address if the pool is not configured,
     * or an empty String if neither are configured.
     */
    public String getPoolAddress() {
        StringBuilder sb = new StringBuilder();
        if (system != null) {
            sb.append(system.provider().getDefaultAddress());
            if (pool != null) {
                sb.append(pool.path().toStringWithoutAddress());
            }
        }
        return sb.toString();
    }

    /**
     * Sets the "next" link in the processing pipeline.  Results from this pool are sent to this Actor.
     * @param next Reference to next step in the processing.
     */
    public void setNext(ActorRef next) {
        if (pool != null) {
            this.next = next;
            pool.tell(this.next, system.guardian());
        }
    }

    /**
     * Returns a reference to the next step in the processing.
     * @return next step, or null if not set.
     */
    public ActorRef getNext() {
        return next;
    }

    /**
     * Number of workers in the pool.
     * @return number of workers
     */
    public int getNumWorkers() {
        return numWorkers;
    }

    /**
     * Sets the number of workers in the pool.
     * @param numWorkers number of workers
     */
    public void setNumWorkers(int numWorkers) {
        this.numWorkers = numWorkers;
    }

    /**
     * Retrieves the name of the pool
     * @return name of the pool
     */
    public String getPoolName() {
        return poolName;
    }

    /**
     * Sets the name of the pool
     * @param poolName name of the pool
     */
    public void setPoolName(String poolName) {
        this.poolName = poolName;
    }
}
