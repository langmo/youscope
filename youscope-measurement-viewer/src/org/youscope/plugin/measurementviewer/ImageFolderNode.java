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

import java.util.Collections;
import java.util.Enumeration;
import java.util.Vector;

import javax.swing.tree.TreeNode;

/**
 * @author Moritz Lang
 *
 */
class ImageFolderNode implements TreeNode, Comparable<ImageFolderNode>
{
	private final String folder;
	private final Vector<ImageFolderNode> children = new Vector<ImageFolderNode>();
	private final ImageFolderNode parent;
	private ImageList imageList = null;
	private final ImageFolderType type;
	private boolean sorted = false;
	public ImageFolderNode(ImageFolderNode parent, String folder, ImageFolderType type)
	{
		this.folder = folder;
		this.parent = parent;
		this.type = type;
	}
	
	public enum ImageFolderType
	{
		ROOT,
		WELL,
		POSITION,
		JOB
	}
	public ImageFolderType getType()
	{
		return type;
	}
	
	private void sortChildren()
	{
		if(sorted)
			return;
		Collections.sort(children);
		sorted = true;
	}
	
	public ImageList getImageList()
	{
		return imageList;
	}
	
	/**
	 * Integrates the imageList element under this folder, by adding sub-folders according to the path argument,
	 * and setting the last element of the path to own the imageList.
	 * @param path The path where the image list should be added. Relative to the path of this element.
	 * @param imageList The image list which should be added under the given path.
	 */
	public void insertChild(String[] path, ImageList imageList)
	{
		// Test if the image list should be added to this folder.
		if(path.length == 0)
		{
			this.imageList = imageList;
			return;
		}
		
		// The well number can be empty, meaning that it was not a well measurement. This means that the first element
		// of the path is empty for the root element.
		boolean removedEmptyWell = false;
		while(path.length > 1 && path[0].length() == 0)
		{
			removedEmptyWell = true;
			String[] newPath = new String[path.length -1];
			System.arraycopy(path, 1, newPath, 0, path.length -1);
			path = newPath;
		}
		
		// Search for the sub-folder with the given next path element.
		ImageFolderNode nextFolder = null;
		for(ImageFolderNode childFolder : children)
		{
			if(childFolder.getFolder().equals(path[0]))
			{
				nextFolder = childFolder;
				break;
			}
		}
		// If we didn't find the respective sub-folder, create it.
		if(nextFolder == null)
		{
			ImageFolderType nextType;
			if(path.length == 1)
				nextType = ImageFolderType.JOB;
			else if(type == ImageFolderType.POSITION || type == ImageFolderType.WELL)
				nextType = ImageFolderType.POSITION;
			else if(removedEmptyWell)
				nextType = ImageFolderType.POSITION;
			else
				nextType = ImageFolderType.WELL;
			nextFolder = new ImageFolderNode(this, path[0], nextType);
			children.addElement(nextFolder);
		}
		
		// Remove first element from path and give it to the sub-folder
		String[] nextPath = new String[path.length -1];
		System.arraycopy(path, 1, nextPath, 0, path.length -1);
		nextFolder.insertChild(nextPath, imageList);
	}
	
	public String getFolder()
	{
		return folder;
	}
	
	@Override
	public String toString()
	{
		if(type == ImageFolderType.WELL)
			return "Well " + folder;
		else if(type == ImageFolderType.POSITION)
			return "Position " + folder;
		else
			return folder;
	}
	
	@Override
	public TreeNode getChildAt(int childIndex)
	{
		sortChildren();
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
		return parent;
	}

	@Override
	public int getIndex(TreeNode node)
	{
		sortChildren();
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
		return children.isEmpty();
	}

	@Override
	public Enumeration<ImageFolderNode> children()
	{
		sortChildren();
		return children.elements();
	}
	@Override
	public int compareTo(ImageFolderNode o)
	{
		if(o == null)
			return -1;
		else if(o.getType() == ImageFolderType.ROOT)
			return 1;
		else if(o.getType() == ImageFolderType.WELL)
		{
			if(type == ImageFolderType.ROOT)
				return -1;
			else if(type == ImageFolderType.WELL)
			{
				// Extract coordinates
				String oFolder = o.getFolder(); 
				int oXPos = 0;
				String oYPos = "";
				for(int i=1;i<oFolder.length(); i++)
				{
					try
					{
						oXPos = Integer.parseInt(oFolder.substring(i));
						oYPos = oFolder.substring(0, i);
						break;
					}
					catch(@SuppressWarnings("unused") NumberFormatException e)
					{
						// Continue iterating, this means there was still a letter in the index, corresponding to a y-coordinate.
					}
				}
				
				int xPos = 0;
				String yPos = "";
				for(int i=1;i<folder.length(); i++)
				{
					try
					{
						xPos = Integer.parseInt(folder.substring(i));
						yPos = folder.substring(0, i);
						break;
					}
					catch(@SuppressWarnings("unused") NumberFormatException e)
					{
						// Continue iterating, this means there was still a letter in the index, corresponding to a y-coordinate.
					}
				}
				
				// Sort first by y-position, then by x
				if(yPos.length() != oYPos.length())
					return yPos.length() - oYPos.length();
				int diffY = yPos.compareTo(oYPos);
				if(diffY != 0)
					return diffY;
				// ypos the same, compare x pos
				return xPos -oXPos;
			}
			else
				return 1;
		}
		else if(o.getType() == ImageFolderType.POSITION)
		{
			if(type == ImageFolderType.JOB)
				return 1;
			else if(type == ImageFolderType.POSITION)
			{
				return folder.compareTo(o.getFolder());
			}
			else 
				return -1;
		}
		else if(o.getType() == ImageFolderType.JOB)
		{
			if(type == ImageFolderType.JOB)
				return getFolder().compareTo(o.getFolder());
			return -1;
		}
		else
		{
			// unknown type, put behind
			return -1;
		}
	}

}
