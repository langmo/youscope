/**
 * 
 */
package org.youscope.plugin.fullmicroplatepath;

import java.util.Vector;

import org.youscope.addon.pathoptimizer.PathOptimizer;
import org.youscope.addon.pathoptimizer.PathOptimizerPosition;
import org.youscope.common.measurement.microplate.Well;
import org.youscope.plugin.microplatemeasurement.MicroplatePositionConfiguration;

/**
 * Returns the optimized path for a microplate where all wells are selected, as well as all positions.
 * Requires that the number of wells in each row and column is even, as well as that the number of rows and columns in each position is not even.
 * The returned path for this case is the global optimum. 
 * @author Moritz Lang
 *
 */
public class FullMicroplateOptimizer implements PathOptimizer
{

	@Override
	public Iterable<PathOptimizerPosition> getPath(MicroplatePositionConfiguration posConf)
	{
		if(!isApplicable(posConf))
			return null;
		
		Vector<PathOptimizerPosition> positions = new Vector<PathOptimizerPosition>();
		
		int numWellsX = posConf.getNumWellsX();
		int numWellsY = posConf.getNumWellsY();
		
		// current direction
		boolean forward = true;
		boolean upward = true;
		
		// start well
		int wellX = 0;
		int wellY = numWellsY / 2 - 1;
		
		while(true)
		{
			// Add current well and all its positions...
			addWell(posConf, positions, wellX, wellY);
			
			// goto next well
			if(upward)
				wellY--;
			else
				wellY++;
			
			// Check if we should go right or left
			if(wellY < 0)
			{
				// Hitting the top, only possible in forward
				wellY = 0;
				wellX++;
				upward = false;
			}
			else if(wellY >= numWellsY)
			{
				// hintting the bottom, only possible in backward
				wellY = numWellsY - 1;
				wellX--;
				upward = true;
			}
			else if(forward && wellY >= numWellsY/2)
			{
				// hitting the middle in forward
				wellY = numWellsY/2-1;
				wellX++;
				upward = true;
			}
			else if(!forward && wellY <= numWellsY/2 - 1)
			{
				// hitting the middle in backward
				wellY = numWellsY/2;
				wellX--;
				upward = false;
			}
			
			// Check if forward or backward is finished
			if(wellX >= numWellsX)
			{
				// Change to back iteration
				wellX = numWellsX-1;
				wellY++;
				upward = false;
				forward = false;
			}
			else if(wellX < 0)
			{
				// finish
				break;
			}
		}
		return positions;
	}
	
	private void addWell(MicroplatePositionConfiguration posConf, Vector<PathOptimizerPosition> positions, int wellX, int wellY)
	{
		if(posConf.getWellNumPositionsX() == 1)
		{
			positions.addElement(new PathOptimizerPosition(posConf.getPosition(new Well(wellY, wellX), 0, 0), wellY, wellX, 0, 0));
		}
		else
		{
			int numWellsY = posConf.getNumWellsY();
					
			// we have four different ways to iterate over all positions depending on the well we are in.
			// see each sub-function for meaning.
			if(wellY == 0)
			{
				addTopWellPositions(posConf, positions, wellX, wellY);
			}
			else if(wellY == numWellsY - 1)
			{
				addBottomWellPositions(posConf, positions, wellX, wellY);
			}
			else if(wellX % 2 == 0)
			{
				addUnevenWellPositions(posConf, positions, wellX, wellY);
			}
			else
			{
				addEvenWellPositions(posConf, positions, wellX, wellY);
			}
		}
	}
	
	private void addTopWellPositions(MicroplatePositionConfiguration posConf, Vector<PathOptimizerPosition> positions, int wellX, int wellY)
	{
		/*
		 *  +--+--+
		 *  |     |
		 *  +--+  +
		 *     |  |
		 *  +--+  +   
		 */
		
		int numPosX = posConf.getWellNumPositionsX();
		int numPosY = posConf.getWellNumPositionsY();
		
		// run zick-zack up at the left
		boolean right = true;
		int posY = numPosY -1;
		int posX = 0;
		while(true)
		{
			// add position
			positions.addElement(new PathOptimizerPosition(posConf.getPosition(new Well(wellY, wellX), posY, posX), wellY, wellX, posY, posX));
			// go right/left
			if(right)
				posX++;
			else
				posX--;
			
			// go up
			if(posX < 0)
			{
				posX = 0;
				posY--;
				right = true;
			}
			else if(posX >= numPosX - 1)
			{
				posX = numPosX -2;
				posY--;
				right = false;
			}
			
			// check if finished
			if(posY < 0)
				break;
		}
		
		// run down at the right
		posX = numPosX-1;
		for(posY = 0; posY < numPosY; posY++)
		{
			// add position
			positions.addElement(new PathOptimizerPosition(posConf.getPosition(new Well(wellY, wellX), posY, posX), wellY, wellX, posY, posX));
		}
	}
	
