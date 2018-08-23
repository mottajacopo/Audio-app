%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%(in COMMAND WINDOW)
%GO TO PATH 
%CREATE MODEL COMBINING MULTIPLE TXT FILE 
%MOVE MODEL.TXT TO MODELLI FOLDER
%%% cd \Users\motta\Desktop\frase2
%system('copy MarcoB1.txt + MarcoT2.txt + Claudio2.txt + Jacopo2 modelMB.txt')
%%% movefile \Users\motta\Desktop\frase2\model.txt \Users\motta\Downloads\SVM_matlab\modelli

%MODIFY LABEL
nFrame = 299;
label_vector_model(1:nFrame) = 1;
label_vector_model(nFrame +1:end) = 2;

%SAVE MODEL ON FILE 
%libsvmwrite('modelSpeaker2.txt', model.sv_coef, model.SVs);
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

[ modelMT , instance_matrix_MT ] = generate_model ('frase4\modelMT.txt' , '-t 2 -c 1 -g 0.003');
libsvmwrite('modelSpeaker3.txt', modelMT.sv_coef, modelMT.SVs);
%testing1(model);  %model from frase1
testing2(modelMJ);  %model from frase2

