function testing(model)
[label_vector_Claudio1, instance_matrix_Claudio1] = libsvmread('Claudio1.txt');
[label_vector_Claudio2, instance_matrix_Claudio2] = libsvmread('Claudio2.txt');
[label_vector_Claudio3, instance_matrix_Claudio3] = libsvmread('Claudio3.txt');
[label_vector_Claudio4, instance_matrix_Claudio4] = libsvmread('Claudio4.txt');
[label_vector_Claudio5, instance_matrix_Claudio5] = libsvmread('Claudio5.txt');
[label_vector_Claudio6, instance_matrix_Claudio6] = libsvmread('Claudio6.txt');
[label_vector_Claudio7, instance_matrix_Claudio7] = libsvmread('Claudio7.txt');
[label_vector_Claudio8, instance_matrix_Claudio8] = libsvmread('Claudio8.txt');
[label_vector_Claudio9, instance_matrix_Claudio9] = libsvmread('Claudio9.txt');
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
[label_vector_Jacopo1, instance_matrix_Jacopo1] = libsvmread('Jacopo1.txt');
[label_vector_Jacopo2, instance_matrix_Jacopo2] = libsvmread('Jacopo2.txt');
[label_vector_Jacopo3, instance_matrix_Jacopo3] = libsvmread('Jacopo3.txt');
[label_vector_Jacopo4, instance_matrix_Jacopo4] = libsvmread('Jacopo4.txt');
[label_vector_Jacopo5, instance_matrix_Jacopo5] = libsvmread('Jacopo5.txt');
[label_vector_Jacopo6, instance_matrix_Jacopo6] = libsvmread('Jacopo6.txt');
[label_vector_Jacopo7, instance_matrix_Jacopo7] = libsvmread('Jacopo7.txt');
[label_vector_Jacopo8, instance_matrix_Jacopo8] = libsvmread('Jacopo8.txt');
[label_vector_Jacopo9, instance_matrix_Jacopo9] = libsvmread('Jacopo9.txt');
[label_vector_Jacopo10, instance_matrix_Jacopo10] = libsvmread('Jacopo10.txt');
[label_vector_Jacopo11, instance_matrix_Jacopo11] = libsvmread('Jacopo11.txt');
[label_vector_Jacopo12, instance_matrix_Jacopo12] = libsvmread('Jacopo12.txt');
[label_vector_Jacopo13, instance_matrix_Jacopo13] = libsvmread('Jacopo13.txt');
[label_vector_Jacopo14, instance_matrix_Jacopo14] = libsvmread('Jacopo14.txt');
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
[label_vector_MarcoB1, instance_matrix_MarcoB1] = libsvmread('MarcoB1.txt');
[label_vector_MarcoB2, instance_matrix_MarcoB2] = libsvmread('MarcoB2.txt');
[label_vector_MarcoB3, instance_matrix_MarcoB3] = libsvmread('MarcoB3.txt');
[label_vector_MarcoB4, instance_matrix_MarcoB4] = libsvmread('MarcoB4.txt');
[label_vector_MarcoB5, instance_matrix_MarcoB5] = libsvmread('MarcoB5.txt');
[label_vector_MarcoB6, instance_matrix_MarcoB6] = libsvmread('MarcoB6.txt');
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
[label_vector_Piera1, instance_matrix_Piera1] = libsvmread('Piera1.txt');
[label_vector_Piera2, instance_matrix_Piera2] = libsvmread('Piera2.txt');
[label_vector_Piera3, instance_matrix_Piera3] = libsvmread('Piera3.txt');
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
[label_vector_Ste1, instance_matrix_Ste1] = libsvmread('Ste1.txt');
[label_vector_Ste2, instance_matrix_Ste2] = libsvmread('Ste2.txt');
[label_vector_Ste3, instance_matrix_Ste3] = libsvmread('Ste3.txt');
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
[label_vector_Void1, instance_matrix_Void1] = libsvmread('Void1.txt');
[label_vector_Void2, instance_matrix_Void2] = libsvmread('Void2.txt');
[label_vector_Void3, instance_matrix_Void3] = libsvmread('Void3.txt');
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
[label_vector_Jack1, instance_matrix_Jack1] = libsvmread('Jack1.txt');
[label_vector_Jack2, instance_matrix_Jack2] = libsvmread('Jack2.txt');
[label_vector_Jack3, instance_matrix_Jack3] = libsvmread('Jack3.txt');
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
[label_vector_Michi1, instance_matrix_Michi1] = libsvmread('Michi1.txt');
[label_vector_Michi2, instance_matrix_Michi2] = libsvmread('Michi2.txt');
[label_vector_Michi3, instance_matrix_Michi3] = libsvmread('Michi3.txt');
[label_vector_Michi4, instance_matrix_Michi4] = libsvmread('Michi4.txt');
[label_vector_Michi5, instance_matrix_Michi5] = libsvmread('Michi5.txt');
[label_vector_Michi6, instance_matrix_Michi6] = libsvmread('Michi6.txt');
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
[label_vector_J1, instance_matrix_J1] = libsvmread('J1.txt');
[label_vector_J2, instance_matrix_J2] = libsvmread('J2.txt');
[label_vector_J3, instance_matrix_J3] = libsvmread('J3.txt');
[label_vector_J4, instance_matrix_J4] = libsvmread('J4.txt');
[label_vector_J5, instance_matrix_J5] = libsvmread('J5.txt');
[label_vector_J6, instance_matrix_J6] = libsvmread('J6.txt');
[label_vector_J7, instance_matrix_J7] = libsvmread('J7.txt');
[label_vector_J8, instance_matrix_J8] = libsvmread('J8.txt');
[label_vector_J9, instance_matrix_J9] = libsvmread('J9.txt');
[label_vector_J10, instance_matrix_J10] = libsvmread('J10.txt');
[label_vector_J11, instance_matrix_J11] = libsvmread('J11.txt');
[label_vector_J12, instance_matrix_J12] = libsvmread('J12.txt');
[label_vector_J13, instance_matrix_J13] = libsvmread('J13.txt');
[label_vector_J14, instance_matrix_J14] = libsvmread('J14.txt');
[label_vector_J15, instance_matrix_J15] = libsvmread('J15.txt');
[label_vector_J16, instance_matrix_J16] = libsvmread('J16.txt');
[label_vector_J17, instance_matrix_J17] = libsvmread('J17.txt');
[label_vector_J18, instance_matrix_J18] = libsvmread('J18.txt');
[label_vector_J19, instance_matrix_J19] = libsvmread('J19.txt');
[label_vector_J20, instance_matrix_J20] = libsvmread('J20.txt');
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
[label_vector_testDataFormat1, instance_matrix_testDataFormat1] = libsvmread('testDataFormat1.txt');
[label_vector_testDataFormat2, instance_matrix_testDataFormat2] = libsvmread('testDataFormat2.txt');
[label_vector_testDataFormat3, instance_matrix_testDataFormat3] = libsvmread('testDataFormat3.txt');
[label_vector_testDataFormat4, instance_matrix_testDataFormat4] = libsvmread('testDataFormat4.txt');
[label_vector_testDataFormat5, instance_matrix_testDataFormat5] = libsvmread('testDataFormat5.txt');
[label_vector_testDataFormat6, instance_matrix_testDataFormat6] = libsvmread('testDataFormat6.txt');
[label_vector_testDataFormat7, instance_matrix_testDataFormat7] = libsvmread('testDataFormat7.txt');
[label_vector_testDataFormat8, instance_matrix_testDataFormat8] = libsvmread('testDataFormat8.txt');
[label_vector_testDataFormat9, instance_matrix_testDataFormat9] = libsvmread('testDataFormat9.txt');
[label_vector_testDataFormat10, instance_matrix_testDataFormat10] = libsvmread('testDataFormat10.txt');

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

