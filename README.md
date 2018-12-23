# Logfly
Logfly is a log book which stores paragliding flights with GPS tracks or not. It's possible to visualize flights and evaluate the score for online contest.

**Build and run requirements :**   
The optimization task (finding largest free flight, flat or FAI triangle) on the tracklogs is computed by an external program writen by [Ondrej Palkovsky](http://www.penguin.cz/~ondrap/paragliding.php) called ***points***. 
Flymaster and Flytec communication are made with ***GPSDump*** writen by [Stein Sorensen](http://www.gpsdump.no/).
They are free but not open source programs. It's necessary to put the binaries of your platform at the right place. Folder runlib-win contains the windows exexutables, runlib_mac the mac versions and runlib_linux the 32 bits binaries. To run the code in an IDE, you must place points and GpsDump at the root of the project. To build the project with gradle, you must place the binaries of your platform in folder src/main/runlib. In the final bundle, points and GPSDump will be placed at the same level than logfly.jar.

**Thanks :**  
Logfly was made possible by many Github contributors. Special thanks to [Tom Payne](https://github.com/twpayne) for his great job on paragliding tracks, to [Victor Berchet](https://github.com/vicb) for VisuGPS and [Rishi Gupta](https://github.com/RishiGupta12) for the serial SDK SerialPundit and Alessandro Faillace for Flymaster class. 


[License](LICENSE)
