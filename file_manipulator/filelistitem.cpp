#include "filelistitem.h"

#include <QListWidgetItem>
#include <QMessageBox>

FileListItem::FileListItem(QString _directory_path_name, QString _relative_file_name)
    : QListWidgetItem()
{

    file = new QFile(_directory_path_name + "/" + _relative_file_name);
    relative_file_name = _relative_file_name;
    directory_path_name = _directory_path_name;
    setText(_relative_file_name);

}

FileListItem::~FileListItem() { }

bool FileListItem::removeFile()
{
    return file->remove();
}

bool FileListItem::makeInvisible()
{
    relative_file_name = "." + relative_file_name;
    return file->rename(directory_path_name + "/" + relative_file_name);

}

bool FileListItem::copy(QString _destination_directory)
{
    return file->copy( _destination_directory + "/" + relative_file_name );
}

QString FileListItem::returnFileName() { return relative_file_name; }
