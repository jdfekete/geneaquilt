package geneaquilt.selection.highlight;

import geneaquilt.nodes.GraphicsConstants;
import geneaquilt.selection.Selection;
import geneaquilt.utils.GUIUtils;
import geneaquilt.utils.MulticolorStroke;

import java.awt.Color;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

/**
 * This class computes and stores graphic attributes associated to a specific set of selections, such as
 * their average color. It ensures that every combination of selections is represented by a unique object.
 * 
 * @author dragice
 *
 */
public class SelectionCombination {
	
	private static final Set<SelectionCombination> allCombinations = new HashSet<SelectionCombination>();
	private static final SelectionCombination EMPTY_SELECTION = new SelectionCombination(new HashSet<Selection>());
	private final Set<Selection> selections;
	
	private final Color combinedColor;
	private final Color combinedColor_translucent;
	private final Color combinedColor_light;
	private final Hashtable<Float, MulticolorStroke> multicolorStrokes = new Hashtable<Float, MulticolorStroke>();
	
	protected SelectionCombination(Set<Selection> selections) {
		this.selections = selections;
		this.combinedColor = getAverageColor(selections);
		this.combinedColor_translucent = GUIUtils.multiplyAlpha(combinedColor, selections.size() > 1 ? 0.5f : 0.3f);
		this.combinedColor_light = GUIUtils.mix(combinedColor, Color.white, selections.size() > 1 ? 0f : 0.5f);
	}

	public boolean equals(Object o) {
		if (!(o instanceof SelectionCombination))
			return false;
		return selections.equals(((SelectionCombination)o).selections);
	}
	
	public boolean contains(Selection selection) {
		return selections.contains(selection);
	}
	
	public static SelectionCombination getInstance(Set<Selection> selections) {
		for (SelectionCombination sc : allCombinations) {
			if (sc.selections.equals(selections))
				return sc;
		}
		SelectionCombination sc = new SelectionCombination(selections);
		allCombinations.add(sc);
		return sc;
	}

	public static SelectionCombination getInstance(Selection selection) {
		Set<Selection> sels = new HashSet<Selection>();
		sels.add(selection);
		return getInstance(sels);
	}
	
	public static SelectionCombination getEmptyInstance() {
		if (!allCombinations.contains(EMPTY_SELECTION))
			allCombinations.add(EMPTY_SELECTION);
		return EMPTY_SELECTION;
	}

	public SelectionCombination getInstanceWithSelection(Selection newSelection) {
		Set<Selection> sels = new HashSet<Selection>(selections);
		sels.add(newSelection);
		return getInstance(sels);
	}
	
	public SelectionCombination getInstanceWithoutSelection(Selection selection) {
		Set<Selection> sels = new HashSet<Selection>(selections);
		sels.remove(selection);
		return getInstance(sels);
	}

	static void cleanupDeletedSelection(Selection selection) {
		Set<SelectionCombination> combinations_tmp = new HashSet<SelectionCombination>(allCombinations);
		for (SelectionCombination sc : combinations_tmp) {
			if (sc.contains(selection)) {
				allCombinations.remove(sc);
			}
		}
	}
	
	public Set<Selection> getSelections() {
		return selections;
	}
	
	public static void clear() {
		allCombinations.clear();
	}
	
	protected static Color getAverageColor(Set<Selection> selections) {
		
		if (selections.size() == 0)
			return Color.white;
		
		int r = 0, g = 0, b = 0, a = 0;
		int count = 0;
		for (Selection s : selections) {
			Color c = s.getOpaqueColor();
			r += c.getRed();
			g += c.getGreen();
			b += c.getBlue();
			a += c.getAlpha();
			count++;
		}
		return new Color(r/count, g/count, b/count, a/count);
	}
	
	
	protected static MulticolorStroke createMulticolorStroke(Set<Selection> selections, float width, boolean light) {	
		Color[] colors = new Color[selections.size()];
		int i = 0;
		for (Selection s: selections) {
			colors[i] = light ? s.getLightColor() : s.getStrongColor();
			i++;
		}
		return new MulticolorStroke(width, colors, GraphicsConstants.PATH_HIGHLIGHT_MULTICOLOR_SPACING);
	}
	
	/**
	 * @return Returns the color obtained by blending all selection colors.
	 * 
	 */
	public Color getOpaqueCombinedColor() {
		return combinedColor;
	}
	
	public Color getTranslucentCombinedColor() {
		return combinedColor_translucent;
	}
	
	public Color getLightCombinedColor() {
		return combinedColor_light;
	}
	
	public MulticolorStroke getMulticolorStroke(float width, boolean light) {
		MulticolorStroke s = multicolorStrokes.get(width);
		if (s == null) {
			s = createMulticolorStroke(selections, width, light);
			multicolorStrokes.put(width, s);
		}
		return s;
	}
	
	public boolean isEmpty() {
		return selections.size() == 0;
	}
	
	public int getSelectionCount() {
		return selections.size();
	}
}