	private void addBottomWellPositions(MicroplatePositionConfiguration posConf, Vector<PathOptimizerPosition> positions, int wellX, int wellY)
	{
		/*
		 *  +  +--+
		 *  |  |
		 *  +  +--+
		 *  |     |
		 *  +--+--+   
		 */
		
		int numPosX = posConf.getWellNumPositionsX();
		int numPosY = posConf.getWellNumPositionsY();
		
		// run zick-zack down at the right
		boolean right = false;
		int posX = numPosX -1;
		int posY = 0;
		while(true)
		{
			// add position
			positions.addElement(new PathOptimizerPosition(posConf.getPosition(new Well(wellY, wellX), posY, posX), wellY, wellX, posY, posX));
			// go right/left
			if(right)
				posX++;
			else
				posX--;
			
			// go up
			if(posX < 1)
			{
				posX = 1;
				posY++;
				right = true;
			}
			else if(posX >= numPosX)
			{
				posX = numPosX -1;
				posY++;
				right = false;
			}
			
			// check if finished
			if(posY >= numPosY)
				break;
		}
		
		// run up at the left
		posX = 0;
		for(posY = numPosY-1; posY >= 0; posY--)
		{
			// add position
			positions.addElement(new PathOptimizerPosition(posConf.getPosition(new Well(wellY, wellX), posY, posX), wellY, wellX, posY, posX));
		}
	}
	
	private void addUnevenWellPositions(MicroplatePositionConfiguration posConf, Vector<PathOptimizerPosition> positions, int wellX, int wellY)
	{
		/*
		 *  +--+--+
		 *        |
		 *  +--+  +
		 *  |  |  |
		 *  +  +--+   
		 */
		
		int numPosX = posConf.getWellNumPositionsX();
		int numPosY = posConf.getWellNumPositionsY();
		
		// run zick-zack right at the bottom
		boolean up = true;
		int posX = 0;
		int posY = numPosY - 1;
		while(true)
		{
			// add position
			positions.addElement(new PathOptimizerPosition(posConf.getPosition(new Well(wellY, wellX), posY, posX), wellY, wellX, posY, posX));
			// go up, down
			if(up)
				posY--;
			else
				posY++;
			
			// go right
			if(posY < 1)
			{
				posY = 1;
				posX++;
				up = false;
			}
			else if(posY >= numPosY)
			{
				posY = numPosY -1;
				posX++;
				up = true;
			}
			
			// check if finished
			if(posX >= numPosX)
				break;
		}
		
		// run left at the top
		posY = 0;
		for(posX = numPosX-1; posX >= 0; posX--)
		{
			// add position
			positions.addElement(new PathOptimizerPosition(posConf.getPosition(new Well(wellY, wellX), posY, posX), wellY, wellX, posY, posX));
		}
	}
	
	private void addEvenWellPositions(MicroplatePositionConfiguration posConf, Vector<PathOptimizerPosition> positions, int wellX, int wellY)
	{
		/*
		 *  +--+  +
		 *  |  |  |
		 *  +  +--+
		 *  |
		 *  +--+--+
		 */
		
		int numPosX = posConf.getWellNumPositionsX();
		int numPosY = posConf.getWellNumPositionsY();
		
		// run zick-zack left at the top
		boolean up = false;
		int posY = 0;
		int posX = numPosX-1;
		while(true)
		{
			// add position
			positions.addElement(new PathOptimizerPosition(posConf.getPosition(new Well(wellY, wellX), posY, posX), wellY, wellX, posY, posX));
			// go right/left
			if(up)
				posY--;
			else
				posY++;
			
			// go up
			if(posY < 0)
			{
				posY = 0;
				posX--;
				up = false;
			}
			else if(posY >= numPosY - 1)
			{
				posY = numPosY -2;
				posX--;
				up = true;
			}
			
			// check if finished
			if(posX < 0)
				break;
		}
		
		// run down at the right
		posY = numPosY-1;
		for(posX = 0; posX < numPosX; posX++)
		{
			// add position
			positions.addElement(new PathOptimizerPosition(posConf.getPosition(new Well(wellY, wellX), posY, posX), wellY, wellX, posY, posX));
		}
	}

	@Override
	public boolean isApplicable(MicroplatePositionConfiguration posConf)
	{
		if(!posConf.isAliasMicroplate()
				&& posConf.isAllPositionsSelected()
				&& posConf.isAllWellsSelected()
				&& posConf.getNumWellsX() % 2 == 0
				&& posConf.getNumWellsY() % 2 == 0
				&& posConf.getWellNumPositionsX() % 2 == 1
				&& posConf.getWellNumPositionsY() % 2 == 1
				&& posConf.getWellNumPositionsX() == posConf.getWellNumPositionsY())
			return true;
		return false;
	}

	@Override
	public double getSpecificity(MicroplatePositionConfiguration posConf)
	{
		// For the problems this optimizer can handle, it is optimal.
		if(!isApplicable(posConf))
			return -1;
		return 1;
	}

	@Override
	public String getName()
	{
		return "Screening Path";
	}

	@Override
	public String getOptimizerID()
	{
		return "YouScope.FullPlatePathOptimizer";
	}

}
