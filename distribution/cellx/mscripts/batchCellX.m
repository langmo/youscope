function exitValue = batchCellX(varargin)
%
% This is the deploy function of CellX.
%


% Parse command line arguments
cmdParser = CellXCommandLineParser();
try
    cmdParser.parse(varargin);
catch exception
    cmdParser.showUsage();
    exitValue = CellXExceptionHandler.handleException(exception);
    if( isdeployed() )
        exit(exitValue);
    end
    return;
end

% Read parameter file
config = CellXConfiguration.readXML(cmdParser.configFile);
config.check();


if( cmdParser.execMode==1 ) % process a file series
    
    cph = CellXPositionHandler(cmdParser.seriesFile);  
    
    if( cmdParser.hasTrackIndex() )
        fprintf('Processing series %d\n', cmdParser.trackNumber);
        fileHandler = CellXFileHandler.readCellXFilesXML(cmdParser.seriesFile, cmdParser.trackNumber);
        
        if( cmdParser.hasTrackingSwitch() )
            fileHandler.setTrackingEnabled(cmdParser.tracking);
        end
        
        if( cmdParser.hasResultDir() )
            fileHandler.setResultDir(cmdParser.resultDir);
        end

        exitValue = runMode1(config, fileHandler, cmdParser);
        
        if( exitValue~=0 )
            if( isdeployed() )
                exit(exitValue);
            end
            return;
        end
    else
        fprintf('Number of series: %d\n', cph.numberOfPositions);
        for p=1:cph.numberOfPositions
            fileHandler = CellXFileHandler.readCellXFilesXML(cmdParser.seriesFile, p);
            
            if( cmdParser.hasTrackingSwitch() )
                fileHandler.setTrackingEnabled(cmdParser.tracking);
            end

            exitValue = runMode1(config, fileHandler, cmdParser);
            
            if( exitValue~=0 )
                if( isdeployed() )
                    exit(exitValue);
                end
                return;
            end
        end
    end
    
elseif( cmdParser.execMode==2 ) % process a set of files possibly in test mode
    exitValue = runMode2(config, cmdParser);
elseif( cmdParser.execMode==3) % do the tracking only - not yet implemented, required for cluster
    exitValue = runMode3(config);
else % should not be reached
    error('Unknown exec mode');
end

fprintf('done\n');
if( isdeployed() )
        exit(0);
end
return;



% Process one file series
    function ret = runMode1(config, fileHandler, cmdParser)
        ret=1;
        disp(config);
        fprintf('Number of file sets: %d\n', fileHandler.getNumberOfFileSets);
        
        first = 1;
        last  = fileHandler.getNumberOfFileSets; 
        
        if( ~isempty(cmdParser.setIndex) )
            first = cmdParser.setIndex;
            last  = cmdParser.setIndex;
        end
        
        if( cmdParser.trackOnly==1 )
            last = 0;
        end
        
        for k=first:last
            fSet = fileHandler.fileSets(k);
            
            fprintf('Processing file set %d (frame %d)\n', k, fSet.frameIdx);
            
            cellXSegmenter = CellXSegmenter(config, fSet);
            
            try
                cellXSegmenter.run();
            catch exc
                ret = CellXExceptionHandler.handleException(exc);
                if( isdeployed() )
                    exit(ret);
                end
                return;
            end
            
            segmentedCells = cellXSegmenter.getDetectedCells();
            
            fprintf('Detected %d cell(s) on frame %d (file set %d)\n', numel(segmentedCells), fSet.frameIdx, k);
            
            cellXIntensityExtractor = CellXIntensityExtractor(...
                config, ...
                fSet, ...
                segmentedCells);
            cellXIntensityExtractor.run();
            
            % save mat files
            CellXResultWriter.writeMatSegmentationResults(...
                fSet.getSeedsMatFileName(), ...
                segmentedCells...
                );
            
            CellXResultWriter.writeSegmentationMask(...
                fSet.getMaskMatFileName(), ...
                segmentedCells, ...
                cellXSegmenter.getInputImageDimension()...
                );
            
            % write TXT result of the current segmentation
            CellXResultWriter.writeTxtSegmentationResults(...
                fSet, ...
                segmentedCells, ...
                config);
            
            CellXResultWriter.writeSegmentationControlImageWithIndices(...
                fSet.oofImage, ...
                fSet.controlImageFile, ...
                segmentedCells, ...
                config...
                );
            CellXResultWriter.writeSeedingControlImage( ...
                fSet.oofImage, ...
                fSet.seedingImageFile, ...
                cellXSegmenter.seeds, ...
                config...
                );
            clear segmentedCells  cellXSegmenter  cellXIntensityExtractor
        end
        
        if(fileHandler.doTracking && isempty(cmdParser.setIndex))
            tracker = CellXTracker(config, fileHandler);
            tracker.run();
            
            % save the track data
            CellXResultWriter.writeMatTrackerData(...
                fileHandler.trackingMatResultFile, ...
                tracker.getTrackData())
            
            % write the track results
            CellXResultWriter.writeTxtTrackResults( ...
                fileHandler,tracker.getTrackData(), ...
                config);
            
            % create lineage plot
             CellXResultWriter.writeLineageImage(...
                 fileHandler.lineageControlImage, ...
                 tracker.trackingInfoMatrix);
        else
            % write the track results
            CellXResultWriter.writeTxtTrackResults( ...
                fileHandler, [], ...
                config);          
        end
        ret = 0;
    end


