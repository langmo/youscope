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
package org.youscope.plugin.scripting;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Enumeration;
import java.util.Set;
import java.util.Vector;
import java.util.Map.Entry;

import javax.swing.Icon;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.border.TitledBorder;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.youscope.uielements.ImageLoadingTools;

/**
 * @author Moritz Lang
 *
 */
class Workspace extends JPanel implements ScriptVariablesListener
{
	/**
	 * Serial Version UID.
	 */
	private static final long	serialVersionUID	= 6479090656846227942L;
	
	// UI elements
	private VariablesTreeModel variablesTreeModel = new VariablesTreeModel();
	private JTree variablesTree = new JTree(variablesTreeModel);
	
	/**
	 * The root node of the tree showing the defined variables.
	 */
	private RootNode rootNode = new RootNode();
	
	/**
	 * List of all currently defined variables.
	 */
	private Set<Entry<String, Object>> definedVariables = null;
	
	/**
	 * Constructor
	 */
	Workspace()
	{
		super(new BorderLayout());
		add(new JScrollPane(variablesTree), BorderLayout.CENTER);
		setPreferredSize(new Dimension(200, 300));
		variablesTree.setRootVisible(false);
		variablesTree.setShowsRootHandles(true);
		
		// Customize icons
		DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();
		Icon leafIcon = ImageLoadingTools.getResourceIcon("icons/sticky-note-pin.png", "document");
		if(leafIcon != null)
			renderer.setLeafIcon(leafIcon);
		Icon openIcon = ImageLoadingTools.getResourceIcon("icons/wooden-box.png", "opened folder");
		if(openIcon != null)
			renderer.setOpenIcon(openIcon);
		Icon closedIcon = ImageLoadingTools.getResourceIcon("icons/wooden-box.png", "closed folder");
		if(closedIcon != null)
			renderer.setClosedIcon(closedIcon);
		variablesTree.setCellRenderer(renderer);
		
		setBorder(new TitledBorder("Workspace"));
	}
	
	private class RootNode implements TreeNode
	{
		private Vector<TreeNode> children = new Vector<TreeNode>();
		void actualize()
		{
			children.clear();
			if(definedVariables == null)
			{
				return;
			}
			
			for(Entry<String, Object> variable : definedVariables)
			{
				children.addElement(new VariableNode(variable, this));
			}
		}
		
		@Override
		public TreeNode getChildAt(int childIndex)
		{
			return children.elementAt(childIndex);
		}

		@Override
		public int getChildCount()
		{
			return children.size();
		}

		@Override
		public TreeNode getParent()
		{
			return null;
		}

		@Override
		public int getIndex(TreeNode node)
		{
			return children.indexOf(node);
		}

		@Override
		public boolean getAllowsChildren()
		{
			return true;
		}

		@Override
		public boolean isLeaf()
		{
			return false;
		}

		@Override
		public Enumeration<TreeNode> children()
		{
			return children.elements();
		}
		
	}
	
	private class MethodNode implements TreeNode
	{
		private Method method;
		private TreeNode parent;
		MethodNode(Method method, TreeNode parent)
		{
			this.method = method;
			this.parent = parent;
		}
		
		@Override
		public String toString()
		{
			String returnVal = method.getReturnType().getSimpleName() + " " + method.getName() + "(";
			boolean first = true;
			for(Class<?> parameterClass: method.getParameterTypes())
			{
				if(first)
					first = false;
				else
					returnVal += ", ";
				returnVal += parameterClass.getSimpleName();
			}
			returnVal += ")";
			return returnVal;
		}
		
		@Override
		public TreeNode getChildAt(int childIndex)
		{
			return null;
		}

		@Override
		public int getChildCount()
		{
			return 0;
		}

		@Override
		public TreeNode getParent()
		{
			return parent;
		}

		@Override
		public int getIndex(TreeNode node)
		{
			return 0;
		}

		@Override
		public boolean getAllowsChildren()
		{
			return false;
		}

		@Override
		public boolean isLeaf()
		{
			return true;
		}

		@Override
		public Enumeration<TreeNode> children()
		{
			return new Vector<TreeNode>().elements();
		}
		
	}
	
	private class FieldNode implements TreeNode
	{
		private Field field;
		private TreeNode parent;
		FieldNode(Field field, TreeNode parent)
		{
			this.field = field;
			this.parent = parent;
		}
		
		@Override
		public String toString()
		{
			return field.getType().getSimpleName() + " " + field.getName();
		}
		
		@Override
		public TreeNode getChildAt(int childIndex)
		{
			return null;
		}

		@Override
		public int getChildCount()
		{
			return 0;
		}

		@Override
		public TreeNode getParent()
		{
			return parent;
		}

