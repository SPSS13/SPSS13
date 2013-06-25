if [ -z "$ISGCIROOT" ]
then

ISGCIROOT=/home/ux/Isgci
CLASSPATH=$CLASSPATH:$ISGCIROOT/lib/xalan.jar:$ISGCIROOT/lib/crimson.jar:$ISGCIROOT/lib/ziplock.jar:$ISGCIROOT/lib/xml-writer.jar:$ISGCIROOT/lib/saxon.jar:$ISGCIROOT/lib/getopt.jar:$ISGCIROOT/lib/JFlex.jar:$ISGCIROOT/lib/javacup.jar
export ISGCIROOT CLASSPATH

PATH=$PATH:$ISGCIROOT/bin
#export PATH MANPATH

# II_SYSTEM=/users/db10
# ING_TERM=xterm
# export II_SYSTEM ING_TERM
# PATH=$PATH:$II_SYSTEM/ingres/bin:$II_SYSTEM/ingres/utility
# LD_LIBRARY_PATH=$LD_LIBRARY_PATH:$II_SYSTEM/ingres/lib
# CLASSPATH=$CLASSPATH:$II_SYSTEM/ingres/slje/classes11:$II_SYSTEM/ingres/slje/jdbctest/classes

fi
