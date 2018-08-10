function testing2(model)

    folder = 'frase2';

    for i=1:1:13     
    file = strcat('MJ',int2str(i),'.txt');
    read_predict (model , file , folder , 399);
    end
    
    for i=1:1:16 
    file = strcat('MB',int2str(i),'.txt');
    read_predict (model , file , folder , 399);
    end
    
    for i=1:1:16   
    file = strcat('MT',int2str(i),'.txt');
    read_predict (model , file , folder , 199);
    end

end