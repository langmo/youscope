classdef CellXPositionHandler < handle
    %CELLXPOSITIONHANDLER Here we go:)
    
    properties
        numberOfPositions;
        xmlFile;
    end
    
    properties (Constant)
        rootNodeName = 'CellXFiles';
        timeSeriesNodeName = 'CellXTimeSeries';
    end
    
    methods
          
        function this = CellXPositionHandler(xmlFile)          
            doc = xmlread(xmlFile);
            root = doc.getDocumentElement();
            rootName = char(root.getNodeName);
            if( strcmp(rootName, this.rootNodeName)==0 )
                error('Wrong XML format, root %s ', rootName )
            end         
            timeSeriesNodes = root.getElementsByTagName(this.timeSeriesNodeName);
            n = timeSeriesNodes.getLength;
            
            if( n==0 )
                error('No time series found.');
            end
            this.numberOfPositions = n;
            this.xmlFile = xmlFile;
        end
        
        function fh = getFileHandlerForPosition(this, posIdx)       
            if( posIdx>this.numberOfPositions || posIdx==0 )
                error('Position index out of range %d (min=%d, max=%d)', posIdx, 1,this.numberOfPositions );
            end           
            fh = CellXFileHandler.fromCellXFilesXML(this.xmlFile, posIdx);           
        end
        
    end
    
end