[predicted_label] = svmpredict(ones(299,1),instance_matrix_MarcoB1,model);
occurr = sum(predicted_label == 1);
disp(['Marco1 = ', num2str((occurr/299)*100),'%']);

[predicted_label] = svmpredict(ones(299,1),instance_matrix_MarcoB2,model);
occurr = sum(predicted_label == 1);
disp(['Marco2 = ', num2str((occurr/299)*100),'%']);

[predicted_label] = svmpredict(ones(299,1),instance_matrix_MarcoB3,model);
occurr = sum(predicted_label == 1);
disp(['Marco3 = ', num2str((occurr/299)*100),'%']);

[predicted_label] = svmpredict(ones(299,1),instance_matrix_MarcoB4,model);
occurr = sum(predicted_label == 1);
disp(['Marco4 = ', num2str((occurr/299)*100),'%']);

[predicted_label] = svmpredict(ones(299,1),instance_matrix_MarcoB5,model);
occurr = sum(predicted_label == 1);
disp(['Marco5 = ', num2str((occurr/299)*100),'%']);

[predicted_label] = svmpredict(ones(299,1),instance_matrix_MarcoB6,model);
occurr = sum(predicted_label == 1);
disp(['Marco6 = ', num2str((occurr/299)*100),'%']);

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

