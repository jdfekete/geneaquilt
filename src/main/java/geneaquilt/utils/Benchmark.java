package geneaquilt.utils;

import edu.umd.cs.piccolo.PCanvas;
import edu.umd.cs.piccolo.PNode;
import geneaquilt.BirdsEyeView;
import geneaquilt.GeneaQuilt;
import geneaquilt.data.Vertex;
import geneaquilt.nodes.QuiltManager;
import geneaquilt.selection.SelectionManager;

import java.util.Collection;
import java.util.regex.Pattern;

/**
 * Computes a <b>Benchmark</b>
 * 
 * @author Pierre Dragicevic
 */
public class Benchmark {
	
	static String taskname = null;
	static long t0 = 0;

	/**
	 * 
	 * This class is used to compare the performance of different rendering methods.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		
//		beginTask("load");
		GeneaQuilt.main(new String[]{"data/royal92.ged"});
//		endTask();
		
		GeneaQuilt.getFrame().setLocation(GeneaQuilt.getFrame().getX(), 0);
		
		final Thread t = new Thread() {
			public void run() {
				doBenchmark();
			}
		};
		t.start();
	}
	
	static void doBenchmark() {
		
		wait(2);

//		JFrame frame = GeneaQuilt.getFrame();
		GeneaQuilt quilt = GeneaQuilt.getQuilt();
		QuiltManager root = quilt.getManager();
		SelectionManager selection = root.getSelectionManager();
		PCanvas canvas = quilt.getCanvas();
		BirdsEyeView bev = quilt.getBev();
		
//		beginTask("search 1");
		Collection<Vertex> v = quilt.search("I2018", "ID", Pattern.LITERAL | Pattern.CASE_INSENSITIVE);
		PNode n = v.iterator().next().getNode(); 
		quilt.search("", "", Pattern.LITERAL);
//		endTask();
		
		wait(6);
		
		beginTask("select 1");
//		Selection s = selection.select(n);
		endTask();

		wait(1);

		/*beginTask("highlight 1");
		s.highlightPredecessors();
		s.highlightSuccessors();
		s.highlightSelection();
		frame.repaint();
		endTask();*/

		wait(2);

//		beginTask("search 2");
		v = quilt.search("I2961", "ID", Pattern.LITERAL | Pattern.CASE_INSENSITIVE);
		n = v.iterator().next().getNode(); 
		quilt.search("", "", Pattern.LITERAL);
//		endTask();
		
		wait(1);
		
		beginTask("select 2");
//		s = 
		    selection.select(n);
		endTask();

		wait(1);

		/*beginTask("highlight 2");
		s.highlightPredecessors();
		s.highlightSuccessors();
		s.highlightSelection();
		frame.repaint();
		endTask();*/

		wait(2);

		beginTask("paint canvas");
		canvas.paintImmediately(canvas.getBounds());
		endTask();

		wait(1);

		beginTask("paint bev");
		bev.paintImmediately(bev.getBounds());
		endTask();
		
		wait(1);

		beginTask("paint bev");
		bev.paintImmediately(bev.getBounds());
		endTask();
		
		wait(1);
		
		//System.exit(0);
	}

	/////////////////////
	
	private static long now() {
		return System.nanoTime();
	}
	
	/**
	 * Begin a long task
	 * @param taskname_ the task name
	 */
	public static void beginTask(String taskname_) {
		t0 = now();
		taskname = taskname_;
	}
	
	/**
	 * Ends the current long task.
	 */
	public static void endTask() {
		if (t0 == 0 || taskname == null) {
			System.out.println("Error: attempt to end a task that has not started.");
			return;
		}
		int timems = (int)((now() - t0)/1000000);
		System.out.println("Task \"" + taskname + "\" done in " + timems + " ms.");
		t0 = 0;
		taskname = null;
	}
	
	/**
	 * Sleeps for a specified amount os seconds.
	 * @param s the number of seconds
	 */
	public static void wait(int s) {
		try {
			Thread.sleep(1000 * s);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	

}
