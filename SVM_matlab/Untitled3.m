%system('copy MB1.txt + MB2.txt + MB3.txt +  CC2.txt + MJ2.txt + MT2.txt modelMB20sec.txt');
[label_vector, instance_matrix] = libsvmread('frase2\modelMJ20sec.txt');
label_vector(1:1596) = 1;
label_vector(1596+1:end) = 2;
model = svmtrain(label_vector, instance_matrix, '-t 2 -c 100 -g 0.003');
testing2(model);
%libsvmwrite('modelSpeaker12.txt', model.sv_coef, model.SVs);

svm_type c_svc
kernel_type rbf
gamma 0.0003
nr_class 2
total_sv 122
rho 0.265436504719889
label 1 2
nr_sv 42 80
SV