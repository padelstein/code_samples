#ifndef DIRECTORYWIDGET_H
#define DIRECTORYWIDGET_H

#include "filelistitem.h"

#include <QDir>
#include <QWidget>
#include <QLineEdit>
#include <QListWidget>


class DirectoryWidget : public QWidget {

    Q_OBJECT

    QLineEdit *directory_path_input;
    QListWidget *file_list;

    QDir directory;
    std::list<std::list<QString> > removed;
    std::list<std::list<QString> > added;

public:
    DirectoryWidget();
    virtual ~DirectoryWidget();

    std::list<FileListItem*> returnSelectedFiles();
    QString returnPathName();

    void addFiles(std::list<FileListItem*> files_to_add);
    void addNoFiles();

    void selectRandom();

    void undoLastAdd();
    void undoLastDelete();

    void removeSelectedFiles();
    void removeNoFiles();
    void removeAllHiddenFiles();

public slots:
    void displayFileList();
    void openDirectory();

};


#endif
