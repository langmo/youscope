package org.youscope.plugin.cellx;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import org.youscope.addon.celldetection.CellDetectionException;
import org.youscope.common.ExecutionInformation;
import org.youscope.common.PositionInformation;
import org.youscope.common.table.Table;
import org.youscope.common.table.TableDefinition;
import org.youscope.common.table.TableEntry;
import org.youscope.common.table.TableException;
import org.youscope.common.table.TemporaryRow;

class CellXSinkImpl extends UnicastRemoteObject implements CellXSink 
{
	/**
	 * Serial Version UID.
	 */
	private static final long serialVersionUID = 565305972641267479L;
	private final Table table;
	private final TableDefinition tableDefinition;
	public CellXSinkImpl(long creationTime, PositionInformation positionInformation, ExecutionInformation executionInformation) throws RemoteException {
		
		tableDefinition = CellXTable.getTableDefinition();
		table = new Table(tableDefinition, creationTime, positionInformation, executionInformation);
	}
	
	Table getTable()
	{
		return table;
	}

	@Override
	public void addCell(String[] headers, double[] data) throws RemoteException, CellDetectionException 
	{
		if(headers == null || data == null || headers.length != data.length)
			throw new CellDetectionException("Provide only headers and data with the same number of elements.");
		int numFluo = 0;
		for(int i=0; i<headers.length; i++)
		{
			if(headers[i].indexOf("fluo") != 0)
				continue;
			int fluoID;
			try
			{
				fluoID = Integer.parseInt(headers[i].substring(4, headers[i].indexOf('.')));
			}
			catch(Throwable throwable) 
			{
				throw new CellDetectionException("Couldn't find out index of fluorescence channel in table column with name "+headers[i]+".", throwable);
			}
			numFluo = fluoID>numFluo ? fluoID : numFluo;
		}
		TemporaryRow[] rows = new TemporaryRow[numFluo>0 ? numFluo : 1];
		for(int i=0; i<rows.length; i++)
		{
			try {
				rows[i] = table.createTemporaryRow();
				rows[i].get("fluo.id").setValue(i);
			} catch (TableException e) {
				throw new CellDetectionException("Couldn't create temporary row.", e);
			}
		}
		for(int i=0; i<headers.length; i++)
		{
			String header = headers[i];
			int fluoID = 0;
			if(header.indexOf("fluo") == 0)
			{
				try 
				{
					fluoID = Integer.parseInt(header.substring(4, header.indexOf('.')))-1;
				}
				catch(@SuppressWarnings("unused") Throwable throwable)
				{
					continue;
				}
				if(fluoID < 0 || fluoID >= rows.length)
					continue;
				header = "fluo"+header.substring(header.indexOf('.'));
			}
			try {
				TableEntry<?> entry = rows[fluoID].get(header);
				Class<?> valueType = entry.getValueType();
				if(valueType.equals(Double.class))
					entry.setValue(data[i]);
				else if(valueType.equals(Integer.class))
					entry.setValue((int)data[i]);
				else
					throw new CellDetectionException("Couldn't add data index "+Integer.toString(i)+" with header "+header+". Table expect entry of type "+valueType.toString());
			} catch (TableException e) {
				throw new CellDetectionException("Couldn't add data index "+Integer.toString(i)+" with header "+header+".", e);
			}
		}
		for(int i=0; i<rows.length; i++)
		{
			if(i>0)
			{
				try {
					rows[i].get(0).setValue(rows[0].get(0).getValue());
					rows[i].get(1).setValue(rows[0].get(1).getValue());
					rows[i].get(2).setValue(rows[0].get(2).getValue());
				} catch (IndexOutOfBoundsException | TableException e) {
					throw new CellDetectionException("Couldn't copy cell ID.", e);
				} 
			}
			try {
				table.addRow(rows[i]);
			} 
			catch (NullPointerException | TableException e) 
			{
				String content = "";
				for(int j = 0; j<rows[i].getNumColumns(); j++)
				{
					Serializable value = rows[i].getValue(j);
					content+="\n"+
							rows[i].getColumnName(j)+"="+(value==null ? "null":value.toString());
				}
				throw new CellDetectionException("Couldn't add row "+Integer.toString(i)+" with content "+content+".", e);
			}
		}
	}

}
