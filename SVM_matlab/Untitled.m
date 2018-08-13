%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%(in COMMAND WINDOW)
%GO TO PATH 
%CREATE MODEL COMBINING MULTIPLE TXT FILE 
%MOVE MODEL.TXT TO MODELLI FOLDER
%%% cd \Users\motta\Desktop\frase2
%%% system('copy file1.txt + file2.txt + file3.txt + file4.txt model.txt')
%%% movefile \Users\motta\Desktop\frase2\model.txt \Users\motta\Downloads\SVM_matlab\modelli

%MODIFY LABEL
%%%nFrame = 798;
%%%label_vector_model(1:nFrame) = 1;
%%%label_vector_model(nFrame +1:end) = 2;

%SAVE MODEL ON FILE 
%libsvmwrite('modelSpeaker2.txt', model.sv_coef, model.SVs);
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

[ modelMJ , instance_matrix_MJ ] = generate_model ('modelli\modelMJ.txt' , '-t 2 -w1 3.7 -w2 0.8 -c 1 -g 0.003');

%testing1(model);  %model from frase1
testing2(modelMJ_avg);  %model from frase2

