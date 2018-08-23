function testing2(model)

    folder = 'frase3';

    for i=1:1:10     
    file = strcat('MJ',int2str(i),'.txt');
    read_predict (model , file , folder , 299);
    end
    
    for i=1:1:10 
    file = strcat('MB',int2str(i),'.txt');
    read_predict (model , file , folder , 299);
    end
    
    for i=1:1:15   
    file = strcat('MT',int2str(i),'.txt');
    read_predict (model , file , folder , 299);
    end
    
    for i=1:1:11
    file = strcat('CC',int2str(i),'.txt');
    read_predict (model , file , folder , 299);
    end
    
    %for i=1:1:3 
    %file = strcat('testDataFormat',int2str(i),'.txt');
    %read_predict (model , file , folder , 299);
    %end

end