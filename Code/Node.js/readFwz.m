
% MODIFIED BY DANIEL GARCIA
% ----------
function readFwz(filename, startSession, endSession, username)
% ----------


dataErrors=0;

lat=0.0;
lon=0.0;
speed=0.0;
time=0.0;
alt=0.0;
hr=0.0;
hdop=0.0;
numsv=0.0;

hr_seg=0.0;
hdop_seg=0.0;
numsv_seg=0.0;

%% CODE EFFACEE concernant la récupération des données GPS 


% -- Récupération des valeur selon la session
allValues = [lat lon speed time alt];
startRow=[];
endRow=[];
startSessionNum = datenum(startSession);
endSessionNum = datenum(endSession);
while isempty(startRow)
    startRow = find(abs(time-startSessionNum) < 0.0001);
    startSessionNum = addtodate(startSessionNum, 1, 'second');
end
while isempty(endRow)
    endRow = find(abs(time-endSessionNum) < 0.0001);
    endSessionNum = addtodate(endSessionNum, 1, 'second');
end
startRow = startRow(1);
endRow = endRow(1);
allValues = allValues(startRow:endRow,:);

% -- Calcul de la vitesse max, vitesse moyenne et dist. parcourue
speed = speed(startRow:endRow,:);
speedMax = max(speed(:));
speedAvg = sum(speed) / (endRow - startRow);
dist = 0;
for i = 1:endRow-startRow;
	dist = dist + speed(i) * 0.1 / 3600;
end

% -- Affichage des données
disp(speedMax);
disp(speedAvg);
disp(dist);
disp(allValues(1,1));
disp(allValues(1,2));
disp(username);
disp(startSession);

% -- Sauvegarde du fichier CSV
startSession = strrep(startSession,'-','_');
startSession = strrep(startSession,' ','_');
startSession = strrep(startSession,':','_');
pathDir = strcat("Users/", username, "/", startSession, ".csv");
delete(filename);
csvwrite(pathDir, allValues);
% -----------

end