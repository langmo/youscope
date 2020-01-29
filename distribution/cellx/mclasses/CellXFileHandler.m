classdef CellXFileHandler < handle
    %UNTITLED6 Summary of this class goes here
    %   Detailed explanation goes here
    
    properties (SetAccess=private)
        resultsDir;
        fileSets = CellXFileSet.empty(0,0);
        trackingMatResultFile;
        
        % filename for the complete result table if tracking is enabled
        trackingTxtResultFile;
        
        % filename for the complete result table if tracking is disabled
        seriesTxtResultFile;
        
        lineageControlImage;
        doTracking;
    end
    
    
    properties(Constant)
        % XML 
        fileSeriesXMLRootNodeName = 'CellXFileSeries';
        fileSetXMLNodeName = 'CellXFileSet';
        fileSetFrameAttrName = 'frame';
        fileSetPositionAttrName = 'position'; 
        oofImageXMLNodeName = 'oofImage';
        fluoImageXMLNodeName = 'fluoImage';
        ffImageXMLNodeName = 'ffImage';
        fluoSetXMLNodeName = 'fluoSet';
        fluoTypeAttrName = 'type';
               
        rootNodeName ='CellXFiles';
        timeSeriesNodeName = 'CellXTimeSeries';
        resultDirNodeName = 'CellXResultDir';
        idAttrName = 'id';
        posAttrName = 'position';
        fluoTypesAttrName = 'fluotypes';
        trackingAttrName = 'tracking';
    end
    
    
    methods
        
        function obj = CellXFileHandler(dir)          
           obj.setResultDir(dir);
        end
               
        function setResultDir(this, dir)
            if( dir(end-1:end)~=filesep )
                dir = [dir filesep];
           end
            this.resultsDir = dir;
            this.trackingMatResultFile = [dir 'trackData.mat'];
            this.trackingTxtResultFile = [dir 'timeCourseResults.txt'];
            this.seriesTxtResultFile   = [dir 'seriesResults.txt'];
            this.lineageControlImage   = [dir 'lineage.png'];   
                    
            for k=1:length(this.fileSets)
                this.fileSets(k).setResultsDirectory(dir);
            end
                     
        end
        
        function n = getNumberOfFileSets(this)
            n = numel(this.fileSets);
        end
        
        function addFileSet(this, fs)
            this.fileSets(end+1)=fs;
        end
        
        function setTrackingEnabled(this, value)
            this.doTracking = value;
        end
        
    end
    
    
    methods(Static)
        
        
        function this = fromXML(file, resultsDir)
            this = CellXFileHandler(resultsDir);   

            doc = xmlread(file);
            root = doc.getDocumentElement();
            rootName = char(root.getNodeName);

            if( strcmp(rootName, this.fileSeriesXMLRootNodeName)==0 )
                error('Wrong XML format, root %s ', rootName )
            end
                  
            paramNodes = root.getElementsByTagName(this.fileSetXMLNodeName);
            n = paramNodes.getLength;
            for i=1:n
                node = paramNodes.item(i-1);
                frame = str2double(char(node.getAttribute(this.fileSetFrameAttrName)));
                oofNode = node.getElementsByTagName(this.oofImageXMLNodeName).item(0);                
                oofFile = oofNode.getTextContent;
                cellXFileSet = CellXFileSet(frame, char(oofFile));
                cellXFileSet.setResultsDirectory(resultsDir);
                fluoSets = node.getElementsByTagName(this.fluoSetXMLNodeName);              
                for j=1:fluoSets.getLength
                
                    fluoSet = fluoSets.item(j-1);
                    
                    fluoType = fluoSet.getAttribute(this.fluoTypeAttrName);
                    
                    fluoImageNode = fluoSet.getElementsByTagName(this.fluoImageXMLNodeName).item(0);
                    
                    fluoImage = fluoImageNode.getTextContent;
                    
                    ff = fluoSet.getElementsByTagName(this.ffImageXMLNodeName);
                    
                    if( ff.getLength>0 )
                        ffImage = ff.item(0).getTextContent;
                        cellXFileSet.addFluoImageTag(char(fluoImage), char(fluoType), char(ffImage));
                    else
                        cellXFileSet.addFluoImageTag(char(fluoImage), char(fluoType));
                    end

                    this.fileSets(end+1) = cellXFileSet;
                    
                end
                
            end
        end
    
    
        

        function this = readCellXFilesXML(xmlFile, posIdx)
            doc = xmlread(xmlFile);
            root = doc.getDocumentElement();
            rootName = char(root.getNodeName);
            if( strcmp(rootName, CellXFileHandler.rootNodeName)==0 )
                error('Wrong XML format, root %s ', rootName )
            end         
            timeSeriesNodes = root.getElementsByTagName(CellXFileHandler.timeSeriesNodeName);
            
            queryTS = timeSeriesNodes.item(posIdx-1);     
            resultDir = CellXFileHandler.getResultDir(queryTS);
            this = CellXFileHandler(resultDir);
            this.doTracking = CellXFileHandler.getTrackingAttribute(queryTS);
                     
            fileSetList = queryTS.getElementsByTagName(CellXFileHandler.fileSetXMLNodeName);
            n = fileSetList.getLength();
            for i=1:n
                this.fileSets(end+1) = CellXFileHandler.getFileSet(fileSetList.item(i-1), resultDir);
            end
            
        end
        
        
        
        function ret = getResultDir(timeSeriesNode)
            
            fluoTypes = strtrim(char(timeSeriesNode.getAttribute(CellXFileHandler.fluoTypesAttrName)));
            
            resultDirList = timeSeriesNode.getElementsByTagName(CellXFileHandler.resultDirNodeName);
            if( resultDirList.getLength()==0 )
                ret = [];
                sprintf('Warning: No result dir in XML defined');
            else
                ret = char(resultDirList.item(0).getTextContent);             
                if( ~strcmp(fluoTypes,'') )
                    if( ~strcmp(ret(end-1:end), filesep) )
                        ret = [ret filesep];
                    end
                    ret = [ret fluoTypes];
                end              
            end
        end
        
        
        function ret = getTrackingAttribute(timeSeriesNode)
            if( timeSeriesNode.hasAttribute(CellXFileHandler.trackingAttrName) )
                ret = strcmp('1', timeSeriesNode.getAttribute(CellXFileHandler.trackingAttrName));
            else
                ret = 0;
                sprintf('Warning: Tracking not defined in XML');
            end
        end
        
        
        function cellXFileSet = getFileSet(node, resultDir)
            frame = str2double(char(node.getAttribute(CellXFileHandler.fileSetFrameAttrName)));
            oofNode = node.getElementsByTagName(CellXFileHandler.oofImageXMLNodeName).item(0);
            oofFile = oofNode.getTextContent;
            cellXFileSet = CellXFileSet(frame, char(oofFile));
            cellXFileSet.setResultsDirectory(resultDir);
            fluoSets = node.getElementsByTagName(CellXFileHandler.fluoSetXMLNodeName);
            for j=1:fluoSets.getLength           
                fluoSet = fluoSets.item(j-1);            
                fluoType = fluoSet.getAttribute(CellXFileHandler.fluoTypeAttrName);             
                fluoImageNode = fluoSet.getElementsByTagName(CellXFileHandler.fluoImageXMLNodeName).item(0);             
                fluoImage = fluoImageNode.getTextContent;         
                ff = fluoSet.getElementsByTagName(CellXFileHandler.ffImageXMLNodeName);       
                if( ff.getLength>0 )
                    ffImage = ff.item(0).getTextContent;
                    cellXFileSet.addFluoImageTag(char(fluoImage), char(fluoType), char(ffImage));
                else
                    cellXFileSet.addFluoImageTag(char(fluoImage), char(fluoType));
                end
            end
        end
        
        
    end
    
    
end

