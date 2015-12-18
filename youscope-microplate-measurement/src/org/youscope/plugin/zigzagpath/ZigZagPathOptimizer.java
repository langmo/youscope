/**
 * 
 */
package org.youscope.plugin.zigzagpath;

import java.util.Vector;

import org.youscope.addon.pathoptimizer.PathOptimizer;
import org.youscope.addon.pathoptimizer.PathOptimizerPosition;
import org.youscope.common.measurement.microplate.Well;
import org.youscope.plugin.microplatemeasurement.MicroplatePositionConfiguration;

/**
 * An optimizer which assumes no specific selected wells and positions. Just iterates over all wells in a zick-zack manner
 * (first row from lowest well to highest, second row from highest to lowest, and so forth). For every well, it also iterates in a
 * zick-zack manner over all positions. However, it alternates iterating from top to bottom and from bottom to top, as well as starts
 * iterating from the leftmost well if the well iteration is currently positive, and vice versa.
 * Probably the best general optimizer if no specific information about selected wells and positions can be assumed, and if no specific optimization algorithm should be run.
 * @author Moritz Lang
 *
 */
public class ZigZagPathOptimizer implements PathOptimizer
{

	@Override
	public Iterable<PathOptimizerPosition> getPath(MicroplatePositionConfiguration posConf)
	{
		Vector<PathOptimizerPosition> positions = new Vector<PathOptimizerPosition>();
		
		boolean isInverseWellRow = false;
		for(int wellY = 0; wellY < posConf.getNumWellsY(); wellY++)
		{
			boolean wellRowExists = false;
			boolean isInversePosColumn = false;
			for(int wellX = isInverseWellRow ? posConf.getNumWellsX() - 1 : 0; isInverseWellRow ? wellX >= 0 : wellX < posConf.getNumWellsX(); wellX = isInverseWellRow ? wellX - 1 : wellX + 1)
			{
				if(!posConf.isMeasureWell(new Well(wellY, wellX)))
					continue;
				boolean posColumnExists = false;
				boolean isInversePosRow = isInverseWellRow;
				for(int posY = isInversePosColumn ? posConf.getWellNumPositionsY() - 1 : 0; isInversePosColumn ? posY >= 0 : posY < posConf.getWellNumPositionsY(); posY = isInversePosColumn ? posY - 1 : posY + 1)
				{
					boolean posRowExists = false;
					for(int posX = isInversePosRow ? posConf.getWellNumPositionsX() - 1 : 0; isInversePosRow ? posX >= 0 : posX < posConf.getWellNumPositionsX(); posX = isInversePosRow ? posX - 1 : posX + 1)
					{
						if(!posConf.isMeasurePosition(posY, posX))
							continue;
						wellRowExists = true;
						posRowExists = true;
						posColumnExists = true;
						
						positions.addElement(new PathOptimizerPosition(posConf.getPosition(new Well(wellY, wellX), posY, posX), wellY, wellX, posY, posX));
					}
					// Change direction if at least one position was active
					if(posRowExists)
						isInversePosRow = !isInversePosRow;
				}
				// Change direction if at least one position was active
				if(posColumnExists)
					isInversePosColumn = !isInversePosColumn;
			}

			// Change direction if at least one well and position was active
			if(wellRowExists)
				isInverseWellRow = !isInverseWellRow;
		}
		return positions;
	}

	@Override
	public boolean isApplicable(MicroplatePositionConfiguration posConf)
	{
		if(!posConf.isAliasMicroplate())
			return true;
		return false;
	}

	@Override
	public double getSpecificity(MicroplatePositionConfiguration posConf)
	{
		// not very specific, but better than no optimization at all...
		if(isApplicable(posConf))
			return 0.1;
		return -1;
	}

	@Override
	public String getName()
	{
		return "Zig-Zag Path";
	}

	@Override
	public String getOptimizerID()
	{
		return "YouScope.ZigZagPathOptimizer";
	}

}
