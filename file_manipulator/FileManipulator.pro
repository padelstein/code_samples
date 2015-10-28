#-------------------------------------------------
#
# Project created by QtCreator 2014-01-08T14:46:30
#
#-------------------------------------------------

QT       += core
QT       += widgets
QT       += gui

TARGET = Project4
CONFIG   += console
CONFIG   -= app_bundle

TEMPLATE = app

INCLUDEPATH += /opt/local/include
LIBS += -L/opt/local/lib
LIBS += -lboost_system-mt -lboost_filesystem-mt

SOURCES += main.cpp \
    mainwindow.cpp \
    directorywidget.cpp \
    filelistitem.cpp

HEADERS += \
    mainwindow.h \
    directorywidget.h \
    filelistitem.h