% Process a file set
    function ret = runMode2(config, cmdParser)
        
        if( cmdParser.isCalibrationMode() )
            % setup the config
            config.setCalibrationMode(cmdParser.testExit+1);
            if(cmdParser.testExit==1)
                config.check();
            end
            disp(config);
            
            % setup the file set
            fileSet = CellXFileSet(cmdParser.frameNumber, cmdParser.segImage);
            if( length(cmdParser.fluoImages)>0 )
                if( length(cmdParser.ffImages)>0 )
                    fileSet.addFluoImage(cmdParser.fluoImages{1}, cmdParser.ffImages{1});
                else
                    fileSet.addFluoImage(cmdParser.fluoImages{1});
                end
            end
            disp(fileSet);
            
            % run the segmenter
            cellXSegmenter = CellXSegmenter(config, fileSet);
            try
                cellXSegmenter.run();
            catch exc
                ret = CellXExceptionHandler.handleException(exc);
                return;
            end
            
            % write the files
            
            if(config.calibrationMode==1)
                CellXResultWriter.writeHoughTransformControlImage(...
                    cmdParser.segImage, ...
                    cmdParser.controlImage1, ...
                    cellXSegmenter.seeds, config);
                fprintf('Wrote hough transform control image ''%s'' \n', cmdParser.controlImage1);
            elseif( config.calibrationMode==2 )
                CellXResultWriter.writeSegmentationControlImage(...
                    cmdParser.segImage, ...
                    cmdParser.controlImage1, ...
                    cellXSegmenter.getDetectedCells(), config);
                fprintf('Wrote segmentation control image ''%s'' \n', cmdParser.controlImage1);
                
                if( length(cmdParser.fluoImages)>0 )
                    cellXIntensityExtractor = CellXIntensityExtractor(...
                        config, ...
                        fileSet, ...
                        cellXSegmenter.getDetectedCells());
                    
                    cellXIntensityExtractor.run();
                    
                    CellXResultWriter.writeFluoControlImage(...
                        cmdParser.fluoImages{1}, ...
                        cmdParser.controlImage2, ...
                        cellXSegmenter.getDetectedCells(), ...
                        config);
                    fprintf('Wrote fluorescence control image ''%s'' \n', cmdParser.controlImage2);
                end              
            end
        else
            % regular analysis of files
            fSet = CellXFileSet(cmdParser.frameNumber, cmdParser.segImage);
            fSet.setResultsDirectory( cmdParser.resultDir );
            for k=1:numel(cmdParser.fluoImages)
                if( numel(cmdParser.ffImages)>=k )
                    fSet.addFluoImage(cmdParser.fluoImages{k}, cmdParser.ffImages{k});
                else
                    fSet.addFluoImage(cmdParser.fluoImages{k});
                end
            end     
            % run the segmenter
            cellXSegmenter = CellXSegmenter(config, fSet);
            try
                cellXSegmenter.run();
            catch exc
                ret = CellXExceptionHandler.handleException(exc);
                return;
            end
            
            segmentedCells = cellXSegmenter.getDetectedCells();
            
            cellXIntensityExtractor = CellXIntensityExtractor(...
                config, ...
                fSet, ...
                segmentedCells);
            cellXIntensityExtractor.run();
            
            % save mat files
            CellXResultWriter.writeMatSegmentationResults(...
                fSet.getSeedsMatFileName(), ...
                segmentedCells...
                );
            
            CellXResultWriter.writeSegmentationMask(...
                fSet.getMaskMatFileName(), ...
                segmentedCells, ...
                cellXSegmenter.getInputImageDimension()...
                );
            
            % write TXT result of the current segmentation
            CellXResultWriter.writeTxtSegmentationResults(...
                fSet, ...
                segmentedCells, ...
                config);
            
            CellXResultWriter.writeSegmentationControlImageWithIndices(...
                fSet.oofImage, ...
                fSet.controlImageFile, ...
                segmentedCells, ...
                config...
                );
            CellXResultWriter.writeSeedingControlImage( ...
                fSet.oofImage, ...
                fSet.seedingImageFile, ...
                cellXSegmenter.seeds, ...
                config...
                );  
        end
        ret=0;
    end

% Perform the tracking analysis only
    function ret = runMode3(config)
        error('Not yet implemented');
    end


end