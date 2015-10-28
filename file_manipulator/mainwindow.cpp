#include "mainwindow.h"
#include "directorywidget.h"
#include "filelistitem.h"

#include <QVBoxLayout>
#include <QHBoxLayout>

#include <QMessageBox>
#include <QPushButton>
#include <QCheckBox>

using namespace Qt;

MainWindow::MainWindow(QWidget *parent)
    : QMainWindow(parent)
{
    QWidget *main = new QWidget();
    setCentralWidget(main);

    QHBoxLayout *mainLayout = new QHBoxLayout();
    main->setLayout(mainLayout);

    // the left directory widget
    left_directory = new DirectoryWidget();
    mainLayout->addWidget(left_directory);


    // the central control panel
    QVBoxLayout *controlPanelLayout = new QVBoxLayout();
    mainLayout->addLayout(controlPanelLayout);

    QPushButton *switch_button = new QPushButton("Switch");
    controlPanelLayout->addWidget(switch_button);
    connect(switch_button, SIGNAL(clicked()), this, SLOT(switchSelected()) );

    QPushButton *remove_selected_button = new QPushButton("Remove Selected");
    controlPanelLayout->addWidget(remove_selected_button);
    connect(remove_selected_button, SIGNAL(clicked()), this, SLOT(removeSelected()) );

    QPushButton *switch_random_button = new QPushButton("Randomize!");
    controlPanelLayout->addWidget(switch_random_button);
    connect(switch_random_button, SIGNAL(clicked()), this, SLOT(switchRandom()) );

    QPushButton *undo_last_switch_button = new QPushButton("Undo");
    controlPanelLayout->addWidget(undo_last_switch_button);
    connect(undo_last_switch_button, SIGNAL(clicked()), this, SLOT(undoLast()) );

    keep_switched_checkbox = new QCheckBox("Keep Files");
    controlPanelLayout->addWidget(keep_switched_checkbox);

    // the right directory widget
    right_directory = new DirectoryWidget();
    mainLayout->addWidget(right_directory);
}

MainWindow::~MainWindow() { }

void MainWindow::switchSelected()
{
    right_directory->addFiles(left_directory->returnSelectedFiles());
    left_directory->addFiles(right_directory->returnSelectedFiles());

    if (!keep_switched_checkbox->isChecked())
    {
        left_directory->removeSelectedFiles();
        right_directory->removeSelectedFiles();
    } else
    {
        left_directory->removeNoFiles();
        right_directory->removeNoFiles();
    }

    left_directory->displayFileList();
    right_directory->displayFileList();
}

void MainWindow::switchRandom()
{
    left_directory->selectRandom();
    right_directory->selectRandom();
    switchSelected();
}

void MainWindow::undoLast()
{
    left_directory->undoLastAdd();
    left_directory->undoLastDelete();

    right_directory->undoLastAdd();
    right_directory->undoLastDelete();
}

void MainWindow::removeSelected()
{
    left_directory->removeSelectedFiles();
    right_directory->removeSelectedFiles();

    left_directory->addNoFiles();
    right_directory->addNoFiles();

    left_directory->displayFileList();
    right_directory->displayFileList();
}

void MainWindow::closing()
{
    left_directory->removeAllHiddenFiles();
    right_directory->removeAllHiddenFiles();
}
