
system('copy frase2\CC1.txt +  frase2\MB1.txt + frase2\MJ1.txt + frase2\MT1.txt modelli\modelCC1.txt');
system('copy frase2\CC2.txt +  frase2\MB2.txt + frase2\MJ2.txt + frase2\MT2.txt modelli\modelCC2.txt');
system('copy frase2\CC3.txt +  frase2\MB3.txt + frase2\MJ3.txt + frase2\MT3.txt modelli\modelCC3.txt');
system('copy frase2\CC4.txt +  frase2\MB4.txt + frase2\MJ4.txt + frase2\MT4.txt modelli\modelCC4.txt');
system('copy frase2\CC5.txt +  frase2\MB5.txt + frase2\MJ5.txt + frase2\MT5.txt modelli\modelCC5.txt');
system('copy frase2\CC6.txt +  frase2\MB6.txt + frase2\MJ6.txt + frase2\MT6.txt modelli\modelCC6.txt');
system('copy frase2\CC7.txt +  frase2\MB7.txt + frase2\MJ7.txt + frase2\MT7.txt modelli\modelCC7.txt');
system('copy frase2\CC8.txt +  frase2\MB8.txt + frase2\MJ8.txt + frase2\MT8.txt modelli\modelCC8.txt');
system('copy frase2\CC9.txt +  frase2\MB9.txt + frase2\MJ9.txt + frase2\MT9.txt modelli\modelCC9.txt');
system('copy frase2\CC10.txt +  frase2\MB10.txt + frase2\MJ10.txt + frase2\MT10.txt modelli\modelCC10.txt');


option = '-t 2 -c 100 -g 0.0003';

[label_vector, instance_matrix] = libsvmread('modelli\modelCC1.txt');
label_vector(1:399) = 1;
label_vector(399 +1:end) = 2;

[ modelCC1 , instance_matrix_CC1 ] = generate_model2 ('modelli\modelCC1.txt' , option,label_vector);
[ modelCC2 , instance_matrix_CC2 ] = generate_model2 ('modelli\modelCC2.txt' , option,label_vector);
[ modelCC3 , instance_matrix_CC3 ] = generate_model2 ('modelli\modelCC3.txt' , option,label_vector);
[ modelCC4 , instance_matrix_CC4 ] = generate_model2 ('modelli\modelCC4.txt' , option,label_vector);
[ modelCC5 , instance_matrix_CC5 ] = generate_model2 ('modelli\modelCC5.txt' , option,label_vector);
[ modelCC6 , instance_matrix_CC6 ] = generate_model2 ('modelli\modelCC6.txt' , option,label_vector);
[ modelCC7 , instance_matrix_CC7 ] = generate_model2 ('modelli\modelCC7.txt' , option,label_vector);
[ modelCC8 , instance_matrix_CC8 ] = generate_model2 ('modelli\modelCC8.txt' , option,label_vector);
[ modelCC9 , instance_matrix_CC9 ] = generate_model2 ('modelli\modelCC9.txt' , option,label_vector);
[ modelCC10 , instance_matrix_CC10 ] = generate_model2 ('modelli\modelCC10.txt' , option,label_vector);


instance_matrix_CC_avg = (instance_matrix_CC1 + instance_matrix_CC2 + instance_matrix_CC3 + instance_matrix_CC4 + instance_matrix_CC5 +instance_matrix_CC6 + instance_matrix_CC7 + instance_matrix_CC8 + instance_matrix_CC9 +instance_matrix_CC10 )/10;

modelCC_avg = svmtrain(label_vector, instance_matrix_CC_avg, option);

testing2(modelCC_avg);

