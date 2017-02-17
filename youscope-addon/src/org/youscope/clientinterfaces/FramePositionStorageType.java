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
package org.youscope.clientinterfaces;

/**
 * Enum defining how frame positions should be stored.
 * @author Moritz Lang
 *
 */
public enum FramePositionStorageType 
{
	/**
	 * Do not store window positions.
	 */
	NONE      ("none",      false, false, false, false, false, "Do not store window positions."),
	/**
	 * Store window positions
	 */
    ALL ("all",                 true, true, true, true, true, "Store window positions."),
    /**
     * Do not store new, but apply existing positions.
     */
    READ_ONLY ("read",      true, false, false, false, true, "Do not store new, but apply existing positions."),
    /**
     * Do not store new, but apply and update existing positions.
     */
    READ_UPDATE ("read_update", true, true, true, false, true, "Do not store new, but apply and update existing positions.");
    
    private final String identifier;
    private final String description;
    private final boolean readDisk;
    private final boolean writeDisk;
    private final boolean updatePositions;
    private final boolean storeNewPositions;
    private final boolean positionFrames;
    FramePositionStorageType(String identifier, boolean readDisk, boolean writeDisk, boolean updatePositions, boolean storeNewPositions, boolean positionFrames, String description)
    {
        this.identifier = identifier;
        this.readDisk = readDisk;
        this.writeDisk = writeDisk;
        this.updatePositions = updatePositions;
        this.storeNewPositions = storeNewPositions;
        this.positionFrames = positionFrames;
        this.description = description;
    }
    /**
     * True if the position of frames should be set to the last position.
     * @return True if newly created frames should be set to their previous position.
     */
    public boolean isPositionFrames()
    {
        return positionFrames;
    }
    /**
     * True if frame positions should be read from a position config file at startup.
     * @return True to read positions on startup.
     */
    public boolean isReadDisk()
    {
        return readDisk;
    }
    /**
     * True if frame positions should be stored in a config file upon shutdown.
     * @return True if frame positions should be stored.
     */
    public boolean isWriteDisk()
    {
        return writeDisk;
    }
    /**
     * True if existing frame positions should be updated when user moves frames around.
     * @return true if positions get updated.
     */
    public boolean isUpdatePositions()
    {
        return updatePositions;
    }
    /**
     * True if positions of frames for which yet no position is stored, should be stored.
     * @return True to add new frame positions.
     */
    public boolean isStoreNewPositions()
    {
        return storeNewPositions;
    }
    /**
     * True if the identifier refers to this frame position storage type.
     * @param identifier identifier of storage type.
     * @return true if identifier refers to this storage type.
     */
    public boolean equals(String identifier)
    {
        return this.identifier.equals(identifier);
    }
    /**
     * Returns the identifier of this storage type.
     * @return identifier of this storage type. See {@link PropertyProvider#setProperty(String, String)}.
     */
    public String getIdentifier()
    {
        return identifier;
    }
    /**
     * Returns a short human readable description of this storage type.
     * @return Short human readable description.
     */
    public String getDescription()
    {
        return description;
    }
    @Override
    public String toString() 
    {
        return description;
    }
    /**
     * Returns the storage type corresponding to the identifier. If the identifier is null or unknown, returns {@link FramePositionStorageType#NONE}.
     * @param identifier Identifier of storage type.
     * @return The frame position storage type having the identifier, or NONE if unknown identifier.
     */
    public static FramePositionStorageType getType(String identifier)
    {
        if(identifier == null)
            return NONE;
        for(FramePositionStorageType type : FramePositionStorageType.values())
        {
            if(type.equals(identifier))
                return type;
        }
        return NONE;
    }
}
