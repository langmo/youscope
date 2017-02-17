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
package org.youscope.server;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Date;
import java.util.LinkedList;
import java.util.concurrent.locks.ReentrantLock;

import org.youscope.common.table.RowView;
import org.youscope.common.table.Table;
import org.youscope.common.table.TableListener;

class TableDataSaver extends UnicastRemoteObject implements TableListener, Runnable {

	/**
	 * Serial Version UID.
	 */
	private static final long				serialVersionUID	= 7112259208478805045L;
	private volatile Writer					tableWriter			= null;
	
	private final String					tableSaveName;
	private final MeasurementSaverImpl.SaverInformation supervisor;
	
	private volatile boolean				dataReceived		= false;
	private final String[]					defaultColumns		= {"Evaluation", "Measurement Time (ms)", "Absolute Time (ms)", "Absolute Time String", "Well", "Position"};

	private volatile LinkedList<String[]>	toBeSavedEntries	= null;

	private final ReentrantLock				writeLock			= new ReentrantLock();
	
	private final boolean isImageTable;

	/**
	 * Constructor.
	 * @throws RemoteException
	 */
	TableDataSaver(String tableSaveName, MeasurementSaverImpl.SaverInformation supervisor, boolean isImageTable) throws RemoteException
	{
		super();
		this.tableSaveName = tableSaveName;
		this.supervisor = supervisor;
		this.isImageTable = isImageTable;
	}
	
	/**
	 * Constructor.
	 * @throws RemoteException
	 */
	TableDataSaver(String tableSaveName, MeasurementSaverImpl.SaverInformation supervisor) throws RemoteException
	{
		this(tableSaveName, supervisor, false);
	}

	@Override
	public synchronized void newTableProduced(Table table) throws RemoteException
	{
		if(!supervisor.isReady())
			return;

		boolean sendToExecutor;
		synchronized(this)
		{
			sendToExecutor = toBeSavedEntries == null;
			if(sendToExecutor)
				toBeSavedEntries = new LinkedList<String[]>();
			if(!dataReceived)
			{
				// append default information (well, time, etc.)
				String[] columnNames = new String[table.getNumColumns() + defaultColumns.length];
				System.arraycopy(defaultColumns, 0, columnNames, 0, defaultColumns.length);
				System.arraycopy(table.getColumnNames(), 0, columnNames, defaultColumns.length, table.getNumColumns());
				toBeSavedEntries.add(columnNames);
				dataReceived = true;
			}
			for(RowView rowView : table)
			{
				// append default information (well, time, etc.)
				String[] saveEntry = new String[rowView.getNumColumns() + defaultColumns.length];
				saveEntry[0] = table.getExecutionInformation() == null ? "" : table.getExecutionInformation().getEvaluationString();
				saveEntry[1] = (table.getExecutionInformation() == null) ? "" : Long.toString(table.getCreationRuntime());
				saveEntry[2] = Long.toString(table.getCreationTime());
				saveEntry[3] = new Date(table.getCreationTime()).toString();
				saveEntry[4] = (table.getPositionInformation() != null && table.getPositionInformation().getWell() != null) ? table.getPositionInformation().getWell().getWellName() : "";
				saveEntry[5] = table.getPositionInformation() != null ? table.getPositionInformation().getPositionsString() : "";
				
				for(int i=0; i< rowView.getNumColumns(); i++)
				{
					String value =  rowView.get(i).getValueAsString();
					saveEntry[6+i] = value == null ? "" : value;
				}
				toBeSavedEntries.add(saveEntry);
			}
		}
		// only queue this for execution if this is the first row arrived since the last execution.
		// otherwise, it is already queued.
		if(sendToExecutor)
			FileSaverManager.execute(this);
	}

	String getTableSaveName()
	{
		return tableSaveName;
	}