[predicted_label] = svmpredict(ones(299,1),instance_matrix_Jacopo1,model);
occurr = sum(predicted_label == 1);
disp(['Jacopo1 = ', num2str((occurr/299)*100),'%']);

[predicted_label] = svmpredict(ones(299,1),instance_matrix_Jacopo2,model);
occurr = sum(predicted_label == 1);
disp(['Jacopo2 = ', num2str((occurr/299)*100),'%']);

[predicted_label] = svmpredict(ones(299,1),instance_matrix_Jacopo3,model);
occurr = sum(predicted_label == 1);
disp(['Jacopo3 = ', num2str((occurr/299)*100),'%']);

[predicted_label] = svmpredict(ones(299,1),instance_matrix_Jacopo4,model);
occurr = sum(predicted_label == 1);
disp(['Jacopo4 = ', num2str((occurr/299)*100),'%']);

[predicted_label] = svmpredict(ones(299,1),instance_matrix_Jacopo5,model);
occurr = sum(predicted_label == 1);
disp(['Jacopo5 = ', num2str((occurr/299)*100),'%']);

[predicted_label] = svmpredict(ones(299,1),instance_matrix_Jacopo6,model);
occurr = sum(predicted_label == 1);
disp(['Jacopo6 = ', num2str((occurr/299)*100),'%']);

[predicted_label] = svmpredict(ones(299,1),instance_matrix_Jacopo7,model);
occurr = sum(predicted_label == 1);
disp(['Jacopo7 = ', num2str((occurr/299)*100),'%']);

[predicted_label] = svmpredict(ones(299,1),instance_matrix_Jacopo8,model);
occurr = sum(predicted_label == 1);
disp(['Jacopo8 = ', num2str((occurr/299)*100),'%']);

[predicted_label] = svmpredict(ones(299,1),instance_matrix_Jacopo9,model);
occurr = sum(predicted_label == 1);
disp(['Jacopo9 = ', num2str((occurr/299)*100),'%']);

[predicted_label] = svmpredict(ones(299,1),instance_matrix_Jacopo10,model);
occurr = sum(predicted_label == 1);
disp(['Jacopo10 = ', num2str((occurr/299)*100),'%']);

[predicted_label] = svmpredict(ones(299,1),instance_matrix_Jacopo11,model);
occurr = sum(predicted_label == 1);
disp(['Jacopo11 = ', num2str((occurr/299)*100),'%']);

[predicted_label] = svmpredict(ones(299,1),instance_matrix_Jacopo12,model);
occurr = sum(predicted_label == 1);
disp(['Jacopo12 = ', num2str((occurr/299)*100),'%']);

[predicted_label] = svmpredict(ones(299,1),instance_matrix_Jacopo13,model);
occurr = sum(predicted_label == 1);
disp(['Jacopo13 = ', num2str((occurr/299)*100),'%']);

[predicted_label] = svmpredict(ones(299,1),instance_matrix_Jacopo14,model);
occurr = sum(predicted_label == 1);
disp(['Jacopo14 = ', num2str((occurr/299)*100),'%']);

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%5

