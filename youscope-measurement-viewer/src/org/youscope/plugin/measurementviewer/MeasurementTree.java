/*******************************************************************************
 * Copyright (c) 2017 Moritz Lang.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Moritz Lang - initial API and implementation
 ******************************************************************************/
/**
 * 
 */
package org.youscope.plugin.measurementviewer;

import java.awt.Color;
import java.awt.Component;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Vector;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.RowMapper;
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
		setExpandsSelectedPaths(true);
		setSelectionModel(new MyTreeSelectionModel());
		getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION );
		setOpaque(false);
		
	}
	
	public synchronized void setRootNode(ImageFolderNode rootNode, ImageFolderNode selectedNode)
	{
		this.treeModel.setRootNode(rootNode);
		TreePath path = findNode(rootNode, selectedNode);
		if(path != null)
			setSelectionPath(path);
		
	}
	private TreePath findNode(ImageFolderNode rootNode, ImageFolderNode node)
	{
		TreePath path = new TreePath(rootNode);
		if(node == null)
		{
			ImageFolderNode currentNode = rootNode;
			while(currentNode.getChildCount() > 0)
			{
				currentNode = currentNode.children().nextElement();
				path = path.pathByAddingChild(currentNode);
				if(currentNode.getImageList() != null)
				{
					return path;
				}
			}
			return null;
		}
		ArrayList<ImageFolderNode> pathElements = new ArrayList<>();
		while(node != null)
		{
			pathElements.add(node);
			node = (ImageFolderNode) node.getParent();
		}
		for(int i=pathElements.size()-2; i>=0; i--)
		{
			path = path.pathByAddingChild(pathElements.get(i));
		}
		return path;
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
	private class MyTreeSelectionModel implements TreeSelectionModel
	{
		private final ArrayList<TreeSelectionListener> listeners = new ArrayList<>(1);
		private RowMapper rowMapper = null;
		private TreePath lastSelected = null;
		@Override
		public void addPropertyChangeListener(PropertyChangeListener listener) {
			// do nothing.
		}

		@Override
		public void addSelectionPath(TreePath path) 
		{
			if(lastSelected == null)
				setSelectionPaths(new TreePath[]{path});
			else
				setSelectionPaths(new TreePath[]{path, lastSelected});
			
		}

		@Override
		public void addSelectionPaths(TreePath[] paths) {
			if(lastSelected == null)
				setSelectionPaths(paths);
			else
			{
				TreePath[] newPaths = new TreePath[paths.length+1];
				newPaths[0] = lastSelected;
				System.arraycopy(paths, 0, newPaths, 1, paths.length);
				setSelectionPaths(newPaths);
			}
		}

		@Override
		public void addTreeSelectionListener(TreeSelectionListener x) {
			listeners.add(x);
			
		}

		@Override
		public void clearSelection() {
			// do nothing.
		}

		@Override
		public TreePath getLeadSelectionPath() {
			if(lastSelected == null)
				return null;
			return lastSelected;
		}

		@Override
		public int getLeadSelectionRow() {
			if(rowMapper == null || lastSelected == null)
				return -1;
			return rowMapper.getRowsForPaths(new TreePath[]{lastSelected})[0];
		}

		@Override
		public int getMaxSelectionRow() 
		{
			int[] rows = getSelectionRows();
			if(rows == null || rows.length == 0)
				return -1;
			int maxVal = Integer.MIN_VALUE;
			for(int row : rows)
			{
				if(row > maxVal)
					maxVal = row;
			}
			return maxVal;
		}

		@Override
		public int getMinSelectionRow() {
			int[] rows = getSelectionRows();
			if(rows == null || rows.length == 0)
				return -1;
			int minVal = Integer.MAX_VALUE;
			for(int row : rows)
			{
				if(row < minVal)
					minVal = row;
			}
			return minVal;
		}

		@Override
		public RowMapper getRowMapper() {
			return rowMapper;
		}

		@Override
		public int getSelectionCount() {
			return lastSelected == null ? 0 : 1;
		}

		@Override
		public int getSelectionMode() {
			return DISCONTIGUOUS_TREE_SELECTION;
		}

		@Override
		public TreePath getSelectionPath() {
			return lastSelected;
		}

		@Override
		public TreePath[] getSelectionPaths() {
			return lastSelected == null ? new TreePath[0] : new TreePath[]{lastSelected};
		}

		@Override
		public int[] getSelectionRows() {
			if(rowMapper == null)
				return null;
			return rowMapper.getRowsForPaths(getSelectionPaths());
		}

		@Override
		public boolean isPathSelected(TreePath path) {
			if(lastSelected == null)
				return false;
			return lastSelected.equals(path);
		}

		@Override
		public boolean isRowSelected(int aRow) 
		{
			int[] rows = getSelectionRows();
			if(rows == null)
				return false;
			for(int row : rows)
			{
				if(aRow == row)
					return true;
			}
			return false;
		}

		@Override
		public boolean isSelectionEmpty() {
			return lastSelected == null;
		}

		@Override
		public void removePropertyChangeListener(PropertyChangeListener listener) {
			// do nothing
		}

		@Override
		public void removeSelectionPath(TreePath path) {
			// do nothing.
		}

		@Override
		public void removeSelectionPaths(TreePath[] paths) {
			// do nothing.
		}

		@Override
		public void removeTreeSelectionListener(TreeSelectionListener x) {
			listeners.remove(x);
		}

		@Override
		public void resetRowSelection() {
			// do nothing.
		}

		@Override
		public void setRowMapper(RowMapper newMapper) {
			rowMapper = newMapper;
		}

		@Override
		public void setSelectionMode(int mode) {
			// do nothing.
		}

		@Override
		public void setSelectionPath(TreePath path) {
			setSelectionPaths(new TreePath[]{path});
		}

		@Override
		public void setSelectionPaths(TreePath[] paths) 
		{
			ArrayList<TreePath> validPaths = new ArrayList<>(1);
			for(TreePath path : paths)
			{
				Object selectedObject = path.getLastPathComponent();
				if(!(selectedObject instanceof ImageFolderNode))
						continue;
				if(((ImageFolderNode)selectedObject).getImageList() == null)
            		return;
				validPaths.add(path);
			}
			if(validPaths.size() == 0)
				return;
			TreePath oldSelected = lastSelected;
			lastSelected = validPaths.remove(0);
			if(!lastSelected.equals(oldSelected))
			{
				TreePath[] isPaths;
				boolean[] isNew;
				if(oldSelected == null)
				{
					isPaths = new TreePath[]{oldSelected, lastSelected};
					isNew = new boolean[]{false, true};
				}
				else
				{
					isPaths = new TreePath[]{lastSelected};
					isNew = new boolean[]{true};
				}
				for(TreeSelectionListener listener:listeners)
				{
					listener.valueChanged(new TreeSelectionEvent(MeasurementTree.this, isPaths, isNew, oldSelected, lastSelected));
				}
				
				Object selectedObject = lastSelected.getLastPathComponent();
				if(selectedObject instanceof ImageFolderNode)
				{
					for(ImageFolderListener listener : imageFolderListeners)
            		{
            			listener.showFolder((ImageFolderNode) selectedObject);
            		}
				}
			}
			if(!validPaths.isEmpty())
			{
				ImageFolderNode[] addNodes = new ImageFolderNode[validPaths.size()];
				for(int i=0; i<validPaths.size(); i++)
				{
					addNodes[i] = (ImageFolderNode) validPaths.get(i).getLastPathComponent();
				}
				
				for(ImageFolderListener listener : imageFolderListeners)
        		{
        			listener.addFolders(addNodes);
        		}
			}
			
		}
		
	}
	private class MyTreeModel implements TreeModel
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
							listener.treeStructureChanged(new TreeModelEvent(MeasurementTree.this, new Object[]{rootNode}));
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
