
system('copy frase2\MJ1txt +  frase2\CC1.txt + frase2\MB1.txt + frase2\MT1.txt modelli\modelMJ1.txt');
system('copy frase2\MJ2.txt +  frase2\CC2.txt + frase2\MB2.txt + frase2\MT2.txt modelli\modelMJ2.txt');
system('copy frase2\MJ3.txt +  frase2\CC3.txt + frase2\MB3.txt + frase2\MT3.txt modelli\modelMJ3.txt');
system('copy frase2\MJ4.txt +  frase2\CC4.txt + frase2\MB4.txt + frase2\MT4.txt modelli\modelMJ4.txt');
system('copy frase2\MJ5.txt +  frase2\CC5.txt + frase2\MB5.txt + frase2\MT5.txt modelli\modelMJ5.txt');
system('copy frase2\MJ6.txt +  frase2\CC6.txt + frase2\MB6.txt + frase2\MT6.txt modelli\modelMJ6.txt');
system('copy frase2\MJ7.txt +  frase2\CC7.txt + frase2\MB7.txt + frase2\MT7.txt modelli\modelMJ7.txt');
system('copy frase2\MJ8.txt +  frase2\CC8.txt + frase2\MB8.txt + frase2\MT8.txt modelli\modelMJ8.txt');
system('copy frase2\MJ9.txt +  frase2\CC9.txt + frase2\MB9.txt + frase2\MT9.txt modelli\modelMJ9.txt');
system('copy frase2\MJ10.txt +  frase2\CC10.txt + frase2\MB10.txt + frase2\MT10.txt modelli\modelMJ10.txt');



option = '-t 2 -c 1 -g 0.003';

[label_vector, instance_matrix] = libsvmread('modelli\modelMJ1.txt');
label_vector(1:299) = 1;
label_vector(299 +1:end) = 2;

[ modelMJ1 , instance_matrix_MJ1 ] = generate_model2 ('modelli\modelMJ1.txt' , option,label_vector);
[ modelMJ2 , instance_matrix_MJ2 ] = generate_model2 ('modelli\modelMJ2.txt' , option,label_vector);
[ modelMJ3 , instance_matrix_MJ3 ] = generate_model2 ('modelli\modelMJ3.txt' , option,label_vector);
[ modelMJ4 , instance_matrix_MJ4 ] = generate_model2 ('modelli\modelMJ4.txt' , option,label_vector);
[ modelMJ5 , instance_matrix_MJ5 ] = generate_model2 ('modelli\modelMJ5.txt' , option,label_vector);
[ modelMJ6 , instance_matrix_MJ6 ] = generate_model2 ('modelli\modelMJ6.txt' , option,label_vector);
[ modelMJ7 , instance_matrix_MJ7 ] = generate_model2 ('modelli\modelMJ7.txt' , option,label_vector);
[ modelMJ8 , instance_matrix_MJ8 ] = generate_model2 ('modelli\modelMJ8.txt' , option,label_vector);
[ modelMJ9 , instance_matrix_MJ9 ] = generate_model2 ('modelli\modelMJ9.txt' , option,label_vector);
[ modelMJ10 , instance_matrix_MJ10 ] = generate_model2 ('modelli\modelMJ10.txt' , option,label_vector);



instance_matrix_MJ_avg = (instance_matrix_MJ1 + instance_matrix_MJ2 + instance_matrix_MJ3 + instance_matrix_MJ4 + instance_matrix_MJ5 +instance_matrix_MJ6 + instance_matrix_MJ7 + instance_matrix_MJ8 + instance_matrix_MJ9 +instance_matrix_MJ10 )/10;

modelMJ_avg = svmtrain(label_vector, instance_matrix_MJ_avg, option);
modelMJ1 = svmtrain(label_vector, instance_matrix_MJ1, option);
modelMJ2 = svmtrain(label_vector, instance_matrix_MJ2, option);
modelMJ3 = svmtrain(label_vector, instance_matrix_MJ3, option);
modelMJ4 = svmtrain(label_vector, instance_matrix_MJ4, option);
modelMJ5 = svmtrain(label_vector, instance_matrix_MJ5, option);
modelMJ6 = svmtrain(label_vector, instance_matrix_MJ6, option);
modelMJ7 = svmtrain(label_vector, instance_matrix_MJ7, option);
modelMJ8 = svmtrain(label_vector, instance_matrix_MJ8, option);
modelMJ9 = svmtrain(label_vector, instance_matrix_MJ9, option);
modelMJ10 = svmtrain(label_vector, instance_matrix_MJ10, option);

%libsvmwrite('modelSpeaker2.txt', modelMJ6.sv_coef, modelMJ6.SVs);

testing2(modelMJ_avg);


