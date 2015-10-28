#ifndef FILELISTITEM_H
#define FILELISTITEM_H

#include <QListWidget>
#include <QDir>

class FileListItem:public QListWidgetItem {

    QFile *file;
    QString relative_file_name;
    QString directory_path_name;

public:
    FileListItem(QString directory_path_name,
                 QString relative_file_name);
    virtual ~FileListItem();

    bool removeFile();
    bool makeInvisible();
    bool copy(QString destination_directory);
    QString returnFileName();

public slots:

};

#endif
