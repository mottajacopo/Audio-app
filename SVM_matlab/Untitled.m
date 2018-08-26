%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%(in COMMAND WINDOW)
%GO TO PATH 
%CREATE MODEL COMBINING MULTIPLE TXT FILE 
%MOVE MODEL.TXT TO MODELLI FOLDER
%%% cd \Users\motta\Desktop\frase2
%system('copy MarcoB1.txt + MarcoT2.txt + Claudio2.txt + Jacopo2 modelMB.txt')
%%% movefile \Users\motta\Desktop\frase2\model.txt \Users\motta\Downloads\SVM_matlab\modelli

%MODIFY LABEL
%nFrame = 299;
%label_vector_model(1:nFrame) = 1;
%label_vector_model(nFrame +1:end) = 2;

%SAVE MODEL ON FILE 
%libsvmwrite('modelSpeaker2.txt', model.sv_coef, model.SVs);
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

option = '-t 2 -c 1 -g 0.006';

%mettere norm = 0 per normalizzare tra -1 e 1
norm = 1;
if(norm == 0)
    option = '-t 2 -c 12 -g 0.03';
end

[ modelMJ , instance_matrix_MJ ,normValsOut] = generate_model ('modelMJmic.txt' , option, norm);

%libsvmwrite('modelSpeaker3.txt', modelMT.sv_coef, modelMT.SVs);
%testing1(model);  %model from frase1
testing2(modelMJ,normValsOut);  %model from frase2