[predicted_label] = svmpredict(ones(299,1),instance_matrix_Claudio3,model);
occurr = sum(predicted_label == 1);
disp(['Claudio3 = ', num2str((occurr/299)*100),'%']);

[predicted_label] = svmpredict(ones(299,1),instance_matrix_Claudio1,model);
occurr = sum(predicted_label == 1);
disp(['Claudio1 = ', num2str((occurr/299)*100),'%']);

[predicted_label] = svmpredict(ones(299,1),instance_matrix_Claudio2,model);
occurr = sum(predicted_label == 1);
disp(['Claudio2 = ', num2str((occurr/299)*100),'%']);

[predicted_label] = svmpredict(ones(299,1),instance_matrix_Claudio4,model);
occurr = sum(predicted_label == 1);
disp(['Claudio4 = ', num2str((occurr/299)*100),'%']);

[predicted_label] = svmpredict(ones(299,1),instance_matrix_Claudio5,model);
occurr = sum(predicted_label == 1);
disp(['Claudio5 = ', num2str((occurr/299)*100),'%']);

[predicted_label] = svmpredict(ones(299,1),instance_matrix_Claudio6,model);
occurr = sum(predicted_label == 1);
disp(['Claudio6 = ', num2str((occurr/299)*100),'%']);

[predicted_label] = svmpredict(ones(299,1),instance_matrix_Claudio7,model);
occurr = sum(predicted_label == 1);
disp(['Claudio7 = ', num2str((occurr/299)*100),'%']);

[predicted_label] = svmpredict(ones(299,1),instance_matrix_Claudio8,model);
occurr = sum(predicted_label == 1);
disp(['Claudio8 = ', num2str((occurr/299)*100),'%']);

[predicted_label] = svmpredict(ones(299,1),instance_matrix_Claudio9,model);
occurr = sum(predicted_label == 1);
disp(['Claudio9 = ', num2str((occurr/299)*100),'%']);

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

[predicted_label] = svmpredict(ones(299,1),instance_matrix_Piera1,model);
occurr = sum(predicted_label == 1);
disp(['Piera1 = ', num2str((occurr/299)*100),'%']);

[predicted_label] = svmpredict(ones(299,1),instance_matrix_Piera2,model);
occurr = sum(predicted_label == 1);
disp(['Piera2 = ', num2str((occurr/299)*100),'%']);

[predicted_label] = svmpredict(ones(299,1),instance_matrix_Piera3,model);
occurr = sum(predicted_label == 1);
disp(['Piera3 = ', num2str((occurr/299)*100),'%']);

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

[predicted_label] = svmpredict(ones(299,1),instance_matrix_Ste1,model);
occurr = sum(predicted_label == 1);
disp(['Ste1 = ', num2str((occurr/299)*100),'%']);

[predicted_label] = svmpredict(ones(299,1),instance_matrix_Ste2,model);
occurr = sum(predicted_label == 1);
disp(['Ste2 = ', num2str((occurr/299)*100),'%']);

[predicted_label] = svmpredict(ones(299,1),instance_matrix_Ste3,model);
occurr = sum(predicted_label == 1);
disp(['Ste3 = ', num2str((occurr/299)*100),'%']);

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

[predicted_label] = svmpredict(ones(299,1),instance_matrix_Jack1,model);
occurr = sum(predicted_label == 1);
disp(['Jack1 = ', num2str((occurr/299)*100),'%']);

[predicted_label] = svmpredict(ones(299,1),instance_matrix_Jack2,model);
occurr = sum(predicted_label == 1);
disp(['Jack2 = ', num2str((occurr/299)*100),'%']);

[predicted_label] = svmpredict(ones(299,1),instance_matrix_Jack3,model);
occurr = sum(predicted_label == 1);
disp(['Jack3 = ', num2str((occurr/299)*100),'%']);

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

[predicted_label] = svmpredict(ones(299,1),instance_matrix_Michi1,model);
occurr = sum(predicted_label == 1);
disp(['Michi1 = ', num2str((occurr/299)*100),'%']);

