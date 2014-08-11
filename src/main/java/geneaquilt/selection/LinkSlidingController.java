package geneaquilt.selection;

import java.awt.Color;
import java.awt.geom.Point2D;

import edu.umd.cs.piccolo.PCanvas;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.event.PInputEvent;
import geneaquilt.GeneaQuilt;
import geneaquilt.event.DisabablePanEventHandler;
import geneaquilt.nodes.PEdge;
import geneaquilt.nodes.PFam;
import geneaquilt.nodes.PIndi;
import geneaquilt.nodes.QuiltManager;
/**
 * <b>LinkSlidingController</b> implements the link sliding.
 * 
 * 
 * @author Juhee Bae
 */
public class LinkSlidingController  extends MouseSelectionController {
//	private final QuiltManager manager;
//	private final DisabablePanEventHandler panEventHandler;
//	private Selection currentSelection = null;

	private double startX = 0;
	private double startY = 0;
	private double endX = 0;
	private double endY = 0;
	private int ancestor = 2; //# of direction of ancestor: up and left
	private int descendant = 2; //# of direction of descendant: right and down
	private int chosenItem = 0; //edge, indi, fam
	
	private String direction = "";
	private String suffix = "family";	
	private String savedDirection = "ancestor";
	
	private PNode to;
	private PNode from;	
	private boolean draggedMouse;
	private boolean clickedMouse;
	
	public LinkSlidingController(QuiltManager manager, DisabablePanEventHandler panEventHandler) {
		super(manager, panEventHandler);
//		this.manager = manager;
//		this.panEventHandler = panEventHandler;
	}
	
	public void mousePressed(PInputEvent event) {
		
		super.mousePressed(event);
		
//		currentSelection = null;
//		updateSelection(event);
		clickedMouse = true;
		
		//remove the colors of previously focused nodes
		if(to!=null)to.setPaint(Color.white);
		if(from!=null)from.setPaint(Color.white);		
	
		Point2D p = event.getPosition();
		//save the current position
		startX = p.getX(); startY = p.getY();

		PNode pnode = event.getPickedNode();
		if (pnode instanceof PEdge) {
			chosenItem = 0;
	    }else if (pnode instanceof PIndi) {
	        chosenItem = 1;
	    }else if (pnode instanceof PFam) {
	        chosenItem = 2;
	    }else{
	        chosenItem = 3;
	    }

		draggedMouse=false;
		 
//		if (currentSelection != null) {
//			panEventHandler.setEnabled(false);	
//		} else{
//			super.mousePressed(event);
//		}
	}
	

	public void mouseDragged(PInputEvent event) {
		
		super.mouseDragged(event);

		//drag and change color of the node - true
		draggedMouse = true;
//		if (currentSelection != null) {
//			updateSelection(event);
//		} else{
//			super.mouseDragged(event);
//		}
	}

	/*private void updateSelection(PInputEvent event) {
		// TODO Auto-generated method stub

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
				} else {
					currentSelection.setSelectedObject(node);
				}					

				currentSelection.setHighlightMode(HighlightMode.HIGHLIGHT_ALL);
			}
		}
	}*/

	// Like event.getPickedNode() but does not grab objects during drags
	/*protected PNode getPickedNode(PInputEvent event) {
		PPickPath picked = new PPickPath(event.getCamera(), new PBounds(event.getPosition().getX(), event.getPosition().getY(), 1, 1));
		manager.fullPick(picked);
		return picked.getPickedNode();
	}*/
	
	public void mouseReleased(PInputEvent event) {
		
		super.mouseReleased(event);
		
		if (currentSelection != null) {	
			//move camera when mouse released
			decideDirection(event);			
			panEventHandler.setEnabled(true);
		}
	}
	
	/*public void mouseClicked(PInputEvent event) {
		if (currentSelection == null && event.isLeftMouseButton() && !event.isShiftDown()) {
			SelectionManager selections = manager.getSelectionManager();
			selections.clearSelections();
		} else if(event.isRightMouseButton()){
			SelectionManager selections = manager.getSelectionManager();
			selections.clearSelections();
		} else { 
			super.mouseClicked(event);
		}
	}*/
	
