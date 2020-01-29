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
package org.youscope.uielements.scripteditor;

import javax.swing.text.*;

/**
 * @author langmo
 * 
 */
class ScriptEditorViewFactory implements ViewFactory
{

	@Override
	public View create(Element elem)
	{
		String kind = elem.getName();
		if(kind != null)
		{
			if(kind.equals(AbstractDocument.ContentElementName))
			{
				return new LabelView(elem);
			}
			else if(kind.equals(AbstractDocument.ParagraphElementName))
			{
				//return new ScriptEditorParagraphView(elem);
				return new ParagraphView(elem);
			}
			else if(kind.equals(AbstractDocument.SectionElementName))
			{
				return new BoxView(elem, View.Y_AXIS);
			}
			else if(kind.equals(StyleConstants.ComponentElementName))
			{
				return new ComponentView(elem);
			}
			else if(kind.equals(StyleConstants.IconElementName))
			{
				return new IconView(elem);
			}
		}
		// default to text display
		return new LabelView(elem);

	}
}
