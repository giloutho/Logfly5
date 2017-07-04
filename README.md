# Logfly
Logfly is a log book which stores paragliding flights with GPS tracks or not. It's possible to visualize flights and evaluate the score for online contest.

**Build and run requirements :**   
The optimization task (finding largest free flight, flat or FAI triangle) on the tracklogs is computed by an external program writen by [Ondrej Palkovsky](http://www.penguin.cz/~ondrap/paragliding.php) called points. It's not an open source program. It's necessary to put the binary of your platform at the right place. Folder runlib-win contains the windows exexutable, runlib_mac the mac version and runlib_linux the 32 bits binary. To run the code you must place points at the root of the project. To build the project with gradle, you must place the binary of your platform in folder src/main/runlib. In the final bundle, the points module will be placed at the same level than logfly.jar.


[License](LICENSE)