	private void decideDirection(PInputEvent event) {
		// TODO Auto-generated method stub
		GeneaQuilt quilt = GeneaQuilt.getQuilt();	
		PCanvas canvas = quilt.getCanvas();
		if(draggedMouse){
			//decide direction of mouse movement and move camera to ancestor or descendant		
			Point2D p = event.getPosition();
			
			//grab position when mouse is released
			endX = p.getX();
			endY = p.getY();

			//inclination of mouse movement
			double vx = (endX - startX);
			double vy = (endY - startY);
			double incline = vy / vx;		
					
			double anc_x = -1; double anc_y= -1;
			double des_x = 1; double des_y= 1;
			double dotproduct = (anc_x * vx + anc_y * vy);

			//dot product > 0 if ancestor, else descendant
			if( dotproduct >= 0 ){ 
				//System.out.println("ancestor");
				suffix = "ancestor";
			}else{
				//System.out.println("descendant");
				suffix = "descendant";
			}
			//index of ancestor or descendant? 0 OR 1
			int index=0;
				
				//<---A <---B---> A --->    (A: left, B: up) or (A: right, B: down)
				//----------------------
				//   -1           1
				do{
					if(index==0 && (incline < -1 || incline > 1)){
						//smaller than -1, greater than 1
						direction = index + suffix; //print this to check direction
						break;
					}else if(index==1 && -1 <= incline && incline <= 1){
						//in between
						direction = index + suffix;
						break;
					}else{ //exceptional direction

					}							
					index++; //status 0 or 1
				}while(index < ancestor);	

			//get next edge, indi, or fam  
			if (suffix.equalsIgnoreCase("descendant")){
				  to = new PNode();
			      if ( chosenItem == 0) {
			            PEdge pedge = (PEdge) event.getPickedNode();
			            to = pedge.getEdge().getFromVertex().getNode();
			        }
			        else if (chosenItem == 1) {
			            PIndi pindi = (PIndi) event.getPickedNode();
			            to = currentSelection.findNextSuccessor(pindi.getIndi());
			        }
			        else if (chosenItem == 2) {		        
			            PFam pfam = (PFam)event.getPickedNode();
			            to = currentSelection.findNextSuccessor(pfam.getFam());
			        }else{
			        }
				if(to!=null){  
					canvas.getCamera().animateViewToCenterBounds(to.getFullBounds(), false, 500);
					to.setPaint(Color.yellow);
				}
			}//get previous edge, indi, or fam  
			else if(suffix.equalsIgnoreCase("ancestor")){
				  from = new PNode();
			      if ( chosenItem == 0) {
			            PEdge pedge = (PEdge) event.getPickedNode();
			            from = pedge.getEdge().getToVertex().getNode();
			        }
			        else if (chosenItem == 1) {
			            PIndi pindi = (PIndi) event.getPickedNode();
			            from = currentSelection.findNextPredecessor(pindi.getIndi());
			        }
			        else if (chosenItem == 2) {		        
			            PFam pfam = (PFam)event.getPickedNode();
			            from = currentSelection.findNextPredecessor(pfam.getFam());
			        }else{
			        }
				if(from!=null){ 
					canvas.getCamera().animateViewToCenterBounds(from.getFullBounds(), false, 500);		
					from.setPaint(Color.green);
				}				
			}

//			manager.saveDirection(suffix); 
		}else {
			//if the mouse is not dragged, then get the direction saved		
			
//			if(manager.getSavedDirection()!=null) savedDirection = manager.getSavedDirection();
			
			//get next edge, indi, or fam  
			if (savedDirection.equalsIgnoreCase("descendant")){
				  to = new PNode();
			      if ( chosenItem == 0) {
			            PEdge pedge = (PEdge) event.getPickedNode();
			            to = pedge.getEdge().getFromVertex().getNode();
			        }
			        else if (chosenItem == 1) {
			            PIndi pindi = (PIndi) event.getPickedNode();
			            to = currentSelection.findNextSuccessor(pindi.getIndi());
			        }
			        else if (chosenItem == 2) {		        
			            PFam pfam = (PFam)event.getPickedNode();
			            to = currentSelection.findNextSuccessor(pfam.getFam());
			        }else{
			        }
				if(to!=null){  
					canvas.getCamera().animateViewToCenterBounds(to.getFullBounds(), false, 500);
					to.setPaint(Color.yellow);
				}
			}//get previous edge, indi, or fam  
			else if(savedDirection.equalsIgnoreCase("ancestor")){
				  from = new PNode();
			      if ( chosenItem == 0) {
			            PEdge pedge = (PEdge) event.getPickedNode();
			            from = pedge.getEdge().getToVertex().getNode();
			        }
			        else if (chosenItem == 1) {
			            PIndi pindi = (PIndi) event.getPickedNode();
			            from = currentSelection.findNextPredecessor(pindi.getIndi());
			        }
			        else if (chosenItem == 2) {		        
			            PFam pfam = (PFam)event.getPickedNode();
			            from = currentSelection.findNextPredecessor(pfam.getFam());
			        }else{
			        }
				if(from!=null){ 
					canvas.getCamera().animateViewToCenterBounds(from.getFullBounds(), false, 500);		
					from.setPaint(Color.green);
				}			
			}
		}
		

	}
}
