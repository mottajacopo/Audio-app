function [ model , instance_matrix ,normValsOut ] = generate_model (file , options, norm)

    [label_vector, instance_matrix] = libsvmread(file);
    
    label_vector(1:399) = 1;
    label_vector(399 +1:end) = 2;
    
    normValsOut = 0;
    
    if(norm == 0)
        [instance_matrix,normValsOut] = scale(instance_matrix,0);
    end

    model = svmtrain(label_vector, instance_matrix, options);
    
    %libsvmwrite('modelSpeaker2.txt', model.sv_coef, model.SVs);

end