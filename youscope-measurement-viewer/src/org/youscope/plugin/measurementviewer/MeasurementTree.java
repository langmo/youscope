/**
 * 
 */
package org.youscope.plugin.measurementviewer;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.youscope.uielements.ImageLoadingTools;

/**
 * @author Moritz Lang
 *
 */
class MeasurementTree extends JTree
{
	/**
	 * Serial Version UID.
	 */
	private static final long	serialVersionUID	= -2175021952583925133L;
	private final ImageFolderNode rootNode;
	private final Vector<ImageFolderListener> imageFolderListeners = new Vector<ImageFolderListener>();
	MeasurementTree(ImageFolderNode rootNode)
	{
		this.rootNode = rootNode;
		
		this.setModel(new MeasurementTreeModel());
		//setPreferredSize(new Dimension(200, 300));
		setRootVisible(false);
		setShowsRootHandles(true);
		getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		
		addMouseListener (new MouseAdapter() 
		{
			@Override
			public void mousePressed( MouseEvent e ) 
			{
				TreePath path = getPathForLocation(e.getX(), e.getY());
	            if(path == null)
	              return;
	            Object selectedObject = path.getLastPathComponent();
	            setSelectionPath(path);
	            if(selectedObject instanceof ImageFolderNode)
		        {
	            	ImageFolderNode selectedNode = (ImageFolderNode)selectedObject;
	            	if(selectedNode.getImageList() == null)
	            		return;
	            	
	            	synchronized(imageFolderListeners)
	        		{
	            		for(ImageFolderListener listener : imageFolderListeners)
	            		{
	            			listener.showFolder(selectedNode);
	            		}
	        		}
		        }
			} 
		});
		// Customize icons
		DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();
		ImageIcon leafIcon = ImageLoadingTools.getResourceIcon("icons/camera.png", "image stream");
		if(leafIcon != null)
			renderer.setLeafIcon(leafIcon);
		ImageIcon openIcon = ImageLoadingTools.getResourceIcon("icons/map-pin.png", "well or position");
		if(openIcon != null)
			renderer.setOpenIcon(openIcon);
		ImageIcon closedIcon = ImageLoadingTools.getResourceIcon("icons/map-pin.png", "well or position");
		if(closedIcon != null)
			renderer.setClosedIcon(closedIcon);
		setCellRenderer(renderer);
	}
	
	public void addImageFolderListener(ImageFolderListener listener)
	{
		synchronized(imageFolderListeners)
		{
			imageFolderListeners.addElement(listener);
		}
	}
	
	public void removeImageFolderListener(ImageFolderListener listener)
	{
		synchronized(imageFolderListeners)
		{
			imageFolderListeners.removeElement(listener);
		}
	}
	
	private class MeasurementTreeModel implements TreeModel
    {
		@Override
        public void addTreeModelListener(TreeModelListener l)
        {
			// Tree does never change.
        }

        @Override
        public Object getChild(Object parent, int index)
        {
            return ((TreeNode) parent).getChildAt(index);
        }
        @Override
        public int getChildCount(Object parent)
        {
            return ((TreeNode) parent).getChildCount();
        }
        @Override
        public int getIndexOfChild(Object parent, Object child)
        {
        	return ((TreeNode) parent).getIndex((TreeNode)child);
        }

        @Override
        public Object getRoot()
        {
            return rootNode;
        }

        @Override
        public boolean isLeaf(Object node)
        {
            if (((TreeNode)node).isLeaf())
                return true;
			return false;
        }

        @Override
        public void valueForPathChanged(TreePath path, Object newValue)
        {
            // Not used by this model.
        }

		@Override
		public void removeTreeModelListener(TreeModelListener l)
		{
			// Tree does never change.
		}
    }
}
