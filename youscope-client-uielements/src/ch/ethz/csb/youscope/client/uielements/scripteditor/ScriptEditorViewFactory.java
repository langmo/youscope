/**
 * 
 */
package ch.ethz.csb.youscope.client.uielements.scripteditor;

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
