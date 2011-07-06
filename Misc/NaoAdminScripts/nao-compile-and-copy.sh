#!/bin/sh
naonr=$1
if [ -z $naonr ]; then
echo "Nao-Nr:"
read naonr
fi

cd $builddir
if [ -f /proc/cpuinfo ]
then
    numcores=`cat /proc/cpuinfo | grep processor | wc -l`
else 
    numcores=`sysctl -n hw.ncpu`
fi

BUILD_PATH=$2
if [ -z $BUILD_PATH ]; then
BUILD_PATH=${NAOTH_BZR}/NaoTHSoccer/Make
fi

DIST=${NAOTH_BZR}/NaoTHSoccer/dist/Nao/libnaoth.so

cd ${BUILD_PATH}
echo "Compile..." && \
premake4 --platform="Nao" gmake && make -R config=optdebugnao  -j $numcores && \
echo "Copy libnaoth.so to 192.168.0.$naonr" && \
scp $DIST nao@192.168.0.$naonr:/home/nao/naoqi/lib/naoqi/ \
|| (echo "Error occurred! [Press Enter to close terminal]"; exit -1) 