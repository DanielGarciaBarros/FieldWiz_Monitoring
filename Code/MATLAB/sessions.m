function readFwz(filename)

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