/**
 * Copyright (c) 2010-2014, Jean-Daniel Fekete, Pierre Dragicevic, and INRIA.
 * All rights reserved.
 *
 * Use of this source code is governed by a BSD-style license that can be
 * found in the LICENSE file.
 */
package geneaquilt.selection;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.event.PBasicInputEventHandler;
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.util.PBounds;
import edu.umd.cs.piccolo.util.PPickPath;
import geneaquilt.event.DisabablePanEventHandler;
import geneaquilt.nodes.QuiltManager;

/**
 * <b>MouseSelectionController</b> implements the selection
 * and Pan.
 * 
 * @author Pierre Dragicevic
 */
public class MouseSelectionController extends PBasicInputEventHandler {
    protected final QuiltManager manager;
	protected final DisabablePanEventHandler panEventHandler;
	protected Selection currentSelection = null;
	protected boolean selectionCreated = false;
	protected boolean selectionEnabled = true;

	/**
	 * Creates a controller on a quilt manager and a pan event handler
	 * @param manager the quilt manager
	 * @param panEventHandler the pan event handler
	 */
	public MouseSelectionController(QuiltManager manager, DisabablePanEventHandler panEventHandler) {
		this.manager = manager;
		this.panEventHandler = panEventHandler;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void mousePressed(PInputEvent event) {
		currentSelection = null;
		selectionCreated = false;
		updateSelection(event);

		if (currentSelection != null) {
			panEventHandler.setEnabled(false);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void mouseDragged(PInputEvent event) {
		if (currentSelection != null) {
			updateSelection(event);
		}
	}

	// Like event.getPickedNode() but does not grab objects during drags
	protected PNode getPickedNode(PInputEvent event) {
		PPickPath picked = new PPickPath(event.getCamera(), new PBounds(event.getPosition().getX(), event.getPosition().getY(), 1, 1));
		manager.fullPick(picked);
		return picked.getPickedNode();
	}
	
	protected void updateSelection(PInputEvent event) {
		if (event.isLeftMouseButton()) {

			PNode node = getPickedNode(event);
			
			SelectionManager selections = manager.getSelectionManager();
			
			// Drag an existing selection?
			if (currentSelection == null && selections.isSelected(node)) {
				currentSelection = selections.getLastSelection(node);
			}
			
			if (selections.isSelectable(node)) {
				if (currentSelection == null) {
					if (!event.isShiftDown())
						selections.clearSelections();
					currentSelection = selections.select(node);
					selectionCreated = true;
				} else {
					currentSelection.setSelectedObject(node);
				}
			}
		}	
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void mouseReleased(PInputEvent event) {
		if (currentSelection != null) {
			panEventHandler.setEnabled(true);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void mouseClicked(PInputEvent event) {
		if (currentSelection == null && event.isLeftMouseButton() && !event.isShiftDown()) {
			SelectionManager selections = manager.getSelectionManager();
			selections.clearSelections();
		} else if (currentSelection != null && !selectionCreated && !event.isShiftDown()) {
			currentSelection.toggleHighlightMode();
		}
	}
}
