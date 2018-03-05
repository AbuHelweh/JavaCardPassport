#
# Generated Makefile - do not edit!
#
# Edit the Makefile in the project folder instead (../Makefile). Each target
# has a -pre and a -post target defined where you can add customized code.
#
# This makefile implements configuration specific macros and targets.


# Environment
MKDIR=mkdir
CP=cp
GREP=grep
NM=nm
CCADMIN=CCadmin
RANLIB=ranlib
CC=gcc
CCC=g++
CXX=g++
FC=gfortran
AS=as

# Macros
CND_PLATFORM=GNU-Linux
CND_DLIB_EXT=so
CND_CONF=Debug
CND_DISTDIR=dist
CND_BUILDDIR=build

# Include project Makefile
include Makefile

# Object Directory
OBJECTDIR=${CND_BUILDDIR}/${CND_CONF}/${CND_PLATFORM}

# Object Files
OBJECTFILES= \
	${OBJECTDIR}/_ext/e699828c/StasmBridgeLib.o


# C Compiler Flags
CFLAGS=-shared

# CC Compiler Flags
CCFLAGS=-shared --verbose
CXXFLAGS=-shared --verbose

# Fortran Compiler Flags
FFLAGS=

# Assembler Flags
ASFLAGS=

# Link Libraries and Options
LDLIBSOPTIONS=-L/home/luca/workspace/JavaCardPassport/Libs/Downloads/stasm4.1.0/Build1 -L/home/luca/TCC/OpenCV/opencv-3.3.0/Build/lib -lstasm -lopencv_core -lopencv_imgproc -lopencv_imgcodecs -lopencv_highgui -lopencv_objdetect

# Build Targets
.build-conf: ${BUILD_SUBPROJECTS}
	"${MAKE}"  -f nbproject/Makefile-${CND_CONF}.mk dist/StasmBridgeLib.so

dist/StasmBridgeLib.so: ${OBJECTFILES}
	${MKDIR} -p dist
	${LINK.cc} -o dist/StasmBridgeLib.so ${OBJECTFILES} ${LDLIBSOPTIONS} --verbose -shared -fPIC

${OBJECTDIR}/_ext/e699828c/StasmBridgeLib.o: /home/luca/workspace/JavaCardPassport/StasmBridgeLib/StasmBridgeLib.cpp 
	${MKDIR} -p ${OBJECTDIR}/_ext/e699828c
	${RM} "$@.d"
	$(COMPILE.cc) -g -I/home/luca/Documents/TCC/jdk1.8.0_131/include -I/home/luca/Documents/TCC/jdk1.8.0_131/include/linux -I/home/luca/workspace/JavaCardPassport/Libs/Downloads/stasm4.1.0/stasm -I/home/luca/workspace/JavaCardPassport/Libs/Downloads/stasm4.1.0/stasm/MOD_1 -I/home/luca/workspace/JavaCardPassport/Libs/Downloads/stasm4.1.0/Build1 -fPIC  -MMD -MP -MF "$@.d" -o ${OBJECTDIR}/_ext/e699828c/StasmBridgeLib.o /home/luca/workspace/JavaCardPassport/StasmBridgeLib/StasmBridgeLib.cpp

# Subprojects
.build-subprojects:

# Clean Targets
.clean-conf: ${CLEAN_SUBPROJECTS}
	${RM} -r ${CND_BUILDDIR}/${CND_CONF}
	${RM} dist/StasmBridgeLib.so

# Subprojects
.clean-subprojects:

# Enable dependency checking
.dep.inc: .depcheck-impl

include .dep.inc