		@Override
		public int getIndex(TreeNode node)
		{
			return 0;
		}

		@Override
		public boolean getAllowsChildren()
		{
			return false;
		}

		@Override
		public boolean isLeaf()
		{
			return true;
		}

		@Override
		public Enumeration<TreeNode> children()
		{
			return new Vector<TreeNode>().elements();
		}
		
	}
	
	private class VariableNode implements TreeNode
	{
		private String name;
		private Object value;
		
		private Vector<TreeNode> children = null;
		private TreeNode parent;
		
		VariableNode(Entry<String, Object> variable, TreeNode parent)
		{
			this.name = variable.getKey();
			this.value = variable.getValue();
			this.parent = parent;
		}
		
		private void initializeChildren()
		{
			if(children != null)
				return;
			
			children = new Vector<TreeNode>();			
			for(Field field : value.getClass().getFields())
			{
				if(!Modifier.isPublic(field.getModifiers()))
						continue;
				
				children.addElement(new FieldNode(field, this));
			}
			
			for(Method method : value.getClass().getMethods())
			{
				if(!Modifier.isPublic(method.getModifiers()))
						continue;
				
				children.addElement(new MethodNode(method, this));
			}
		}
		
		private boolean isElemental()
		{
			if(value instanceof String || value instanceof Number || value instanceof Character)
				return true;
			return false;
		}
		
		@Override
		public String toString()
		{
			if(value == null)
				return name;
			else if(value instanceof String)
				return name + " (\"" + value.toString() +"\")";
			else if(value instanceof Number)
				return name + " (" + value.toString() +")";
			else if(value instanceof Character)
				return name + " ('" + value.toString() +"')";
			else if(value.getClass() != null)
				return name + " (" + value.getClass().getSimpleName() + ")";
			else
				return name;
		}
		@Override
		public TreeNode getChildAt(int childIndex)
		{
			if(isElemental())
				return null;
			initializeChildren();
			return children.elementAt(childIndex);
		}
		@Override
		public int getChildCount()
		{
			if(isElemental())
				return 0;
			initializeChildren();
			return children.size();
		}
		@Override
		public TreeNode getParent()
		{
			return parent;
		}
		@Override
		public int getIndex(TreeNode node)
		{
			if(isElemental())
				return -1;
			initializeChildren();
			return children.indexOf(node);
		}
		@Override
		public boolean getAllowsChildren()
		{
			if(isElemental())
				return false;
			return true;
		}
		
		@Override
		public boolean isLeaf()
		{
			if(isElemental())
				return true;
			return false;
		}
		
		@Override
		public Enumeration<TreeNode> children()
		{
			if(isElemental())
				return new Vector<TreeNode>().elements();
			return children.elements();
		}
	}
	
	@Override
	public Dimension getPreferredSize()
	{
		return new Dimension(200, 300);
	}
	
	private class VariablesTreeModel implements TreeModel
    {
		private Vector<TreeModelListener> treeModelListeners = new Vector<TreeModelListener>();

        void actualizeTree()
        {
        	// Get currently expanded elements
        	Enumeration<TreePath> expandedElementsEnum = variablesTree.getExpandedDescendants(new TreePath(rootNode));
        	Vector<TreePath> expandedElements = new Vector<TreePath>();
        	if(expandedElementsEnum != null)
        	{
	        	while(expandedElementsEnum.hasMoreElements())
	        	{
	        		expandedElements.addElement(expandedElementsEnum.nextElement());
	        	}
        	}
        	
        	// Actualize root node (and therewith all other nodes)
        	rootNode.actualize();
        	
        	// Notify listeners that tree might have changed.
        	for (TreeModelListener listener : treeModelListeners)
            {
                Object[] path = new Object[1];
                path[0] = rootNode;
                listener.treeStructureChanged(new TreeModelEvent(this, path));
            }
        	
        	// Expand all previously expanded elements.
        	for(TreePath path : expandedElements)
        	{
        		for(TreeNode element : rootNode.children)
        		{
        			if(element.toString().compareTo(path.getLastPathComponent().toString()) == 0)
        			{
        				variablesTree.expandPath(new TreePath(new Object[]{rootNode, element}));
        				break;
        			}
        		}
        	}
        }
        
        @Override
        public void addTreeModelListener(TreeModelListener l)
        {
            treeModelListeners.addElement(l);
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
        public void removeTreeModelListener(TreeModelListener l)
        {
            treeModelListeners.removeElement(l);
        }

        @Override
        public void valueForPathChanged(TreePath path, Object newValue)
        {
            // Not used by this model.
        }
    }

	@Override
	public void variablesChanged(Set<Entry<String, Object>> variables)
	{
		definedVariables = variables;
		variablesTreeModel.actualizeTree();
	}
}
