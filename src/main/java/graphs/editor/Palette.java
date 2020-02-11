package graphs.editor;

import com.mxgraph.io.mxCodecRegistry;
import com.mxgraph.io.mxObjectCodec;
import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxGeometry;
import com.mxgraph.swing.util.mxGraphTransferable;
import com.mxgraph.swing.util.mxSwingConstants;
import com.mxgraph.util.mxEvent;
import com.mxgraph.util.mxEventObject;
import com.mxgraph.util.mxEventSource;
import com.mxgraph.util.mxEventSource.mxIEventListener;
import com.mxgraph.util.mxRectangle;
import graphs.model.Operation;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class Palette extends JPanel {
    private static final long serialVersionUID = 7771113885935187066L;
    protected JLabel selectedEntry = null;
    protected mxEventSource eventSource = new mxEventSource(this);
    protected Color gradientColor = Color.WHITE;
    @SuppressWarnings("serial")
    public Palette() {
        setBackground(Color.WHITE);
        setLayout(new FlowLayout(FlowLayout.LEADING, 5, 5));
        
        // Clears the current selection when the background is clicked
        addMouseListener(new MouseListener() {
            public void mousePressed(MouseEvent e) {
                clearSelection();
            }
            public void mouseClicked(MouseEvent e) {
            }
            public void mouseEntered(MouseEvent e) {
            }
            public void mouseExited(MouseEvent e) {
            }
            public void mouseReleased(MouseEvent e) {
            }
            
        });
        
        // Shows a nice icon for drag and drop but doesn't import anything
        setTransferHandler(new TransferHandler() {
            public boolean canImport(JComponent comp, DataFlavor[] flavors) {
                return true;
            }
        });
    }
    
    public void paintComponent(Graphics g) {
        if (gradientColor == null) {
            super.paintComponent(g);
        } else {
            Rectangle rect = getVisibleRect();
            
            if (g.getClipBounds() != null) {
                rect = rect.intersection(g.getClipBounds());
            }
            
            Graphics2D g2 = (Graphics2D) g;
    
            g2.setPaint(new GradientPaint(0, 0, getBackground(), getWidth(), 0, gradientColor));
            g2.fill(rect);
        }
    }
    public void clearSelection() {
        setSelectionEntry(null, null);
    }
    public void setSelectionEntry(JLabel entry, mxGraphTransferable t) {
        JLabel previous = selectedEntry;
        selectedEntry = entry;
        
        if (previous != null) {
            previous.setBorder(null);
            previous.setOpaque(false);
        }
        
        if (selectedEntry != null) {
            selectedEntry.setBorder(ShadowBorder.getSharedInstance());
            selectedEntry.setOpaque(true);
        }
        
        eventSource.fireEvent(new mxEventObject(mxEvent.SELECT, "entry",
                selectedEntry, "transferable", t, "previous", previous));
    }
    
    public void setPreferredWidth(int width) {
        int cols = Math.max(1, width / 55);
        setPreferredSize(new Dimension(width,
                (getComponentCount() * 55 / cols) + 30));
        revalidate();
    }
    
    public void addOperation(Operation opObject, ImageIcon icon, String style,
                             int width, int height) {
        mxCodecRegistry.addPackage(opObject.getClass().getPackage().toString());
        try {
            mxCodecRegistry.register(new mxObjectCodec(opObject.getClass().newInstance()));
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
        
        mxCell cell = new mxCell(opObject, new mxGeometry(0, 0, width, height),
                style);
        cell.setVertex(true);
        addTemplate(opObject.getName(), icon, cell);
    }
    
    public void addTemplate(final String name, ImageIcon icon, String style,
                            int width, int height, Object value) {
        mxCell cell = new mxCell(value, new mxGeometry(0, 0, width, height),
                style);
        cell.setVertex(true);
        
        addTemplate(name, icon, cell);
    }
    
    public void addTemplate(final String name, ImageIcon icon, mxCell cell) {
        mxRectangle bounds = (mxGeometry) cell.getGeometry().clone();
        final mxGraphTransferable t = new mxGraphTransferable(new Object[]{cell}, bounds);
        
        // Scales the image if it's too large for the library
        if (icon != null) {
            if (icon.getIconWidth() > 32 || icon.getIconHeight() > 32) {
                icon = new ImageIcon(icon.getImage().getScaledInstance(32, 32, 0));
            }
        }
        
        final JLabel entry = new JLabel(icon);
        entry.setPreferredSize(new Dimension(50, 50));
        entry.setBackground(Palette.this.getBackground().brighter());
        entry.setFont(new Font(entry.getFont().getFamily(), Font.PLAIN, 10));
        
        entry.setVerticalTextPosition(JLabel.BOTTOM);
        entry.setHorizontalTextPosition(JLabel.CENTER);
        entry.setIconTextGap(0);
        
        entry.setToolTipText(name);
        entry.setText(name);
        
        entry.addMouseListener(new MouseListener() {
            public void mousePressed(MouseEvent e) {
                setSelectionEntry(entry, t);
            }
            public void mouseClicked(MouseEvent e) {
            }
            public void mouseEntered(MouseEvent e) {
            }
            public void mouseExited(MouseEvent e) {
            }
            public void mouseReleased(MouseEvent e) {
            }
        });
        
        // Install the handler for dragging nodes into a graph
        DragGestureListener dragGestureListener = e -> e.startDrag(null, mxSwingConstants.EMPTY_IMAGE, new Point(), t, null);
        
        DragSource dragSource = new DragSource();
        dragSource.createDefaultDragGestureRecognizer(entry, DnDConstants.ACTION_COPY, dragGestureListener);
        
        add(entry);
    }
    public void addListener(String eventName, mxIEventListener listener) {
        eventSource.addListener(eventName, listener);
    }
    public boolean isEventsEnabled() {
        return eventSource.isEventsEnabled();
    }
    public void setEventsEnabled(boolean eventsEnabled) {
        eventSource.setEventsEnabled(eventsEnabled);
    }
    public void removeListener(mxIEventListener listener) {
        eventSource.removeListener(listener);
    }
    public void removeListener(mxIEventListener listener, String eventName) {
        eventSource.removeListener(listener, eventName);
    }
    
}
