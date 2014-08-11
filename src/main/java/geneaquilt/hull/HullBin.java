/**
 * Copyright (c) 2010-2014, Jean-Daniel Fekete, Pierre Dragicevic, and INRIA.
 * All rights reserved.
 *
 * Use of this source code is governed by a BSD-style license that can be
 * found in the LICENSE file.
 */
package geneaquilt.hull;
import edu.umd.cs.piccolo.util.PBounds;

/**
 * 
 * An object that is used as a vertical bin for computing hulls. These objects just have to store a
 * bin index and know their bounds, the rest of the work is done by Hull.
 * 
 * Currently, only PFam and IndiGeneration need to implement this interface.
 * 
 * @author dragice
 *
 */
public interface HullBin {

    /**
     * @return the hull-bin index
     */
	public int getHullBinIndex();

	/**
	 * Sets the hull-bin index
	 * @param index the new index
	 */
	public void setHullBinIndex(int index);
	
	/**
	 * @return the bounds
	 */
	public PBounds getFullBoundsReference();
}
