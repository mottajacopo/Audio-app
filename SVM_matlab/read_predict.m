function read_predict(model , file , folder , nFrame ,normValsOut)

    dir = strcat(folder ,'\', file);

    [label_vector, instance_matrix] = libsvmread(dir);
    
    
    if(normValsOut ~= 0)
        [instance_matrix,normValsOut] = scale(instance_matrix,normValsOut);
    end
        
    [predicted_label] = svmpredict(ones(nFrame,1),instance_matrix,model);
    occurr = sum(predicted_label == 1);
    disp([file ,' = ' , num2str((occurr/nFrame)*100),'%']);

end