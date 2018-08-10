function testing1(model)

    folder = 'frase1';

    for i=1:1:9     
    file = strcat('Claudio',int2str(i),'.txt');
    read_predict (model , file , folder , 299);
    end
    
    for i=1:1:14    
    file = strcat('Jacopo',int2str(i),'.txt');
    read_predict (model , file , folder , 299);
    end
    
    for i=1:1:6     
    file = strcat('MarcoB',int2str(i),'.txt');
    read_predict (model , file , folder , 299);
    end
    
    for i=1:1:3    
    file = strcat('Piera',int2str(i),'.txt');
    read_predict (model , file , folder , 299);
    end
    
    for i=1:1:3    
    file = strcat('Ste',int2str(i),'.txt');
    read_predict (model , file , folder , 299);
    end
    
    for i=1:1:3     
    file = strcat('Void',int2str(i),'.txt');
    read_predict (model , file , folder , 299);
    end
    
    for i=1:1:3     
    file = strcat('Jack',int2str(i),'.txt');
    read_predict (model , file , folder , 299);
    end
    
    for i=1:1:6     
    file = strcat('Michi',int2str(i),'.txt');
    read_predict (model , file , folder , 299);
    end
    
    for i=1:1:15 
    file = strcat('J',int2str(i),'.txt');
    read_predict (model , file , folder , 299);
    end

end