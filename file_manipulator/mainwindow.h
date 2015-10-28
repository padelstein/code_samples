#ifndef MAINWINDOW_H
#define MAINWINDOW_H

#include "directorywidget.h"
#include <QMainWindow>
#include <QCheckBox>

class MainWindow : public QMainWindow
{
    Q_OBJECT

    DirectoryWidget *left_directory;
    DirectoryWidget *right_directory;
    QCheckBox *keep_switched_checkbox;

public:
    MainWindow(QWidget *parent = 0);
    ~MainWindow();

public slots:
    void switchSelected();
    void switchRandom();
    void undoLast();
    void removeSelected();
    void closing();
};

#endif // MAINWINDOW_H
