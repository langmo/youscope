/**
 * 
 */
package org.youscope.plugin.nonoptimizedpath;

import java.util.Vector;

import org.youscope.addon.pathoptimizer.PathOptimizer;
import org.youscope.addon.pathoptimizer.PathOptimizerPosition;
import org.youscope.common.Well;
import org.youscope.plugin.microplatemeasurement.MicroplatePositionConfigurationDTO;

/**
 * This optimizer actually does not really optimize the path in any way, but just iterates through all wells from left to right and from top to bottom.
 * The same for the positions in a well.
 * @author Moritz Lang
 *
 */
public class NonOptimizedOptimizer implements PathOptimizer
{

	@Override
	public Iterable<PathOptimizerPosition> getPath(MicroplatePositionConfigurationDTO posConf)
	{
		Vector<PathOptimizerPosition> positions = new Vector<PathOptimizerPosition>();
		
		for(int wellY = 0; wellY < posConf.getNumWellsY(); wellY++)
		{
			for(int wellX = 0; wellX < posConf.getNumWellsX(); wellX++)
			{
				if(!posConf.isMeasureWell(new Well(wellY, wellX)))
					continue;
				for(int posY = 0; posY < posConf.getWellNumPositionsY(); posY++)
				{
					for(int posX = 0; posX < posConf.getWellNumPositionsX(); posX++)
					{
						if(!posConf.isMeasurePosition(posY, posX))
							continue;
						positions.addElement(new PathOptimizerPosition(posConf.getPosition(new Well(wellY, wellX), posY, posX), wellY, wellX, posY, posX));
					}
				}
			}
		}
		return positions;
	}

	@Override
	public boolean isApplicable(MicroplatePositionConfigurationDTO posConf)
	{
		// Always applicable.
		return true;
	}

	@Override
	public double getSpecificity(MicroplatePositionConfigurationDTO posConf)
	{
		// absolutely not optimal.
		return 0.0;
	}

	@Override
	public String getName()
	{
		return "Non-Optimized Path";
	}

	@Override
	public String getOptimizerID()
	{
		return "CSB::NonOptimizedOptimizer";
	}

}
