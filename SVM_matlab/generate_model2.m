function [ model , instance_matrix ] = generate_model2 (file , options ,label_vector2)

    [label_vector, instance_matrix] = libsvmread(file);
    
    model = svmtrain(label_vector2, instance_matrix, options);
    
    %libsvmwrite('modelSpeaker2.txt', model.sv_coef, model.SVs);

end