[predicted_label] = svmpredict(ones(299,1),instance_matrix_Michi2,model);
occurr = sum(predicted_label == 1);
disp(['Michi2 = ', num2str((occurr/299)*100),'%']);

[predicted_label] = svmpredict(ones(299,1),instance_matrix_Michi3,model);
occurr = sum(predicted_label == 1);
disp(['Michi3 = ', num2str((occurr/299)*100),'%']);

[predicted_label] = svmpredict(ones(299,1),instance_matrix_Michi4,model);
occurr = sum(predicted_label == 1);
disp(['Michi4 = ', num2str((occurr/299)*100),'%']);

[predicted_label] = svmpredict(ones(299,1),instance_matrix_Michi5,model);
occurr = sum(predicted_label == 1);
disp(['Michi5 = ', num2str((occurr/299)*100),'%']);

[predicted_label] = svmpredict(ones(299,1),instance_matrix_Michi6,model);
occurr = sum(predicted_label == 1);
disp(['Michi6 = ', num2str((occurr/299)*100),'%']);

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

[predicted_label] = svmpredict(ones(299,1),instance_matrix_Void1,model);
occurr = sum(predicted_label == 1);
disp(['Void1 = ', num2str((occurr/299)*100),'%']);

[predicted_label] = svmpredict(ones(299,1),instance_matrix_Void2,model);
occurr = sum(predicted_label == 1);
disp(['Void2 = ', num2str((occurr/299)*100),'%']);

[predicted_label] = svmpredict(ones(299,1),instance_matrix_Void3,model);
occurr = sum(predicted_label == 1);
disp(['Void3 = ', num2str((occurr/299)*100),'%']);

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

[predicted_label] = svmpredict(ones(299,1),instance_matrix_J1,model);
occurr = sum(predicted_label == 1);
disp(['J1 = ', num2str((occurr/299)*100),'%']);

[predicted_label] = svmpredict(ones(299,1),instance_matrix_J2,model);
occurr = sum(predicted_label == 1);
disp(['J2 = ', num2str((occurr/299)*100),'%']);

[predicted_label] = svmpredict(ones(299,1),instance_matrix_J3,model);
occurr = sum(predicted_label == 1);
disp(['J3 = ', num2str((occurr/299)*100),'%']);

[predicted_label] = svmpredict(ones(299,1),instance_matrix_J4,model);
occurr = sum(predicted_label == 1);
disp(['J4 = ', num2str((occurr/299)*100),'%']);

[predicted_label] = svmpredict(ones(299,1),instance_matrix_J5,model);
occurr = sum(predicted_label == 1);
disp(['J5 = ', num2str((occurr/299)*100),'%']);

[predicted_label] = svmpredict(ones(299,1),instance_matrix_J6,model);
occurr = sum(predicted_label == 1);
disp(['J6 = ', num2str((occurr/299)*100),'%']);

[predicted_label] = svmpredict(ones(299,1),instance_matrix_J7,model);
occurr = sum(predicted_label == 1);
disp(['J7 = ', num2str((occurr/299)*100),'%']);

[predicted_label] = svmpredict(ones(299,1),instance_matrix_J8,model);
occurr = sum(predicted_label == 1);
disp(['J8 = ', num2str((occurr/299)*100),'%']);

[predicted_label] = svmpredict(ones(299,1),instance_matrix_J9,model);
occurr = sum(predicted_label == 1);
disp(['J9 = ', num2str((occurr/299)*100),'%']);

[predicted_label] = svmpredict(ones(299,1),instance_matrix_J10,model);
occurr = sum(predicted_label == 1);
disp(['J10 = ', num2str((occurr/299)*100),'%']);

[predicted_label] = svmpredict(ones(299,1),instance_matrix_J11,model);
occurr = sum(predicted_label == 1);
disp(['J11 = ', num2str((occurr/299)*100),'%']);

[predicted_label] = svmpredict(ones(299,1),instance_matrix_J12,model);
occurr = sum(predicted_label == 1);
disp(['J12 = ', num2str((occurr/299)*100),'%']);

