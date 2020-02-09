# mis-2018-exercise-3-sensors-context

The the FFT of magnitude of the three X, Y, and Z accelerometer data are shown on the graph. Windowsize of the FFT can be changed.

<img src="https://github.com/Neginysh/mis-2018-exercise-3-sensors-context/blob/master/SensorMIS/photo_2020-02-09_19-53-52.jpg" width="300">


The app plays a song based on the recognized activity (walking/running).

For the activity recognition I took the average magnitude in a given window size, and as I experienced the magnitude was on average around 10 or below that for sitting position, and for jogging it wa between 10 and 20 and for bikinkg more than 20.
I also defind the location listener to get the users' location and the speed so that with information of the speed and average magnitude the I could be sure if the user was on a car or on a bike; since when the user is on car he/she is sitting and the magnitude value does not go higher than 10.
However the problem was that the location.getSpeed retune just the 0 speed. 
