
    [label_vector_JvsAll, instance_matrix_JvsAll] = libsvmread('model_JvsAll.txt');
    [label_vector_MarcoB6vsAll, instance_matrix_MarcoB6vsAll] = libsvmread('model_MarcoB6vsAll.txt');
    [label_vector_JvsAll2, instance_matrix_JvsAll2] = libsvmread('model_JvsAll2.txt');
    [label_vector_JacopoMvsAll, instance_matrix_JacopoMvsAll] = libsvmread('_model_JacopoMvsAll.txt');

    label_vector_JvsAll(1:1196) = 1;
    label_vector_JvsAll(1196 +1:end) = 2;

    label_vector_JacopoMvsAll(1:598) = 1;
    label_vector_JacopoMvsAll(598 +1:end) = 2;

    model = svmtrain(label_vector_JvsAll2, instance_matrix_JvsAll2, '-t 2 -w1 3.7 -w2 0.8 -c 100 -g 0.0003');
    
    %libsvmwrite('modelSpeaker2.txt', model.sv_coef, model.SVs);



