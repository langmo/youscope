classdef CellXFluoFeatures < handle
    % we give values to the following intensity features:
    %             1. total cell intensity
    %             2. 25% quantile of total cell intensity
    %             3. median of total cell intensity
    %             4. 75% quantile of total cell intensity
    %             5. total membrane intensity
    %             6. 25% quantile of total membrane intensity
    %             7. median of total membrane intensity
    %             8. 75% quantile of total membrane intensity
    %             9. total circular nuclear intensity ### fitting the disk stuff
    %             10. 25% quantile of total circular nuclear intensity
    %             11. median of total circular nuclear intensity
    %             12. 75% quantile of total circular nuclear intensity
    %             13. Euler number of 30% fluorescence intensity
    %             14. total "euler" intensity
    %             15. 25% quantile of total "euler"intensity
    %             16. median of total "euler"intensity
    %             17. 75% quantile of total "euler" intensity
    %             18. background mean
    %             19. background std
    
    properties
    
     % seed number
%     seedIndex;
     
     % fluotype   
     fluoTypes;
     
     % whole cell intensity features 
     totalCellIntensity;
     q25TotalCellIntensity;
     medianTotalCellIntensity;
     q75TotalCellIntensity;
     
     % membrane intensity features
     totalMembraneIntensity;
     q25TotalMembraneIntensity;
     medianTotalMembraneIntensity;
     q75TotalMembraneIntensity;
     
     % nuclear intensity features
     totalNuclearIntensity;
     q25TotalNuclearIntensity;
     medianTotalNuclearIntensity;
     q75TotalNuclearIntensity;
     
     % Bright area intensity features
     eulerNumberOfBrightArea;
     totalBrightAreaIntensity;
     q25TotalBrightAreaIntensity;
     medianTotalBrightAreaIntensity;
     q75TotalBrightAreaIntensity;
     
     % background intensity features
     backgroundMeanValue;
     backgroundStdValue;
        
    end
    
    methods
        
        function obj = CellXFluoFeatures()
            
 %           obj.seedIndex = value;
 
        end
        
    end
    
end