	@Override
	public void run()
	{
		writeLock.lock();
		try
		{
			// Get entries which should be saved, and reset list.
			LinkedList<String[]> tempList;
			synchronized(this)
			{
				tempList = toBeSavedEntries;
				toBeSavedEntries = null;
			}
			if(tempList == null)
				return;

			// prepare CSV text
			StringBuffer writeData = new StringBuffer(tempList.size() * 50);
			for(String[] data : tempList)
			{
				for(int i = 0; i < data.length; i++)
				{
					if(i > 0)
						writeData.append(";");
					if(data[i] != null)
					{
						writeData.append("\"");
						writeData.append(data[i].replaceAll("\"", "\"\"").replaceAll("\n", " "));
						writeData.append("\"");
					}
				}
				writeData.append("\n");
			}

			boolean reopenedFile = false;
			if(tableWriter == null)
			{
				// This is the case when a measurement already finished, but table data is still incoming.
				// This can happen shortly after a measurement finished, since the table data is saved asynchronously.
				// We just open a new file writer to append the data and close it afterwards again...
				if(!restartWriting())
					return;
				reopenedFile = true;
			}

			// write CSV text
			try
			{
				tableWriter.write(writeData.toString());
				tableWriter.flush();
			}
			catch(IOException e1)
			{
				ServerSystem.err.println("Could not append data to table " + getTableSaveName() + ".", e1);
			}
			if(reopenedFile)
			{
				endWriting();
			}
		}
		finally
		{
			writeLock.unlock();
		}
	}

	private boolean restartWriting()
	{
		String filePath;
		try
		{
			if(isImageTable)
				filePath = supervisor.getFullImageTablePath();
			else
				filePath = supervisor.getFullTablePath(tableSaveName);
		}
		catch(Exception e)
		{
			ServerSystem.err.println("Could not determine where to save table data \"" + getTableSaveName() + ".", e);
			return false;
		}
		if(filePath == null)
		{
			ServerSystem.err.println("Could not create file to save table data \"" + getTableSaveName() + " since table path is null.", null);
			return false;
		}
		writeLock.lock();
		try
		{
			try
			{
				File folder = new File(filePath).getParentFile();
				if(!folder.exists())
					folder.mkdirs();
				tableWriter = new FileWriter(filePath, true);
				return true;
			}
			catch(IOException e1)
			{
				tableWriter = null;
				ServerSystem.err.println("Could not create file to save table data \"" + getTableSaveName() + ".", e1);
				return false;
			}
		}
		finally
		{
			writeLock.unlock();
		}

	}

	public boolean startWriting()
	{
		String filePath;
		try
		{
			if(isImageTable)
				filePath = supervisor.getFullImageTablePath();
			else
				filePath = supervisor.getFullTablePath(tableSaveName);
		}
		catch(Exception e)
		{
			ServerSystem.err.println("Could not determine where to save table data \"" + getTableSaveName() + ".", e);
			return false;
		}
		if(filePath == null)
		{
			ServerSystem.err.println("Could not create file to save table data \"" + getTableSaveName() + " since table path is null.", null);
			return false;
		}
		writeLock.lock();
		try
		{
			try
			{
				File file = new File(filePath);
				File folder = file.getParentFile();
				if(!folder.exists())
					folder.mkdirs();
				dataReceived = file.exists();
				if(tableWriter != null)
				{
					tableWriter.flush();
					tableWriter.close();
				}
				tableWriter = new FileWriter(filePath, true);
				return true;
			}
			catch(IOException e1)
			{
				ServerSystem.err.println("Could not create file to save table data \"" + getTableSaveName() + ".", e1);
				return false;
			}
		}
		finally
		{
			writeLock.unlock();
		}
	}

	public void endWriting()
	{
		writeLock.lock();
		try
		{
			if(tableWriter == null)
				return;

			try
			{
				tableWriter.flush();
				tableWriter.close();
			}
			catch(IOException e)
			{
				ServerSystem.err.println("Could not close file of table data \"" + getTableSaveName() + ".", e);
			}
			tableWriter = null;
		}
		finally
		{
			writeLock.unlock();
		}
	}
}
