
system('copy frase2\MB1.txt +  frase2\CC1.txt + frase2\MJ1.txt + frase2\MT1.txt modelli\modelMB1.txt');
system('copy frase2\MB2.txt +  frase2\CC2.txt + frase2\MJ2.txt + frase2\MT2.txt modelli\modelMB2.txt');
system('copy frase2\MB3.txt +  frase2\CC3.txt + frase2\MJ3.txt + frase2\MT3.txt modelli\modelMB3.txt');
system('copy frase2\MB4.txt +  frase2\CC4.txt + frase2\MJ4.txt + frase2\MT4.txt modelli\modelMB4.txt');
system('copy frase2\MB5.txt +  frase2\CC5.txt + frase2\MJ5.txt + frase2\MT5.txt modelli\modelMB5.txt');
system('copy frase2\MB6.txt +  frase2\CC6.txt + frase2\MJ6.txt + frase2\MT6.txt modelli\modelMB6.txt');
system('copy frase2\MB7.txt +  frase2\CC7.txt + frase2\MJ7.txt + frase2\MT7.txt modelli\modelMB7.txt');
system('copy frase2\MB8.txt +  frase2\CC8.txt + frase2\MJ8.txt + frase2\MT8.txt modelli\modelMB8.txt');
system('copy frase2\MB9.txt +  frase2\CC9.txt + frase2\MJ9.txt + frase2\MT9.txt modelli\modelMB9.txt');
system('copy frase2\MB10.txt +  frase2\CC10.txt + frase2\MJ10.txt + frase2\MT10.txt modelli\modelMB10.txt');



option = '-t 2 -c 100 -g 0.003';

[label_vector, instance_matrix] = libsvmread('modelli\modelMB1.txt');
label_vector(1:399) = 1;
label_vector(399 +1:end) = 2;

[ modelMB1 , instance_matrix_MB1 ] = generate_model2 ('modelli\modelMB1.txt' , option,label_vector);
[ modelMB2 , instance_matrix_MB2 ] = generate_model2 ('modelli\modelMB2.txt' , option,label_vector);
[ modelMB3 , instance_matrix_MB3 ] = generate_model2 ('modelli\modelMB3.txt' , option,label_vector);
[ modelMB4 , instance_matrix_MB4 ] = generate_model2 ('modelli\modelMB4.txt' , option,label_vector);
[ modelMB5 , instance_matrix_MB5 ] = generate_model2 ('modelli\modelMB5.txt' , option,label_vector);
[ modelMB6 , instance_matrix_MB6 ] = generate_model2 ('modelli\modelMB6.txt' , option,label_vector);
[ modelMB7 , instance_matrix_MB7 ] = generate_model2 ('modelli\modelMB7.txt' , option,label_vector);
[ modelMB8 , instance_matrix_MB8 ] = generate_model2 ('modelli\modelMB8.txt' , option,label_vector);
[ modelMB9 , instance_matrix_MB9 ] = generate_model2 ('modelli\modelMB9.txt' , option,label_vector);
[ modelMB10 , instance_matrix_MB10 ] = generate_model2 ('modelli\modelMB10.txt' , option,label_vector);



instance_matrix_MB_avg = (instance_matrix_MB1 + instance_matrix_MB2 + instance_matrix_MB3 + instance_matrix_MB4 + instance_matrix_MB5 +instance_matrix_MB6 + instance_matrix_MB7 + instance_matrix_MB8 + instance_matrix_MB9 +instance_matrix_MB10)/10;

modelMB_avg = svmtrain(label_vector, instance_matrix_MB_avg, option);

testing2(modelMB_avg);

