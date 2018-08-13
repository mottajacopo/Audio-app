function testing2(model)

    folder = 'frase2';

    for i=1:1:10     
    file = strcat('MJ',int2str(i),'.txt');
    read_predict (model , file , folder , 399);
    end
    
    for i=1:1:10 
    file = strcat('MB',int2str(i),'.txt');
    read_predict (model , file , folder , 399);
    end
    
    for i=1:1:10   
    file = strcat('MT',int2str(i),'.txt');
    read_predict (model , file , folder , 199);
    end
    
    for i=1:1:10
    file = strcat('CC',int2str(i),'.txt');
    read_predict (model , file , folder , 399);
    end
    
    for i=1:1:3 
    file = strcat('testDataFormat',int2str(i),'.txt');
    read_predict (model , file , folder , 399);
    end

end