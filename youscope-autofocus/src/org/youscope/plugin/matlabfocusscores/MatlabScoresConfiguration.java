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
package org.youscope.plugin.matlabfocusscores;

import org.youscope.addon.focusscore.FocusScoreConfiguration;
import org.youscope.common.configuration.ConfigurationException;
import org.youscope.common.configuration.YSConfigAlias;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * @author Moritz Lang
 *
 */
@YSConfigAlias("Matlab based score")
@XStreamAlias("matlab-focus-score")
public class MatlabScoresConfiguration extends FocusScoreConfiguration
{

	/**
	 * Serial Version UID.
	 */
	private static final long	serialVersionUID	= -1111201676160652236L;
	
	@YSConfigAlias("Matlab algorithm")
	private MatlabScoreType scoreAlgorithm = MatlabScoreType.SOBEL3;
	
	/**
	 * Constructor.
	 */
	public MatlabScoresConfiguration()
	{
		// do nothing.
	}

	/**
	 * The identifier for this configuration.
	 */
	public static final String	CONFIGURATION_ID	= "YouScope.MatlabFocusScore";
	
	@Override
	public String getTypeIdentifier()
	{
		return CONFIGURATION_ID;
	}

	/**
	 * Sets the algorithm for the focus score calculation.
	 * @param scoreAlgorithm
	 */
	public void setScoreAlgorithm(MatlabScoreType scoreAlgorithm)
	{
		this.scoreAlgorithm = scoreAlgorithm;
	}

	/**
	 * @return algorithm to calculate the score.
	 */
	public MatlabScoreType getScoreAlgorithm()
	{
		return scoreAlgorithm;
	}

	@Override
	public void checkConfiguration() throws ConfigurationException {
		// nothing to check.
		
	}
}
