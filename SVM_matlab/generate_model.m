function model = generate_model (file , options)

    [label_vector, instance_matrix] = libsvmread(file);
    
    %label_vector(1:1196) = 1;
    %label_vector(1196 +1:end) = 2;
    
    model = svmtrain(label_vector, instance_matrix, options);
    
    %libsvmwrite('modelSpeaker2.txt', model.sv_coef, model.SVs);

end