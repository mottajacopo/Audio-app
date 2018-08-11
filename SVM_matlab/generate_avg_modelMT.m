
system('copy frase2\MT1.txt +  frase2\CC1.txt + frase2\MJ1.txt + frase2\MB1.txt modelli\modelMT1.txt');
system('copy frase2\MT2.txt +  frase2\CC2.txt + frase2\MJ2.txt + frase2\MB2.txt modelli\modelMT2.txt');
system('copy frase2\MT3.txt +  frase2\CC3.txt + frase2\MJ3.txt + frase2\MB3.txt modelli\modelMT3.txt');
system('copy frase2\MT4.txt +  frase2\CC4.txt + frase2\MJ4.txt + frase2\MB4.txt modelli\modelMT4.txt');
system('copy frase2\MT5.txt +  frase2\CC5.txt + frase2\MJ5.txt + frase2\MB5.txt modelli\modelMT5.txt');
system('copy frase2\MT6.txt +  frase2\CC6.txt + frase2\MJ6.txt + frase2\MB6.txt modelli\modelMT6.txt');
system('copy frase2\MT7.txt +  frase2\CC7.txt + frase2\MJ7.txt + frase2\MB7.txt modelli\modelMT7.txt');
system('copy frase2\MT8.txt +  frase2\CC8.txt + frase2\MJ8.txt + frase2\MB8.txt modelli\modelMT8.txt');
system('copy frase2\MT9.txt +  frase2\CC9.txt + frase2\MJ9.txt + frase2\MB9.txt modelli\modelMT9.txt');
system('copy frase2\MT10.txt +  frase2\CC10.txt + frase2\MJ10.txt + frase2\MB10.txt modelli\modelMT10.txt');


option = '-t 2 -c 100 -g 0.0003';

[label_vector, instance_matrix] = libsvmread('modelli\modelMT1.txt');
label_vector(1:399) = 1;
label_vector(399 +1:end) = 2;

[ modelMT1 , instance_matrix_MT1 ] = generate_model2 ('modelli\modelMT1.txt' , option,label_vector);
[ modelMT2 , instance_matrix_MT2 ] = generate_model2 ('modelli\modelMT2.txt' , option,label_vector);
[ modelMT3 , instance_matrix_MT3 ] = generate_model2 ('modelli\modelMT3.txt' , option,label_vector);
[ modelMT4 , instance_matrix_MT4 ] = generate_model2 ('modelli\modelMT4.txt' , option,label_vector);
[ modelMT5 , instance_matrix_MT5 ] = generate_model2 ('modelli\modelMT5.txt' , option,label_vector);
[ modelMT6 , instance_matrix_MT6 ] = generate_model2 ('modelli\modelMT6.txt' , option,label_vector);
[ modelMT7 , instance_matrix_MT7 ] = generate_model2 ('modelli\modelMT7.txt' , option,label_vector);
[ modelMT8 , instance_matrix_MT8 ] = generate_model2 ('modelli\modelMT8.txt' , option,label_vector);
[ modelMT9 , instance_matrix_MT9 ] = generate_model2 ('modelli\modelMT9.txt' , option,label_vector);
[ modelMT10 , instance_matrix_MT10 ] = generate_model2 ('modelli\modelMT10.txt' , option,label_vector);


instance_matrix_MT_avg = (instance_matrix_MT1 + instance_matrix_MT2 + instance_matrix_MT3 + instance_matrix_MT4 + instance_matrix_MT5 +instance_matrix_MT6 + instance_matrix_MT7 + instance_matrix_MT8 + instance_matrix_MT9 +instance_matrix_MT10)/10;

modelMT_avg = svmtrain(label_vector, instance_matrix_MT_avg, option);

testing2(modelMT_avg);

