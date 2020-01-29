classdef CellXSegmenterFig < CellXSegmenter
    
    methods
        % call the constructor of the superclass to initialize an instance
        function obj = CellXSegmenterFig(config, fileSet)
            obj@CellXSegmenter(config, fileSet);
        end
    end
    
    % only protected and public methods can be overwritten by subclasses
    methods (Access=protected)
        
        function detectMembranes(this)           
            seedCount = numel(this.seeds);
            for i = 1:seedCount
                fprintf('   Processing seed %d\n', i);
                this.currentSeed = this.seeds(i);
                this.initCurrentCrops();
                this.membraneDetector.run(...
                    this.currentSeed, ...
                    this.currentCropImage, ...
                    this.currentCropGradientPhase, ...
                    i);
                
                
                % plot the seed convolution image
                offScreenFig = figure('visible','off');
                imagesc(this.membraneDetector.convolutionImage)
                colorbar('FontSize',18)
                axis image
                axis off
                hold on
                Xc=this.membraneDetector.seed.cropCenterX;
                Yc=this.membraneDetector.seed.cropCenterY;
                text(Xc,Yc,['\fontsize{10}{\color{black}' 'S' '}' ],...
                    'HorizontalAlignment','center','BackgroundColor',[1 1 1],'EdgeColor','black');
                locF2=this.fileSet.resultsDir;
                fnc = [locF2 'convolutionImage_Seed_' num2str(i)];
                print(offScreenFig,'-depsc','-r300',fnc);         
                       
%                imwrite(this.membraneDetector.convolutionImage, sprintf('test%i.png',i), 'png');
                
                
                if( this.config.debugLevel>1 )
                    disp(this.seeds(i));
                end
            end
            

        end       
    end
    
end

