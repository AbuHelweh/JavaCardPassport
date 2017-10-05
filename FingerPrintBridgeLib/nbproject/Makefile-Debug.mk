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
	${OBJECTDIR}/FingerPrintBridgeLib.o


# C Compiler Flags
CFLAGS=-shared

# CC Compiler Flags
CCFLAGS=
CXXFLAGS=

# Fortran Compiler Flags
FFLAGS=

# Assembler Flags
ASFLAGS=

# Link Libraries and Options
LDLIBSOPTIONS=

# Build Targets
.build-conf: ${BUILD_SUBPROJECTS}
	"${MAKE}"  -f nbproject/Makefile-${CND_CONF}.mk dist/FingerPrintBridgeLib.so

dist/FingerPrintBridgeLib.so: ${OBJECTFILES}
	${MKDIR} -p dist
	${LINK.c} -o dist/FingerPrintBridgeLib.so ${OBJECTFILES} ${LDLIBSOPTIONS} -lfprint -shared -fPIC

${OBJECTDIR}/FingerPrintBridgeLib.o: FingerPrintBridgeLib.c 
	${MKDIR} -p ${OBJECTDIR}
	${RM} "$@.d"
	$(COMPILE.c) -g -I/home/luca/Documents/TCC/jdk1.8.0_131/include -I/home/luca/Documents/TCC/jdk1.8.0_131/include/linux -I/home/luca/TCC/libfprint-0.7.0/libfprint -I/usr/include/libusb-1.0 -I/home/luca/TCC/libfprint-0.7.0 -I/usr/include/glib-2.0 -I/usr/lib/x86_64-linux-gnu/glib-2.0/include -fPIC  -MMD -MP -MF "$@.d" -o ${OBJECTDIR}/FingerPrintBridgeLib.o FingerPrintBridgeLib.c

# Subprojects
.build-subprojects:

# Clean Targets
.clean-conf: ${CLEAN_SUBPROJECTS}
	${RM} -r ${CND_BUILDDIR}/${CND_CONF}
	${RM} dist/FingerPrintBridgeLib.so

# Subprojects
.clean-subprojects:

# Enable dependency checking
.dep.inc: .depcheck-impl

include .dep.inc
