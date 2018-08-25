function [outputMatrix,normValsOut] = scale(inputMatrix,normVals)

cols = length(inputMatrix(1,:));
rows = length(inputMatrix(:,1));
outputMatrix = zeros(rows,cols);
normValsOut = zeros(2,cols);

for i=1:cols

        colMax = max(inputMatrix(:,i));
        colMin = min(inputMatrix(:,i));
        
        normValsOut(1,i) = colMax;
        normValsOut(2,i) = colMin;
        
    for j = 1:rows
       
        if(normVals ==  0)
            outputMatrix(j,i) = (inputMatrix(j,i)-colMin)/(colMax-colMin)*2-1;
        else
            outputMatrix(j,i) = (inputMatrix(j,i)- normVals(2,i))/(normVals(1,i)-normVals(2,i))*2-1;
        end
    end
            outputMatrix = outputMatrix;
            %outputMatrix = outputMatrix-1;

            outputMatrix = sparse(outputMatrix);
end