[predicted_label] = svmpredict(ones(299,1),instance_matrix_J12,model);
occurr = sum(predicted_label == 1);
disp(['J12 = ', num2str((occurr/299)*100),'%']);

[predicted_label] = svmpredict(ones(299,1),instance_matrix_J13,model);
occurr = sum(predicted_label == 1);
disp(['J13 = ', num2str((occurr/299)*100),'%']);

[predicted_label] = svmpredict(ones(299,1),instance_matrix_J14,model);
occurr = sum(predicted_label == 1);
disp(['J14 = ', num2str((occurr/299)*100),'%']);

[predicted_label] = svmpredict(ones(299,1),instance_matrix_J15,model);
occurr = sum(predicted_label == 1);
disp(['J15 = ', num2str((occurr/299)*100),'%']);

[predicted_label] = svmpredict(ones(299,1),instance_matrix_J16,model);
occurr = sum(predicted_label == 1);
disp(['J16 = ', num2str((occurr/299)*100),'%']);

[predicted_label] = svmpredict(ones(299,1),instance_matrix_J17,model);
occurr = sum(predicted_label == 1);
disp(['J17 = ', num2str((occurr/299)*100),'%']);

[predicted_label] = svmpredict(ones(299,1),instance_matrix_J18,model);
occurr = sum(predicted_label == 1);
disp(['J18 = ', num2str((occurr/299)*100),'%']);

[predicted_label] = svmpredict(ones(299,1),instance_matrix_J19,model);
occurr = sum(predicted_label == 1);
disp(['J19 = ', num2str((occurr/299)*100),'%']);

[predicted_label] = svmpredict(ones(299,1),instance_matrix_J20,model);
occurr = sum(predicted_label == 1);
disp(['J20 = ', num2str((occurr/299)*100),'%']);

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

[predicted_label] = svmpredict(ones(299,1),instance_matrix_testDataFormat1,model);
occurr = sum(predicted_label == 1);
disp(['testDataFormat1 = ', num2str((occurr/299)*100),'%']);

[predicted_label] = svmpredict(ones(299,1),instance_matrix_testDataFormat2,model);
occurr = sum(predicted_label == 1);
disp(['testDataFormat2 = ', num2str((occurr/299)*100),'%']);

[predicted_label] = svmpredict(ones(299,1),instance_matrix_testDataFormat3,model);
occurr = sum(predicted_label == 1);
disp(['testDataFormat3 = ', num2str((occurr/299)*100),'%']);

[predicted_label] = svmpredict(ones(299,1),instance_matrix_testDataFormat4,model);
occurr = sum(predicted_label == 1);
disp(['testDataFormat4 = ', num2str((occurr/299)*100),'%']);

[predicted_label] = svmpredict(ones(299,1),instance_matrix_testDataFormat5,model);
occurr = sum(predicted_label == 1);
disp(['testDataFormat5 = ', num2str((occurr/299)*100),'%']);

[predicted_label] = svmpredict(ones(299,1),instance_matrix_testDataFormat6,model);
occurr = sum(predicted_label == 1);
disp(['testDataFormat6 = ', num2str((occurr/299)*100),'%']);

[predicted_label] = svmpredict(ones(299,1),instance_matrix_testDataFormat7,model);
occurr = sum(predicted_label == 1);
disp(['testDataFormat7 = ', num2str((occurr/299)*100),'%']);

[predicted_label] = svmpredict(ones(299,1),instance_matrix_testDataFormat8,model);
occurr = sum(predicted_label == 1);
disp(['testDataFormat8 = ', num2str((occurr/299)*100),'%']);

[predicted_label] = svmpredict(ones(299,1),instance_matrix_testDataFormat9,model);
occurr = sum(predicted_label == 1);
disp(['testDataFormat9 = ', num2str((occurr/299)*100),'%']);

[predicted_label] = svmpredict(ones(299,1),instance_matrix_testDataFormat10,model);
occurr = sum(predicted_label == 1);
disp(['testDataFormat10 = ', num2str((occurr/299)*100),'%']);

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

end