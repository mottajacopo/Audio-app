function testing2(model,normValsOut)

%     folder = 'frase3';
% 
%     for i=1:1:10    
%     file = strcat('MJ',int2str(i),'.txt');
%     read_predict (model , file , folder , 299 ,normValsOut);
%     end
%     
%     for i=1:1:10 
%     file = strcat('MB',int2str(i),'.txt');
%     read_predict (model , file , folder , 299,normValsOut);
%     end
%     
%     for i=1:1:10   
%     file = strcat('MT',int2str(i),'.txt');
%     read_predict (model , file , folder , 299,normValsOut);
%     end
%     
%     for i=1:1:10
%     file = strcat('CC',int2str(i),'.txt');
%     read_predict (model , file , folder , 299,normValsOut);
%     end
%     
%     folder = 'frase6';
%     for i=1:1:20 
%     file = strcat('testDataFormat',int2str(i),'.txt');
%     read_predict (model , file , folder , 299,normValsOut);
%     end
    
    
    folder = 'rec no mic\MJ\0.02';
    for i=1:1:10
    file = strcat('MJ',int2str(i),'.txt');
    read_predict (model , file , folder , 299,normValsOut);
    end
    
    folder = 'rec no mic\MB\0.02';
    for i=1:1:10
    file = strcat('MB',int2str(i),'.txt');
    read_predict (model , file , folder , 299,normValsOut);
    end

end