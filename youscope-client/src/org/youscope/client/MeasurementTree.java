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
package org.youscope.client;

import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

import javax.swing.Icon;
import javax.swing.JTree;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import org.youscope.clientinterfaces.YouScopeFrame;
import org.youscope.common.PositionInformation;
import org.youscope.common.image.ImageProducer;
import org.youscope.common.job.CompositeJob;
import org.youscope.common.job.Job;
import org.youscope.common.measurement.Measurement;
import org.youscope.common.Well;
import org.youscope.common.table.TableProducer;
import org.youscope.common.task.Task;
import org.youscope.common.util.TextTools;
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
	private static final long	serialVersionUID	= -2908753388157993875L;
	private final JobTreeModel measurementTreeModel	= new JobTreeModel();
	private final MeasurementControl measurementFrame;
	private final ArrayList<ImageNode> allImagingNodes = new ArrayList<ImageNode>();
	public MeasurementTree(MeasurementControl measurementFrame)
	{
		this.measurementFrame = measurementFrame;
		setModel(measurementTreeModel);
		setRootVisible(false);
		setScrollsOnExpand(true);
		addMouseListener(new MouseAdapter()
		{
			@Override
			public void mousePressed(MouseEvent e)
			{
				if(e.getClickCount() != 2)
					return;

				TreePath selPath = getPathForLocation(e.getX(), e.getY());
				if(selPath == null)
					return;

				JobNode node = (JobNode)selPath.getLastPathComponent();
				if(node instanceof ImageNode)
				{
					MeasurementImageFrame imageViewer;
					try {
						imageViewer = new MeasurementImageFrame(((ImageNode)node).imageProducer, ((ImageNode)node).getPositionInformation());
					} catch (Exception e1) {
						ClientSystem.err.println("Could not create image frame", e1);
						return;
					}
					YouScopeFrame childFrame = imageViewer.toFrame();
					MeasurementTree.this.measurementFrame.addChildFrame(childFrame);
					childFrame.setVisible(true);
				}
				else if(node instanceof TableNode)
				{
					YouScopeFrame newFrame = MeasurementTree.this.measurementFrame.createChildFrame();
					
					TableDataFrame tableDataFrame = new TableDataFrame(((TableNode)node).tableProducer, ((TableNode)node).getPositionInformation(), new YouScopeClientConnectionImpl());
					tableDataFrame.createUI(newFrame);
					newFrame.setVisible(true);
				}
				else if(node instanceof LastImageNode)
				{
					PositionInformation[] positionInformations = new PositionInformation[allImagingNodes.size()];
					ImageProducer[]	imageProducers = new ImageProducer[allImagingNodes.size()];
					for(int i=0; i<allImagingNodes.size(); i++)
					{
						ImageNode imagingNode = allImagingNodes.get(i);
						positionInformations[i] = imagingNode.getPositionInformation();
						imageProducers[i] =imagingNode.imageProducer;
					}
					
					MeasurementImageFrame imageViewer;
					try {
						imageViewer = new MeasurementImageFrame(imageProducers, positionInformations);
					} catch (Exception e1) {
						ClientSystem.err.println("Could not create image frame", e1);
						return;
					}
					YouScopeFrame childFrame = imageViewer.toFrame();
					MeasurementTree.this.measurementFrame.addChildFrame(childFrame);
					childFrame.setVisible(true);
				}
			}
		});
		
		DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer()
		{
			/**
			 * Serial Version UID.
			 */
			private static final long	serialVersionUID	= -4744253202373979909L;
			private final Icon lastImageIcon = ImageLoadingTools.getResourceIcon("icons/camcorder.png", "last image stream");
			private final Icon imageIcon = ImageLoadingTools.getResourceIcon("icons/camera.png", "image stream");
			private final Icon tableIcon = ImageLoadingTools.getResourceIcon("icons/chart.png", "image stream");
			private final Icon wellIcon = ImageLoadingTools.getResourceIcon("icons/map-pin.png", "well");
			private final Icon positionIcon = ImageLoadingTools.getResourceIcon("icons/maps-stack.png", "position");
			
			@Override
			public Component getTreeCellRendererComponent(
                    JTree tree,
                    Object value,
                    boolean sel,
                    boolean expanded,
                    boolean leaf,
                    int row,
                    boolean hasFocus) 
			{
			    super.getTreeCellRendererComponent(
			                    tree, value, sel,
			                    expanded, leaf, row,
			                    hasFocus);
			    if(value instanceof ImageNode)
			    	setIcon(imageIcon);
			    else if(value instanceof LastImageNode)
			    	setIcon(lastImageIcon);
			    else if(value instanceof PositionNode)
			    	setIcon(positionIcon);
			    else if(value instanceof TableNode)
			    	setIcon(tableIcon);
			    else if(value instanceof WellNode)
			    	setIcon(wellIcon);
			    else
			    	setIcon(null);
			    return this;
			}


		};
		
		setCellRenderer(renderer);
		setToolTipText("");
	}
	
	@Override
	public String getToolTipText(MouseEvent evt) 
	{
        if (getRowForLocation(evt.getX(), evt.getY()) == -1)
          return null;
        TreePath curPath = getPathForLocation(evt.getX(), evt.getY());
        JobNode jobNode = ((JobNode) curPath.getLastPathComponent());
        if(jobNode != null)
        	return jobNode.toHTML();
		return null;
    }
	private void computeTreeRepresentationOfTask(RootNode rootNode, Task task)
	{
		Job[] jobs;
		try
		{
			jobs = task.getJobs();
		}
		catch(RemoteException e)
		{
			ClientSystem.err.println("Failure in construction of measurement tree representation. Trying to rescue...", e);
			return;
		}
		for(Job job : jobs)
		{
			if(job instanceof CompositeJob)
			{
				computeTreeRepresentationOfJobContainer(rootNode, (CompositeJob)job);
			}
			if(job instanceof ImageProducer)
			{
				computeTreeRepresentationOfImageProducer(rootNode, job);
			}
			if(job instanceof TableProducer)
			{
				computeTreeRepresentationOfTableDataProducer(rootNode, job);
			}
		}
	}
	private void computeTreeRepresentationOfJobContainer(RootNode rootNode, CompositeJob jobContainer)
	{
		Job[] jobs;
		try
		{
			jobs = jobContainer.getJobs();
		}
		catch(RemoteException e)
		{
			ClientSystem.err.println("Failure in construction of measurement tree representation. Trying to rescue...", e);
			return;
		}
		for(Job job : jobs)
		{
			if(job instanceof CompositeJob)
			{
				computeTreeRepresentationOfJobContainer(rootNode, (CompositeJob)job);
			}
			if(job instanceof ImageProducer)
			{
				computeTreeRepresentationOfImageProducer(rootNode, job);
			}
			if(job instanceof TableProducer)
			{
				computeTreeRepresentationOfTableDataProducer(rootNode, job);
			}
		}
	}

	private JobNode getParentNode(RootNode rootNode, PositionInformation positionInformation)
	{
		JobNode parentNode = rootNode;
		if(positionInformation.getWell() != null)
		{
			// For microplate measurements
			// look if well node already exists, otherwise create it.
			boolean found = false;
			for(JobNode node : parentNode.children)
			{
				if(!(node instanceof WellNode))
					continue;
				if(((WellNode)node).well.compareTo(positionInformation.getWell()) == 0)
				{
					parentNode = node;
					found = true;
					break;
				}
			}
			if(!found)
			{
				WellNode wellNode = new WellNode(positionInformation.getWell());
				parentNode.children.add(wellNode);
				parentNode = wellNode;
			}
		}
		// Now search if position node already exists.
		for(int i=0; i < positionInformation.getNumPositions(); i++)
		{
			PositionNode posNode;
			if(positionInformation.getPositionType(i).equals(PositionInformation.POSITION_TYPE_YTILE)
					&& i+1 < positionInformation.getNumPositions()
					&& positionInformation.getPositionType(i+1).equals(PositionInformation.POSITION_TYPE_XTILE))
			{
				PositionInformation newPosition = new PositionInformation(new PositionInformation(parentNode.getPositionInformation(), positionInformation.getPositionType(i), positionInformation.getPosition(i)), positionInformation.getPositionType(i+1), positionInformation.getPosition(i+1));
				posNode = new PositionNode(newPosition, "tile", positionInformation.getPosition(i), positionInformation.getPosition(i+1));
				i++;
			}
			else
			{
				PositionInformation newPosition = new PositionInformation(parentNode.getPositionInformation(), positionInformation.getPositionType(i), positionInformation.getPosition(i));
				
				posNode = new PositionNode(newPosition, positionInformation.getPositionType(i), positionInformation.getPosition(i));
			}
			boolean found = false;
			for(JobNode node : parentNode.children)
			{
				if(node instanceof PositionNode && posNode.compareTo(node) == 0)
				{
					parentNode = node;
					found = true;
					break;
				}
			}
			if(!found)
			{
				parentNode.children.add(posNode);
				parentNode = posNode;
			}
		}
		
		return parentNode;
	}
	
	private void computeTreeRepresentationOfImageProducer(RootNode rootNode, Job imageProducer)
	{
		try
		{
			if(((ImageProducer)imageProducer).getNumberOfImages() == 0)
				return;
		}
		catch(RemoteException e1)
		{
			ClientSystem.err.println("Failure in construction of measurement tree representation. Trying to rescue...", e1);
		}
		
		PositionInformation positionInformation;
		try
		{
			positionInformation = imageProducer.getPositionInformation();
		}
		catch(RemoteException e)
		{
			ClientSystem.err.println("Failure in construction of measurement tree representation. Trying to rescue...", e);
			positionInformation = new PositionInformation(null);
		}
		
		JobNode parentNode = getParentNode(rootNode, positionInformation);
		
		// Now add our own
		ImageNode node = new ImageNode((ImageProducer)imageProducer, positionInformation);
		parentNode.getChildren().add(node);
		allImagingNodes.add(node);
	}
	
	private void computeTreeRepresentationOfTableDataProducer(RootNode rootNode, Job tableDataProducer)
	{
		PositionInformation positionInformation;
		try
		{
			positionInformation = tableDataProducer.getPositionInformation();
		}
		catch(RemoteException e)
		{
			ClientSystem.err.println("Failure in construction of measurement tree representation. Trying to rescue...", e);
			positionInformation = new PositionInformation(null);
		}
		
		JobNode parentNode = getParentNode(rootNode, positionInformation);
		
		// Now add our own
		TableNode node = new TableNode((TableProducer)tableDataProducer, positionInformation);
		parentNode.getChildren().add(node);
	}
	
	public void setMeasurement(Measurement measurement)
	{
		RootNode rootNode = measurementTreeModel.getRootNode();
		
		rootNode.getChildren().clear();

		Task[] tasks;
		try
		{
			tasks = measurement.getTasks();
		}
		catch(RemoteException e)
		{
			ClientSystem.err.println("Failure in construction of measurement tree representation", e);
			return;
		}
		for(Task task : tasks)
		{
			computeTreeRepresentationOfTask(rootNode, task);
		}
		if(allImagingNodes.size() > 1)
			rootNode.getChildren().add(new LastImageNode());
		
		rootNode.sortChildren();
		
		measurementTreeModel.treeActualized();
	}
	
	protected class JobTreeModel implements TreeModel
	{
		private RootNode							rootNode			= new RootNode();

		private Vector<TreeModelListener>	treeModelListeners	= new Vector<TreeModelListener>();

		RootNode getRootNode()
		{
			return rootNode;
		}
		
		void treeActualized()
		{
			// update UI
			for(TreeModelListener listener : treeModelListeners)
			{
				Object[] path = new Object[1];
				path[0] = rootNode;
				listener.treeStructureChanged(new TreeModelEvent(this, path));
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
			return ((JobNode)parent).children.get(index);
		}

		@Override
		public int getChildCount(Object parent)
		{
			return ((JobNode)parent).children.size();
		}

		@Override
		public int getIndexOfChild(Object parent, Object child)
		{
			for(int i = 0; i < ((JobNode)parent).children.size(); i++)
			{
				if(((JobNode)parent).children.get(i) == child)
					return i;
			}
			return -1;
		}

		@Override
		public Object getRoot()
		{
			return rootNode;
		}

		@Override
		public boolean isLeaf(Object node)
		{
			if(node instanceof ImageNode || node instanceof TableNode || node instanceof LastImageNode)
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
	
	private abstract static class JobNode implements Comparable<JobNode>
	{
		private final List<JobNode>	children	= new ArrayList<JobNode>();
		private final String name;
		private final String description;
		private final PositionInformation positionInformation;
		public JobNode(String name, String description, PositionInformation positionInformation)
		{
			this.name = name;
			this.description = description;
			this.positionInformation = positionInformation;
		}
		public String toHTML()
		{
			return "<html>"+TextTools.toHTML(getName(), getDescription())+"</html>";
		}
		public List<JobNode> getChildren()
		{
			return children;
		}
		
		public PositionInformation getPositionInformation()
		{
			return positionInformation;
		}
		
		@Override
		public String toString()
		{
			return name;
		}
		
		public String getName()
		{
			return name;
		}
		
		public String getDescription()
		{
			return description;
		}
		
		public void sortChildren()
		{
			Collections.sort(children);
			for(JobNode childNode : children)
			{
				childNode.sortChildren();
			}
		}
	}

	private static class RootNode extends JobNode
	{
		public RootNode()
		{
			super("Root", "Root element of measurement tree.", new PositionInformation());
		}
		@Override
		public int compareTo(JobNode otherNode)
		{
			if(otherNode instanceof RootNode)
				return 0;
			return -1;
		}
	}

	private static class WellNode extends JobNode
	{
		final Well	well;
		WellNode(Well well)
		{
			super("Well "+well.getWellName(), "Contains all data producers in well " + well.getWellName(), new PositionInformation(well));
			this.well = well;
		}

		@Override
		public int compareTo(JobNode otherNode)
		{
			if(otherNode instanceof RootNode)
				return 1;
			else if (otherNode instanceof LastImageNode)
				return 1;
			else if(otherNode instanceof ImageNode)
				return -1;
			else if(otherNode instanceof PositionNode)
				return -1;
			else if(otherNode instanceof TableNode)
				return -1;
			else if(otherNode instanceof WellNode)
			{
				return well.compareTo(((WellNode)otherNode).well);
			}
			else
				return 0;
		}
	}

	private static class PositionNode extends JobNode
	{
		PositionNode(PositionInformation positionInformation, String type, int... position)
		{
			super(getPositionString(type, position), "Contains all data producers in " + getPositionString(type, position)+".", positionInformation);
		}
		private static String getPositionString(String type, int... position)
		{
			String name =type;
			for(int i=0; i<position.length; i++)
			{
				if(i>0)
					name += "-" + Integer.toString(position[i]+1);
				else
					name += " " + Integer.toString(position[i]+1);
			}
			return name;
		}

		
		@Override
		public int compareTo(JobNode otherNode)
		{
			if(otherNode instanceof RootNode)
				return 1;
			else if (otherNode instanceof LastImageNode)
				return 1;
			else if(otherNode instanceof ImageNode)
				return -1;
			else if(otherNode instanceof TableNode)
				return -1;
			else if(otherNode instanceof PositionNode)
			{
				PositionNode otherPositionNode = (PositionNode)otherNode;
				return getName().compareTo(otherPositionNode.getName());
			}
			else if(otherNode instanceof WellNode)
			{
				return 1;
			}
			else
				return 0;
		}
	}

	private static class TableNode extends JobNode
	{
		private final TableProducer tableProducer;
		
		TableNode(TableProducer tableProducer, PositionInformation positionInformation)
		{
			super(createName(tableProducer), createDescription(tableProducer), positionInformation);
			this.tableProducer = tableProducer;
		}
		
		private static String createName(TableProducer tableProducer)
		{
			try
			{
				return tableProducer.getProducedTableDefinition().getTableName();
			}
			catch(Exception e)
			{
				ClientSystem.err.println("Could not obtain table name from table producer. Substituting default string.", e);
				return "Unknown Table";
			}
		}
		
		private static String createDescription(TableProducer tableProducer)
		{
			try
			{
				return tableProducer.getProducedTableDefinition().getTableDescription();
			}
			catch(Exception e)
			{
				ClientSystem.err.println("Could not obtain table description from table producer. Substituting default string.", e);
				return "The description of the table could not be obtained due to an error: "+e.getMessage()+".";
			}
		}
		
		@Override
		public int compareTo(JobNode otherNode)
		{
			if(otherNode instanceof RootNode)
				return 1;
			else if(otherNode instanceof ImageNode)
				return -1;
			else if (otherNode instanceof LastImageNode)
				return 1;
			else if(otherNode instanceof TableNode)
				return 0;
			else if(otherNode instanceof PositionNode)
			{
				return 1;
			}
			else if(otherNode instanceof WellNode)
			{
				return 1;
			}
			else
				return 0;
		}
	}
	
	private static class LastImageNode extends JobNode
	{
		public LastImageNode() 
		{
			super("Last Image", "Displays the last image independently of the channel and position where it was taken.", new PositionInformation());
		}
		
		@Override
		public int compareTo(JobNode otherNode)
		{
			if(otherNode instanceof RootNode)
				return 1;
			else if(otherNode instanceof LastImageNode)
				return 0;
			else 
				return -1;
		}
	}
	
	private static class ImageNode extends JobNode
	{
		private final ImageProducer	imageProducer;
		public ImageNode(ImageProducer imageProducer, PositionInformation positionInformation)
		{
			super(createImageName(imageProducer), "Images taken by "+createImageName(imageProducer), positionInformation);
			this.imageProducer = imageProducer;
		}
		
		private static String createImageName(ImageProducer	imageProducer)
		{
			try
			{
				return imageProducer.getImageDescription();
			}
			catch(RemoteException e)
			{
				ClientSystem.err.println("Could not obtain image description from image producer. Substituting default string.", e);
				return "Unknown Image";
			}
		}
		
		@Override
		public int compareTo(JobNode otherNode)
		{
			if(otherNode instanceof RootNode)
				return 1;
			else if (otherNode instanceof LastImageNode)
				return 1;
			else if(otherNode instanceof ImageNode)
				return 0;
			else if(otherNode instanceof TableNode)
				return 1;
			else if(otherNode instanceof PositionNode)
			{
				return 1;
			}
			else if(otherNode instanceof WellNode)
			{
				return 1;
			}
			else
				return 0;
		}
	}
}
