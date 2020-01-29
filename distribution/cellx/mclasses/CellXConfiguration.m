classdef CellXConfiguration < handle
    %CELLXCONFIGURATION Encapsulates the parameters for CellX
    
    properties(Constant)
        version = 1;
        release = 10;
    end
    
    
    properties (GetAccess=public, SetAccess=private)
        
        % maximum cell length [pixel]
        % (CellXSegmenter, CellXMembraneDetector, ...)
        %
        % general param
        maximumCellLength = [];
        
        % number of intensity classes (i.e. the number of gaussians to be
        % fitted to the histogram)
        % can be an interval, e.g [1,3]
        % (CellXSegmenter)
        intensityClassesCount = [3];
        
        % analyse crop region of image only [xbegin, ybegin, xend, yend]
        % leave empty for the complete image
        % (CellXSegmenter, CellXIntensityExtractor, CellXTracker)
        % general param
        cropRegionBoundary = []; % [pixel coordinates, upper-left=(row=1,col=1)]
       
        % wiener filter params
        % (CellXSegmenter, CellXIntensityExtractor)
        wiener2params = [3 3]; % [pixels]
        
        
        % compute the Hough transform 
        % 0 => on the original (normalized to 0-1) image
        % 1 => on the CLAHE image
        % (CellXSegmenter)
        % adv param
        isHoughTransformOnCLAHE = 1; % [bool]
               
        
        %
        %
        %  
        maximumNumberOfCentroids = 3000;              
        
        % compute the graph-cut 
        % 0 => on the original (normalized to 0-1) image
        % 1 => on the CLAHE image
        % (CellXSegmenter, CellXMembraneDetector)
        % adv param
        isGraphCutOnCLAHE = 1; % [bool]
                       
        
        % the membrane profile (gray values) as is is seen along a ray 
        % that is perpendicular to the membrane, 
        % starts inside of the cell,
        % and ends at the outside. 
        % (CellXMembraneDetector)
        % general param
        membraneIntensityProfile = []; % [vector of gray levels]
         
        % location of membrane in the signal 
        % (CellXMembraneDetector)
        % general param
        membraneLocation; % [pixels]
        
        % membrane width 
        % (CellXMembraneDetector)
        % general param
        membraneWidth; % [pixels]
        
        % minimum gradient between two pixels to trigger circular hough transform
        % (CellXSegmenter)        
        houghTransformGradientThreshold = 0.01; % [gray-level-difference]
        
        % minimum and maximum radius (pixels) of the circular seeds
        % in the hough transform
        % (CellXSegmenter, CellXMembraneDetector)
        % general param
        seedRadiusLimit = []; % [pixels]
        
        % percentage of the maximum value of the accumulation image 
        % value for beeing a seed 
        % (CellXSegmenter)
        % adv param
        seedSensitivity = 0.2; % [fraction]

        % fraction of the radius of a seed.
        % pixels within this radius are connected to the source vertex
        % in the graph for the max-flow.
        % (CellXMembraneDetector)
        seedMaskRadiusFraction = 0.5; % [fraction]
        
        % the fraction of pixels of the maximum cell length that define the
        % width of the border vertices that are connected to the sink 
        % vertex in the max-flow graph
        % (CellXMembraneDetector)
        pixelBorderFractionOfMaxCellLength = 0.05; % [fraction] 
        
        % vertex connectivity (4 or 8);
        % (CellXMembraneDetector)
        maxFlowGridConnectivity = 8; % [vertex degree]
        

        % membrane erosion shape
        % this shape is created when the membrane width is set.
        % (CellXMembraneDetector, CellXIntersectionResolver)
        membraneErosionShape; % [disk shape]
        
        % detection of low energy membrane regions and refinement
        % 0 => disabled
        % 1 => enabled
        % (CellXMembraneDetector)
        isNonConvexRegionDetectionEnabled = 1; % [bool]
        
        % the minimum length of low energy membrane pixels to compute
        % a new ray convolution from the center of that region 
        % (smoothing if any is applied first, see next param).
        % (CellXMembraneDetector)
        minimumLengthOfLowEnergyMembraneRegion = 5; % [pixels]
        
        
        % two weak regions of membrane border pixels are connected
        % if their distance is at most maximumSmoothingDistance
        % (CellXMembraneDetector)
        maximumSmoothingDistance = 2; % [pixels]
        
        % the refined max-flow labeling 
        % (using the ray convolutions of centers of weak membrane regions) 
        % is used only if the difference between the resulting and the 
        % initial labeling (wrt the initial labeling) is less than the 
        % following fraction threshold. 
        % (CellXMembraneDetector)
        maximumExpansionFraction = 0.35; % [fraction]
        
                 
        % the required fraction of rays with a maximum >
        % getRayConvolutionValueThreshold() to remain a valid seed.
        % (CellXMembraneDetector)                
        requiredFractionOfGoodRays = 0.5; % [fraction]
        
        % the required fraction of the membrane signal correlation value
        % that a convolution ray requires to be a 'good' ray
        % (CellXMembraneDetector)
        requiredCorrelationFraction = 0.25; % [fraction]

        
        % a reference value that represents a good correlation value
        % determined when the membraneProfile is set.
        % it is not recommended to set this manually
        % (CellXMembraneDetector)
        membraneReferenceCorrelationValue; % [value]
        
        
        % 
        % debugLevel==3 -> show debug images in CellXMembraneDetector
        %
        %
        debugLevel = 0;
        
        
        % number of classes in a mixture model for low, medium, and high 
        % convolution pixels values on the membrane
        % (CellXValidator)
        numberOfRayIntensityClasses = 3; % [positive integer]
        

        % Threshold that determines which
        % membrane pixels are 'good'/accepted
        % given as a fraction wrt the membraneReferenceCorrelationValue
        % (CellXValidator)
        % 0.3 < value < 0.8
        membraneConvolutionThresholdFraction = 0.4; % [fraction]
        
        % if the fraction of 'good' membrane pixels
        % is less than this fraction, the seed will be invalidated.
        % (CellXValidator)
        % adv param
        requiredFractionOfAcceptedMembranePixels = 0.85; % [fraction]
        
        % the minor axis of the detected region should not differ too much
        % from the hough transform radius - if it does, the seed is
        % probably the center of a space in a cell cluster and should be
        % removed.
        % (CellXValidator)
        % adv param
        maximumMinorAxisLengthHoughRadiusRatio = 1.7; % [ratio]
        
        % filter out seeds which do not have the required distance to
        % the image boundary 
        % (CellXValidator)
        requiredDistanceToImageBoundary = 1; % [pixels]
        
        % merge two seeds if the overlapping area of one wrt 
        % to its total area is above overlapMergeThreshold
        % (CellXIntersectionResolver)
        % adv param
        overlapMergeThreshold   = 0.8;  % [fraction]
        
        % if two cells overlap up to overlapResolveThreshold, 
        % distribute the pixels to the seed that is closer wrt its centroid.
        % (CellXIntersectionResolver)
        % adv param
        overlapResolveThreshold = 0.2; % [fraction]
        

        % fraction of nuclear volume to cell volume 
        % adv param
        nuclearVolumeFraction = 0.07; 

        
        % percentage of bright area 
        % fluo pixel values above this threshold are counted for the bright
        % area
        % adv param
        intensityBrightAreaPercentage = 0.3; 
        
        % The number of pixels we allow the  
        % fluo-image displacement (in pixels) 
        % such that the total intensity of the 
        % cells is maximized
        %  0 <= value < 20
        % display name: MaxFluoImageDisplacement
        fluoAlignPixelMove = 5;
        
        % The maximum number of pixels that 
        % the center of a cell can move between
        % 2 frames
        % 0 < value < min(ImHeight,ImWidth)/2
        % display name: MaxCellCenterMove
        spatialMotionFactor = 50;
        
        % maximum number of intermediate
        % frames that a cell may be recovered
        % 0 < value < 10
        % display name: MaxFrameNumberForRecovery
        maxGapConnectionDistance =3;
        
        % for calibration with the gui, we need to exit early at two positions:
        % 0 : normal mode, 
        % 1 : exit after hough transform
        % 2 : exit after segmentation+intensity extraction
        calibrationMode=0;
 
        
        %CLAHE parameters 
        % adv param
        claheClipLimit = 0.01; % [0,1] 0 results in the original image  
        
        % width and height of one tile [pixels]
        % to convert this for the adapthisteq function of matlab
        % divide the width and height of the image by this value and round
        % to integer
        % adv param
        claheBlockSize = 100;
        
        
    end

    
    methods
        
        function check(this)
            %check for matlab>2009, assignment of ~
            
            if verLessThan('matlab', '7.9')
                error('CellX requires MATLAB 7.9 or higher.\nPlease use the deployed version and the MATLAB Compiler Runtime (MCR) if your MATALB version is too old');
            end

            
            % check defined
            if( isempty(this.maximumCellLength) )
                error('Undefined value for maximum cell length\n');
            end
            
            if( isempty(this.membraneWidth) )
                error('Undefined value for membrane width\n');
            end
            
            if( isempty(this.membraneLocation) )
                error('Undefined value for membrane location\n');
            end
            
            if( isempty(this.membraneIntensityProfile) )
                error('Undefined value for membrane intensity profile\n');
            end
            
            if( isempty(this.seedRadiusLimit) )
                error('Undefined value for membrane intensity profile\n');
            end
            
            % check ranges
            
            % TODO: ensure not negative 
                    
            if( numel(this.membraneIntensityProfile)<3 )
                error('The membrane intensity profile must contain at least three elements');
            end
                      
            if( this.maximumCellLength<10 || this.maximumCellLength< 2*numel(this.membraneIntensityProfile) )
                error('Invalid value for maximum cell length. Should be at least 10px or two times the length of the membrane intensity profil');
            end
            
            if( this.seedRadiusLimit(1)<3 )
                error('Lower seed radius limit (%d) should not be less than 5 pixels.', this.seedRadiusLimit(1));
            end
            
            if( this.seedRadiusLimit(2)>=this.maximumCellLength)
                error('Upper seed radius limit (%d) cannot be more than the maximum cell length (%d)', this.seedRadiusLimit(2), this.maximumCellLength);
            end
                        
            if( this.membraneWidth<1 )
                 error('Membrane width (%d) cannot be less than 1 pixel', this.membraneWidth);
            end

            if( this.membraneLocation<1 || this.membraneLocation> numel(this.membraneIntensityProfile) )
                 error('Membrane location (%d) must be within (%d,%d)', ...
                     this.membraneLocation, 1, numel(this.membraneIntensityProfile) );
            end
         
            maxWidth = numel(this.membraneIntensityProfile) - this.membraneLocation + 1;
            if( this.membraneWidth > maxWidth )
                 error('Membrane width (%d) cannot be more than %d pixel', this.membraneWidth, maxWidth);
            end

        end
        
        %
        % boolean methods
        %
        
        function ret = isCropRegionDefined(this)
           ret = ~isempty(this.cropRegionBoundary); 
        end
       
       
        
        function ret = requiresCLAHE(this)
            ret = (this.isHoughTransformOnCLAHE+this.isGraphCutOnCLAHE)>0;
        end
        
        
        %
        % end of boolean methods
        %
        
        
        
        %
        % setters 
        %
             
        function setSeedSensitivity(this, value)
            this.seedSensitivity = value;
        end
        
        function setFluoAlignPixelMove(this, value)
            this.fluoAlignPixelMove = value;
        end
        
        function setCropRegion(this, xbegin, ybegin, width, height)
            this.cropRegionBoundary = [xbegin ybegin width height];
        end
        
        function clearCropRegion(this)
            this.cropRegionBoundary = [];
        end
        
        function setSeedRadiusLimit(this, limit)
            this.seedRadiusLimit = limit;
        end  
             
        function setMembraneIntensityProfile(this, profile) 
            % normalize the dynamic range of the profile to [0,1]
            minp = min(profile);
            normalizedProfile = (profile-minp)/(max(profile)-minp);
            meanProfileValue = sum(normalizedProfile)/numel(normalizedProfile);
            this.membraneIntensityProfile = normalizedProfile - meanProfileValue;
            this.membraneReferenceCorrelationValue = this.membraneIntensityProfile*(profile-mean(profile))';
        end 
        
        function setMembraneLocation(this, loc)
            this.membraneLocation = loc;
        end
        
        function setMembraneWidth(this, w)
            if( w<1 )
                error('Invalid value for membrane width (membraneWidth): %d  (min=%d)\n', w, 1);
            end          
            this.membraneWidth = w;
            this.membraneErosionShape = strel('disk', w);
        end
        
        function setMaximumCellLength(this, value)
            this.maximumCellLength= value;
        end
        
        function setDebugLevel(this, value)
            this.debugLevel = value;
        end
        
        function setHoughTransformOnCLAHEImage(this, value)
            this.isHoughTransformOnCLAHE = (value>0);
        end
        
        function setGraphCutOnCLAHEImage(this, value)
            this.isGraphCutOnCLAHE = (value>0);
        end
        
        function setFluoImages(this, fluofilenamesCell)
                this.fluoFilenames = fluofilenamesCell;
        end
        
        function setFluoTypes(this, fluotypesCell)
                 this.fluotypes = fluotypesCell;
        end
        
        function setFlatFieldImages(this, flatfieldCell)
                 this.flatfieldFilenames = flatfieldCell;
        end
   
        
        
        function setMaximumMinorAxisLengthHoughRadiusRatio(this, value)
            this.maximumMinorAxisLengthHoughRadiusRatio = value;
        end
        
        function setOverlapResolveThreshold(this,value)
            this.overlapResolveThreshold= value;
        end
        
        
        function setCalibrationMode(this,v)
            this.calibrationMode =v;
        end
        
        %
        % end of setters
        %
        
        
        
        %
        % getters, required for objects that are generated on request or
        % if the requested value can be derived from internal variables 
        %
        
        
        % window size for finding local maxima in Hough Transform
        function ret = getAccumulationSmoothingWindowSize(this)
            ret =  round((sum(this.seedRadiusLimit)/2));
        end
                
        % smoothing filter for accumulation image
        function ret = getHoughTransformAccumulationArraySmoothingFilter(this)
            ret = fspecial('gaussian', max(3, ceil(this.seedRadiusLimit(1)/2)) , max(1, this.seedRadiusLimit(1)/3));
        end
        
        
        % grid graph vertices per column (and row)
        function ret = getVertexDimension(this)
            ret = (2*this.maximumCellLength+1);
        end
        
        function ret = getMaxFlowGraphBorderVertexCount(this)
            ret = max(1, ceil(this.maximumCellLength*this.pixelBorderFractionOfMaxCellLength));
        end
        
        function ret = getRayConvolutionValueThreshold(this)
            ret = this.requiredCorrelationFraction * this.membraneReferenceCorrelationValue;
        end
        
        
        %
        % end of getters
        %
        
        
        %
        % I/O
        %
        
        
        function toXML(this, file)
            doc = com.mathworks.xml.XMLUtils.createDocument('CellXConfiguration');
            root = doc.getDocumentElement;
            root.setAttribute('timestamp',datestr(now));
            tagName = 'CellXParam';
            arrayElemTagName = 'ArrayElement';
            nameAttrName = 'name';
            valueAttrName = 'value';
            charAttrName = 'string';
            
            props = properties(CellXConfiguration);
            n = numel(props);
            for i=1:n
                id = props{i};
                if( strcmp(id, 'membraneErosionShape') );
                    continue;
                end
                element = doc.createElement(tagName);
                element.setAttribute(nameAttrName, id);
                fn = numel(this.(id));
                if(fn==1 || ischar(this.(id)) )
                    if( isnumeric(this.(id)) )
                        if(this.(id) ==round(this.(id)))
                            value = num2str(this.(id), '%d');
                        else
                            value = num2str(this.(id), '%f');
                        end
                        element.setAttribute(valueAttrName, value);
                    elseif(islogical(this.(id)))
                        if( this.(id) )
                            element.setAttribute(valueAttrName, '1');
                        else
                            element.setAttribute(valueAttrName, '0');
                        end
                    else
                        element.setAttribute(charAttrName, this.(id));
                    end
                else
                    for j=1:fn
                        childElement = doc.createElement(arrayElemTagName);
                        x = this.(id)(j);
                        if( numel(x)~=1 )
                            error('Cannot handle property ''%s'' inXML export\n', id);
                        end
                        if( isnumeric(x) )
                            if( x==round(x) )
                                value = num2str(x, '%d');
                            else
                                value = num2str(x, '%f');
                            end
                            childElement.setAttribute(valueAttrName, value);
                        else
                            childElement.setAttribute(charAttrName, x);
                        end
                        element.appendChild(childElement);
                    end
                end
                root.appendChild(element);
            end
            xmlwrite(file, doc);
        end

    end
    
    
    methods (Static)    
        function this = readXML(file)
            this = CellXConfiguration();   
            tagName = 'CellXParam';
            arrayElemTagName = 'ArrayElement';
            nameAttrName = 'name';
            valueAttrName = 'value';
            charAttrName = 'string';
            doc = xmlread(file);
            root = doc.getDocumentElement();
            paramNodes = root.getElementsByTagName(tagName);
            n = paramNodes.getLength;
            for i=1:n
                node = paramNodes.item(i-1);
                id = node.getAttribute(nameAttrName);
                hasValue=0;
                value=[];
                if(node.hasChildNodes)
                    arrayElements = node.getElementsByTagName(arrayElemTagName);
                    cn = arrayElements.getLength;
                    value = zeros(1,cn);
                    for j=1:cn
                        cnode = arrayElements.item(j-1);
                        if(cnode.hasAttribute(valueAttrName))
                            value(j) = str2double(cnode.getAttribute(valueAttrName));
                            hasValue=1;
                        end
                    end
                else
                    if(node.hasAttribute(valueAttrName))
                        value = str2double(node.getAttribute(valueAttrName));
                        hasValue=1;
                    elseif(node.hasAttribute(charAttrName))   
                        value = char(node.getAttribute(charAttrName));
                        hasValue=1;
                    end
                end
                if(hasValue)                   
                    if( strcmp(id, 'membraneIntensityProfile') )
                        this.setMembraneIntensityProfile(value);
                    elseif( strcmp(id, 'membraneWidth') )
                        this.setMembraneWidth(value);
                    else
                        this.(char(id)) = value;
                    end           
                end
            end

            
        end
        
        
        
    end
    
end

