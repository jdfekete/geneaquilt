package geneaquilt;

import edu.umd.cs.piccolo.PNode;
import geneaquilt.data.Edge;
import geneaquilt.data.Fam;
import geneaquilt.data.Indi;
import geneaquilt.data.Vertex;
import geneaquilt.nodes.PEdge;
import geneaquilt.nodes.PFam;
import geneaquilt.nodes.PIndi;
import geneaquilt.selection.Selection;
import geneaquilt.selection.SelectionManager;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.util.ArrayList;
import java.util.Vector;
import java.util.Map.Entry;

import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;

/**
 * <b>DetailsTable</b> Visualize the details of selected items.
 * 
 * @author Jean-Daniel Fekete
 */
public class DetailsTable extends JTable implements ChangeListener {
    SelectionManager  selManager;
    DefaultTableModel model;
    JScrollPane       scroll;
    ArrayList<Color>  cellColor = new ArrayList<Color>();
    
    static Vector emptyLine = new Vector();
    static {
    	emptyLine.add("");
    	emptyLine.add("");
    }

    /**
     * Creates a details table looking at the selection.
     * 
     * @param selManager
     *            the selection manager
     */
    public DetailsTable(SelectionManager selManager) {
        this.selManager = selManager;
        setFont(new Font("Helvetica", 0, 11));
        setRowHeight(13);
        selManager.addChangeListener(this);
        model = (DefaultTableModel) getModel();
        model.addColumn("Attribute");
        model.addColumn("Value");
        setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        TableColumnModel tcm = getColumnModel();
        tcm.getColumn(0).setPreferredWidth(75);
        tcm.getColumn(0).setMinWidth(75);
        tcm.getColumn(0).setCellRenderer(new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(
                    JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(
                        table,
                        value,
                        isSelected,
                        hasFocus,
                        row,
                        column);
                setBackground(cellColor.get(row));
                return this;
            }
        });
        tcm.getColumn(1).setPreferredWidth(150);
        tcm.getColumn(1).setMinWidth(150);
        tcm.getColumn(1).setCellRenderer(new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(
                    JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(
                        table, value, isSelected,
                        hasFocus, row, column);
                String v = value == null ? "" : value.toString();
                if (v.indexOf('\n') != -1) {
                    v = "<html>"+v.replace("\n", "<br>") + "</html>";
                }
                setToolTipText(v);
                return this;
            }            
        });

        setPreferredScrollableViewportSize(getPreferredSize());
    }

    /**
     * {@inheritDoc}
     */
    public boolean isCellEditable(int row, int column) {
        return false;
    }

    /**
     * @return a scroll pane on that table
     */
    public JScrollPane getScrollPane() {
        if (scroll == null) {
            scroll = new JScrollPane(this);
            JTableHeader h = getTableHeader();
            DefaultTableCellRenderer dcr = (DefaultTableCellRenderer) h
                    .getDefaultRenderer();
            dcr.setHorizontalAlignment(SwingConstants.LEFT);
            h.setReorderingAllowed(false);

            scroll.setColumnHeaderView(h);
            scroll.setPreferredSize(new Dimension(200, 300));
        }
        return scroll;
    }

    /**
     * {@inheritDoc}
     */
    public void stateChanged(ChangeEvent e) {
        updateSelection();
    }

    protected void updateSelection() {
        model.setNumRows(0);
        cellColor.clear();
        for (Selection s : selManager.getSelections()) {
            PNode n = s.getSelectedObject();
            Color c = s.getStrongColor();
            if (n instanceof PIndi) {
                PIndi pindi = (PIndi) n;
                Indi indi = pindi.getIndi();
                update(indi, c);
            }
            else if (n instanceof PFam) {
                PFam pfam = (PFam) n;
                Fam fam = pfam.getFam();
                update(fam, c);
            }
            if (n instanceof PEdge) {
                PEdge pedge = (PEdge) n;
                Edge edge = pedge.getEdge();
                updateEdge(edge, c);
            }
        }
    }

    protected void update(Vertex v, Color c) {
    	
    	if (model.getRowCount() > 0) {
    		model.addRow(emptyLine);
    		cellColor.add(Color.white);
    		setRowHeight(model.getRowCount() - 1, 6);
    	}
        
        for (Entry<String, Object> entry : v.getProps().entrySet()) {
            Vector row = new Vector();
            row.add(entry.getKey());
            row.add(entry.getValue());
            model.addRow(row);
            setRowHeight(model.getRowCount() - 1, getRowHeight());
            cellColor.add(c);
        }

    }

    protected void updateEdge(Edge edge, Color c) {
    }

    static class TextAreaRenderer extends DefaultTableCellRenderer
        implements TableCellRenderer {
        public Component getTableCellRendererComponent(
                JTable jTable,
                Object obj,
                boolean isSelected,
                boolean hasFocus,
                int row,
                int column) {
            String v = obj.toString();
            super.getTableCellRendererComponent(
                    jTable, 
                    v, 
                    isSelected, 
                    hasFocus,
                    row, 
                    column);
            if (v.indexOf('\n') != -1) {
                v = "<html>"+v.replace("\n", "<br>") + "</html>";
            }
            setToolTipText(v);
            return this;
        }
    }

}
