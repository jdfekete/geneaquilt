/**
 * Copyright (c) 2010-2014, Jean-Daniel Fekete, Pierre Dragicevic, and INRIA.
 * All rights reserved.
 *
 * Use of this source code is governed by a BSD-style license that can be
 * found in the LICENSE file.
 */
package geneaquilt.algorithms;

import geneaquilt.data.Network;

/**
 * <b>AbstractAlgorithm</b> is the base class for 
 * algorithms on a network.
 * 
 * @author Jean-Daniel Fekete
 * @version $Revision$
 */
public abstract class AbstractAlgorithm {
    protected Network network;
    
    /**
     * Creates an algorithm to work on the specified network
     * @param network the network
     */
    public AbstractAlgorithm(Network network) {
        this.network = network;
    }
    
    /**
     * @return the network
     */
    public Network getNetwork() {
        return network;
    }

    /**
     * Computes the algorithm.
     */
    abstract public void compute();

}
