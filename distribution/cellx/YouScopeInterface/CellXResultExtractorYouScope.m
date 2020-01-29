classdef CellXResultExtractorYouScope < handle
    %CELLXRESULTWRITER
    
    properties (Constant)
        
        segHeader = { ...
            'cell.frame', ...
            'cell.index', ...
            'cell.center.x', ...
            'cell.center.y', ...
            'cell.majoraxis', ...
            'cell.minoraxis', ...
            'cell.orientation', ...
            'cell.area', ...
            'cell.volume', ...
            'cell.perimeter', ...
            'cell.mem.area', ...
            'cell.mem.volume', ...
            'cell.nuc.radius', ...
            'cell.nuc.area'};
        
        segFormat = '%d\t%d\t%d\t%d\t%d\t%d\t%+5.3f\t%d\t%d\t%d\t%d\t%d\t%d\t%d';
        
        fluoSuffixesHeader = {...
            '.background.mean',...
            '.background.std',...
            '.cell.total',...
            '.cell.q75',...
            '.cell.median',...
            '.cell.q25',...
            '.mem.total',...
            '.mem.q75',...
            '.mem.median',...
            '.mem.q25',...
            '.nuc.total',...
            '.nuc.q75',...
            '.nuc.median',...
            '.nuc.q25',...
            '.bright.total',...
            '.bright.q75',...
            '.bright.median',...
            '.bright.q25',...
            '.bright.euler'};
        
        fluoFormat = '%d\t%d\t%d\t%d\t%d\t%d\t%d\t%d\t%d\t%d\t%d\t%d\t%d\t%d\t%d\t%d\t%d\t%d\t%d';
       

        trackHeader = {...
            'track.index',...          % the index of this track
            };

        trackFormat = '%d';
        
        
    end
    
    
    methods (Static)
        % writes an R compatible header line and table with
        % 14 fields per line for each seed/cell
        function ret = extractSegmentationResults(fSet, seeds, config, trackerData)
            
            ret = struct('data', [], 'headers', []);
            
            % first check how many fluo types we have and append
            % their tags into the fluoSuffixesHeader
            % extent also the fluoFormat
            nrOfFluoTags = numel(fSet.fluoTags);
            jointFluoHeader = [];
            for ft=1:nrOfFluoTags
                % take the current fluoTag
                currentFluoTag =fSet.fluoTags{ft};
                % build the new fluoHeader
                currentFluoHeader = cell(1,numel(CellXResultExtractorYouScope.fluoSuffixesHeader));
                for fp = 1:numel(CellXResultExtractorYouScope.fluoSuffixesHeader)
                    currentFluoHeader{fp} =[currentFluoTag,...
                        CellXResultExtractorYouScope.fluoSuffixesHeader{fp}];
                end
                jointFluoHeader =[jointFluoHeader,currentFluoHeader];
            end
            % build the joint header
            jointHeader = [CellXResultExtractorYouScope.segHeader,...
                           jointFluoHeader,...
                           CellXResultExtractorYouScope.trackHeader];
                       
            nrCols = numel(jointHeader);

            % initialize the output
            n = length(seeds);
            retdata = zeros(n,nrCols); 
            
            for i = 1:n
                s = seeds(i);
                segCols =[...
                    fSet.frameIdx, ...
                    i, ...
                    round(s.centroid(1)), ...
                    round(s.centroid(2)), ...
                    round(s.majorAxisLength), ...
                    round(s.minorAxisLength), ...
                    s.orientation, ...
                    round(s.getCellArea()), ...
                    round(s.cellVolume), ...
                    round(s.perimeter), ...
                    round(s.getMembraneArea()), ...
                    round(s.cellVolume - s.cytosolVolume), ...
                    round(s.getNucleusRadius(config)), ...
                    round(s.getNucleusArea(config))];
                
                fluoCols = CellXResultWriter.generateFluoCols(s, fSet);
                
                trackCols =  trackerData(i,1);
                
                cellRow = [segCols,fluoCols,trackCols];
                
                retdata(i,:) = cellRow;
            end
            
          ret.data = retdata;
          ret.headers = jointHeader;
            
        end
        
        
        
        
        function fluoCols = generateFluoCols(s, fSet)
            nrOfFluoTags = numel(fSet.fluoTags);
            fluoCols = [];
            for nrf=1:nrOfFluoTags
                
                fluoFeature = s.fluoFeatureList(nrf);
                
                currentFluoCols = [...
                    round(fluoFeature.backgroundMeanValue), ...
                    round(fluoFeature.backgroundStdValue), ...
                    round(fluoFeature.totalCellIntensity), ...
                    round(fluoFeature.q75TotalCellIntensity), ...
                    round(fluoFeature.medianTotalCellIntensity), ...
                    round(fluoFeature.q25TotalCellIntensity),...
                    round(fluoFeature.totalMembraneIntensity), ...
                    round(fluoFeature.q75TotalMembraneIntensity), ...
                    round(fluoFeature.medianTotalMembraneIntensity), ...
                    round(fluoFeature.q25TotalMembraneIntensity),...
                    round(fluoFeature.totalNuclearIntensity), ...
                    round(fluoFeature.q75TotalNuclearIntensity), ...
                    round(fluoFeature.medianTotalNuclearIntensity), ...
                    round(fluoFeature.q25TotalNuclearIntensity),...
                    round(fluoFeature.totalBrightAreaIntensity), ...
                    round(fluoFeature.q75TotalBrightAreaIntensity), ...
                    round(fluoFeature.medianTotalBrightAreaIntensity), ...
                    round(fluoFeature.q25TotalBrightAreaIntensity),...
                    round(fluoFeature.eulerNumberOfBrightArea)
                    ];
                fluoCols=[fluoCols,currentFluoCols];
            end
        end
        
        function writeMatSegmentationResults(filename, cells)
            save(filename, 'cells');
            fprintf('Wrote %s\n', filename);
        end
        
        
        % saves the recovered cells that were created by the tracker
        % recoveredCells is a structure with fields:
        %   seeds, an array of seeds
        %   frame, an array that holds the frame of seeds(i) at position i
        %   index, an array that holds the index of the cell wrt the other
        %          cells on that frame
        %
        %   tracks, a matrix with the track info
        %   tracksHeader, the column names of the tracks matrix
        function writeMatTrackerData(filename, trackerData)
            save(filename, 'trackerData');
            fprintf('Wrote %s\n', filename);
        end
        
        
        % the ASCII table with all information over
        function writeTxtTrackResults(filehandler, trackerData, config)
            
            colDesc = CellXResultWriter.generateBaseHeader( filehandler.fileSets(1) );
            
            if( ~isempty(trackerData) )
                th = CellXResultWriter.concatenate(CellXResultWriter.trackHeader, sprintf('\t'));
                colDesc.header = [colDesc.header sprintf('\t') th sprintf('\n')];
                colDesc.format = [colDesc.format sprintf('\t') CellXResultWriter.trackFormat sprintf('\n')];
            else
                colDesc.header = [colDesc.header sprintf('\n')];
                colDesc.format = [colDesc.format sprintf('\n')];
            end
            
            % open the file
            filename = filehandler.trackingTxtResultFile;
            if( isempty(trackerData) )
                 filename = filehandler.seriesTxtResultFile;
            end
            out = fopen(filename , 'w');     
            fprintf(out, '%s', colDesc.header);
        
            % iterate over file sets
            for ntf=1:numel(filehandler.fileSets)
                fSet = filehandler.fileSets(ntf);
                d = load(fSet.seedsMatFile);
                seedVarName = fieldnames(d);
                seeds = d.(genvarname(seedVarName{1}));
                
                % append seeds if there are recovered
                % cells in the frame
                if( ~isempty(trackerData) )
                    recIndx = ismember( trackerData.recoveredSeedsFrameIdx, fSet.frameIdx );
                    if any(recIndx)
                        frameRecoveredSeeds = trackerData.recoveredSeeds(recIndx);
                        frameRecoveredSeedsSegIdxs = ...
                            trackerData.recoveredSeedsSegmentationIdx(recIndx);
                        [~, ix]  = sort(frameRecoveredSeedsSegIdxs);
                        frameRecoveredSeeds = frameRecoveredSeeds(ix);
                        seeds =[seeds frameRecoveredSeeds];
                    end
                end
                
                % loop through the frame-seeds
                n = length(seeds);
                for i = 1:n
                    s = seeds(i);
                    segCols =[...
                        fSet.frameIdx, ...
                        i, ...
                        round(s.centroid(1)), ...
                        round(s.centroid(2)), ...
                        round(s.majorAxisLength), ...
                        round(s.minorAxisLength), ...
                        s.orientation, ...
                        round(s.getCellArea()), ...
                        round(s.cellVolume), ...
                        round(s.perimeter), ...
                        round(s.getMembraneArea()), ...
                        round(s.cellVolume - s.cytosolVolume), ...
                        round(s.getNucleusRadius(config)), ...
                        round(s.getNucleusArea(config))];
                    
                    fluoCols = CellXResultWriter.generateFluoCols(s, fSet);
                    
                    trackCols=[];
                    if( ~isempty(trackerData) )
                        % find the cells in the tracks
                        trackDataIndx = ismember(trackerData.tracks(:,2:3),...
                            [i, fSet.frameIdx],'rows');
                        
                        if any(trackDataIndx)
                            trackCols=trackerData.tracks(trackDataIndx,4:9);
                        else % find it in the recovery matrix
                            
                        end
                    end
                    
                    cellRow = [segCols, fluoCols, trackCols];
                    fprintf(out, colDesc.format, cellRow);                   
                end
            end
            fprintf(out,'\n');                      
            fclose(out);
            fprintf('Wrote %s\n', filename);
        end
        
        
        % Generates the base header and format, i.e. 
        % the header for the columns with info of the
        % CellXSegmenter and
        % CellXIntensityExtractor
        % The number of columns varies depending on the number of
        % quantification images.
        function base = generateBaseHeader(fSet)
            % create fluo header and column format
            nrOfFluoTags    = numel(fSet.fluoTags);
            jointFluoHeader = [];
            jointFluoFormat = [];
            for ft=1:nrOfFluoTags
                currentFluoTag =fSet.fluoTags{ft};
                currentFluoHeader = cell(1,numel(CellXResultWriter.fluoSuffixesHeader));
                for fp = 1:numel(CellXResultWriter.fluoSuffixesHeader)
                    currentFluoHeader{fp} =[currentFluoTag,...
                        CellXResultWriter.fluoSuffixesHeader{fp}];
                end
                th = CellXResultWriter.concatenate(currentFluoHeader, sprintf('\t'));
                jointFluoHeader =[jointFluoHeader sprintf('\t') th];
                jointFluoFormat = [jointFluoFormat sprintf('\t') CellXResultWriter.fluoFormat];

            end
            th = CellXResultWriter.concatenate(CellXResultWriter.segHeader, sprintf('\t'));
            base.header = [th jointFluoHeader];
            base.format = [CellXResultWriter.segFormat jointFluoFormat]; 
        end
        
        
        
        % Creates an image with the input image dimension
        % All valid cell pixels are set to the segmentation index of the
        % cell (>=1)
        % other pixels are 0
        function ret = takeSegmentationMask(cells, dim)
            segmentationMask = zeros(dim);
            for nrc = 1:length(cells)
                segmentationMask(cells(nrc).cellPixelListLindx) = nrc;
            end
            ret =  segmentationMask;
 %           save(filename,'segmentationMask');
 %           fprintf('Wrote %s\n', filename);
        end
        
        
        function ret = replicateFormat(forOneCol, nCols)
            ret = '';
            for i=1:nCols
                ret = [ret forOneCol];
            end
        end
        
        
        % concatenates an array of strings putting 'separator' between the
        % strings (separator is not appended to the last string)
        function ret = concatenate(array, separator)
            ret = '';
            n = length(array);
            for i=1:n
                ret = [ret char(array(i))];
                if( i<n )
                    ret = [ret separator];
                end
            end
        end
        
       function writeInitialInputImage(srcImgFile, dstImgFile, seeds, config)
            img = CellXImageIO.loadToGrayScaleImage(srcImgFile, config.cropRegionBoundary);
            img = CellXImageIO.normalize( img );
            %img = CellXImgGen.generateHoughTransformImage(seeds, img);
            imwrite(img, dstImgFile, 'png');
            fprintf('Wrote %s\n', dstImgFile);
        end
        
        
        function writeHoughTransformControlImage(srcImgFile, dstImgFile, seeds, config)
            img = CellXImageIO.loadToGrayScaleImage(srcImgFile, config.cropRegionBoundary);
            img = CellXImageIO.normalize( img );
            img = CellXImgGen.generateHoughTransformImage(seeds, img);
            imwrite(img, dstImgFile, 'png');
            fprintf('Wrote %s\n', dstImgFile);
        end
        
        function writeSegmentationControlImage(srcImgFile, dstImgFile, seeds, config)
            img = CellXImageIO.loadToGrayScaleImage(srcImgFile, config.cropRegionBoundary);
            img = CellXImageIO.normalize( img );
            img = CellXImgGen.generateControlImage(seeds, img);
            imwrite(img, dstImgFile, 'png');
            fprintf('Wrote %s\n', dstImgFile);
        end
        
        function writeSegmentationControlImageWithIndices(srcImgFile, dstImgFile, seeds, config)
            img = CellXImageIO.loadToGrayScaleImage(srcImgFile, config.cropRegionBoundary);
            img = CellXImageIO.normalize( img );
            CellXImgGen.createControlImageWithCellIndices(seeds, img,  dstImgFile);
        end
        
        function writeSeedingControlImage(srcImgFile, dstImgFile, seeds, config)
            img = CellXImageIO.loadToGrayScaleImage(srcImgFile, config.cropRegionBoundary);
            img = CellXImageIO.normalize( img );
            img = CellXImgGen.generateHoughTransformImage(seeds, img);
            imwrite(img, dstImgFile, 'png');
            fprintf('Wrote %s\n', dstImgFile);
        end
        
        function writeSegmentationControlImageWithIndicesB(srcImgFile, dstImgFile, seeds, config)
            img = CellXImageIO.loadToGrayScaleImage(srcImgFile, config.cropRegionBoundary);
            img = CellXImageIO.normalize( img );
            CellXImgGen.createControlImageWithCellIndicesB(seeds, img,  dstImgFile);
        end
        
        
        function writeFluoControlImage(srcImgFile, dstImgFile, seeds, config)
            img = CellXImageIO.loadToGrayScaleImage(srcImgFile, config.cropRegionBoundary);
            img = CellXImageIO.normalize( img );
            img = CellXImgGen.generateIntensityExtractionControlImage( ...
                seeds, ...
                img ...
                );
            imwrite(img, dstImgFile, 'png');
            fprintf('Wrote %s\n', dstImgFile);
        end
        
        
        function writeLineageImage(dstImgFile, trackInfoMatrix)
            CellXImgGen.generateLineageTree(trackInfoMatrix, dstImgFile);
            fprintf('Wrote %s\n', dstImgFile);
        end
        
        function writeLineageTree(dstImgFile, trackInfoMatrix)
            CellXLineageGen.generateLineageTree(trackInfoMatrix, dstImgFile);
            fprintf('Wrote %s\n', dstImgFile);
        end
        
        function ret = writeLineageComparisonTree(dstImgFile, trackInfoMatrixControl,trackInfoMatrixSegm)
            ret = CellXLineageGen.generateLineageComparisonTree(trackInfoMatrixControl,trackInfoMatrixSegm, dstImgFile);
            fprintf('Wrote %s\n', dstImgFile);
        end
        
    end
    
    
    
end

