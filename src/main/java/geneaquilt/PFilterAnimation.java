package geneaquilt;

import edu.umd.cs.piccolo.activities.PActivity;
import edu.umd.cs.piccolo.activities.PInterpolatingActivity;

/**
 * <b>PFilterAnimation</b> manages the animation of a filtered animation
 * 
 * @author Pierre Dragicevic
 */
public class PFilterAnimation extends PInterpolatingActivity {
	
	private float source;
	private float destination;
	private Target target;
	private static PFilterAnimation currentAnimation = null;

	/**
	 * <b>Target</b> Objects that want their color to be set by the color
	 * activity must implement this interface.
	 */
	interface Target {
		void startFiltering(float destFilter);
		void setFilteringParameter(float filter);
		float getFilteringParameter();
		void endFiltering();
	}

	/**
	 * Create a new PDOIAnimationActivity.
	 * <P>
	 * @param duration the length of one loop of the activity
	 * @param stepRate the amount of time between steps of the activity
	 * @param aTarget the object that the activity will be applied to and where
	 * the source state will be taken from.
	 * @param aDestination the destination filtering state. 0 is unfiltered, 1 is filtered.
	 */
	public PFilterAnimation(long duration, long stepRate, Target aTarget, float aDestination) {
		super(duration, stepRate);
		target = aTarget;
		destination = aDestination;
		if (currentAnimation != null && currentAnimation != this && currentAnimation.isStepping())
			currentAnimation.terminate(PActivity.TERMINATE_WITHOUT_FINISHING);
		setSlowInSlowOut(false);
	}	

	protected boolean isAnimation() {
		return true;
	}

	/**
	 * @return the destination filtering
	 */
	public float getDestinationFiltering() {
		return destination;
	}

	/**
	 * Sets the destination filtering
	 * @param newDestination the new value
	 */
	public void setDestinationFiltering(float newDestination) {
		destination = newDestination;
	}

	@Override
	protected void activityStarted() {
		source = target.getFilteringParameter();
		target.startFiltering(destination);
		super.activityStarted();
		currentAnimation = this;
	}

	@Override
	protected void activityFinished() {
		source = target.getFilteringParameter();
		target.endFiltering();
		super.activityFinished();
	}

	/**
	 * {@inheritDoc}
	 */
	public void setRelativeTargetValue(float zeroToOne) {
		super.setRelativeTargetValue(zeroToOne);
		float filteringParameter = source + (destination - source) * zeroToOne;
		target.setFilteringParameter(filteringParameter);
	}
}
