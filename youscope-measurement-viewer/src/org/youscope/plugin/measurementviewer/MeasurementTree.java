/**
 * 
 */
package org.youscope.plugin.measurementviewer;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Vector;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeCellRenderer;
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
	private final Vector<ImageFolderListener> imageFolderListeners = new Vector<ImageFolderListener>();
	private final MyTreeModel treeModel = new MyTreeModel();
	MeasurementTree()
	{	
		this.setModel(treeModel);
		this.setCellRenderer(new MyTreeRenderer());
		setRootVisible(false);
		//setShowsRootHandles(true);
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
		setOpaque(false);
		
	}
	
	public synchronized void setRootNode(ImageFolderNode rootNode)
	{
		this.treeModel.setRootNode(rootNode);
		ImageFolderNode currentNode = rootNode;
		TreePath path = new TreePath(rootNode);
		while(true)
		{
			if(currentNode.getChildCount() <= 0)
				break;
			TreeNode node = currentNode.getChildAt(0);
			if(!(node instanceof ImageFolderNode))
				break;
			currentNode = (ImageFolderNode)node;
			path = path.pathByAddingChild(currentNode);
			if(currentNode.getImageList() != null)
			{
				synchronized(imageFolderListeners)
        		{
            		for(ImageFolderListener listener : imageFolderListeners)
            		{
            			listener.showFolder(currentNode);
            		}
        		}
				setSelectionPath(path);
				break;
			}
		}
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
	
	private static class MyTreeRenderer implements TreeCellRenderer
	{
		private final JLabel cellLabel = new JLabel();
		private final Icon leafIcon;
		private final Icon positionIcon;
		MyTreeRenderer()
		{
			cellLabel.setOpaque(false);
			cellLabel.setForeground(Color.WHITE);
			leafIcon = ImageLoadingTools.getResourceIcon("icons/camera.png", "image stream");
			positionIcon = ImageLoadingTools.getResourceIcon("icons/map-pin.png", "well or position");
		}
		@Override
		public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf,
				int row, boolean hasFocus) 
		{
			if(leaf)
				cellLabel.setIcon(leafIcon);
			else
				cellLabel.setIcon(positionIcon);
			if(selected)
			{
				cellLabel.setBackground(Color.WHITE);
				cellLabel.setForeground(Color.BLACK);
				cellLabel.setOpaque(true);
			}
			else
			{
				cellLabel.setForeground(Color.WHITE);
				cellLabel.setOpaque(false);
			}
			cellLabel.setText(value.toString());
			return cellLabel;
		}
		
	}
	
	private static class MyTreeModel implements TreeModel
    {
		private ImageFolderNode rootNode = new ImageFolderNode(null, "", ImageFolderNode.ImageFolderType.ROOT);
		private final ArrayList<TreeModelListener> treeListeners = new ArrayList<TreeModelListener>();
		 
		@Override
        public void addTreeModelListener(TreeModelListener listener)
        {
			synchronized(treeListeners)
			{
				treeListeners.add(listener);
			}
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
		public void removeTreeModelListener(TreeModelListener listener)
		{
			synchronized(treeListeners)
			{
				treeListeners.remove(listener);
			}
		}
		
		/**
		 * Sets the current root node of the measurement tree.
		 * @param rootNode new root node.
		 */
		public void setRootNode(final ImageFolderNode rootNode)
		{
			Runnable runner = new Runnable()
			{

				@Override
				public void run() 
				{
					MyTreeModel.this.rootNode = rootNode;
					synchronized(treeListeners)
					{
						for(TreeModelListener listener : treeListeners)
						{
							listener.treeStructureChanged(new TreeModelEvent(this, new Object[]{rootNode}));
						}
					}
				}
		
			};
			if(SwingUtilities.isEventDispatchThread())
				runner.run();
			else
				SwingUtilities.invokeLater(runner);
		}
    }
}
