#include "directorywidget.h"
#include "filelistitem.h"

#include <stdlib.h>
#include <time.h>

#include <QVBoxLayout>

#include <QDir>
#include <QFileDialog>
#include <QMessageBox>

#include <QLineEdit>
#include <QPushButton>
#include <QListWidget>


DirectoryWidget::DirectoryWidget()
    : QWidget()
{
    QVBoxLayout *layout = new QVBoxLayout();
    setLayout(layout);

    QPushButton *open_file_button = new QPushButton("Choose Directory");
    layout->addWidget(open_file_button);
    connect(open_file_button, SIGNAL(clicked()), this, SLOT(openDirectory()));

    directory_path_input = new QLineEdit("Enter Directory");
    layout->addWidget(directory_path_input);

    QPushButton *show_files_button = new QPushButton("Display Files");
    layout->addWidget(show_files_button);
    connect(show_files_button, SIGNAL(clicked()), this, SLOT(displayFileList()) );

    file_list = new QListWidget();
    file_list->setSelectionMode(QAbstractItemView::MultiSelection);
    layout->addWidget(file_list);

    directory = QDir::root();
    directory.setFilter(QDir::Files | QDir::NoDotAndDotDot | QDir::NoSymLinks);
    directory.setSorting(QDir::Type | QDir::Reversed);
}

DirectoryWidget::~DirectoryWidget() { }

std::list<FileListItem*> DirectoryWidget::returnSelectedFiles()
{
    std::list<FileListItem*> return_list;

    QList<QListWidgetItem*> selected_files = file_list->selectedItems();

    for (int i=0; i < selected_files.size(); i++)
    {
        return_list.push_back( dynamic_cast<FileListItem*> (selected_files.at(i)) );
    }

    return return_list;
}

QString DirectoryWidget::returnPathName()
{
    return directory.absolutePath();
}

void DirectoryWidget::addFiles(std::list<FileListItem*> _files_to_add)
{
    std::list<QString> added_files;

    for (std::list<FileListItem*>::iterator file_iterator = _files_to_add.begin();
                                            file_iterator != _files_to_add.end(); file_iterator++)
    {
        (*file_iterator)->copy( directory.absolutePath() );
        added_files.push_back( (*file_iterator)->returnFileName() );
    }

    added.push_back(added_files);
}

void DirectoryWidget::addNoFiles()
{
   std::list<QString> added_files;
   added.push_back(added_files);
}

void DirectoryWidget::selectRandom()
{
    srand( time(NULL) );

    int count_to_be_selected = rand() % (file_list->count() + 1);

    for (int i=0; i < count_to_be_selected; i++)
    {
        int rand_row = rand() % file_list->count();
        file_list->item(rand_row)->setSelected(true);
    }
}

void DirectoryWidget::undoLastDelete()
{
    if (removed.empty()) { return; }

    std::list<QString> files_to_replace = removed.front();
    removed.pop_front();

    for (std::list<QString>::iterator file_iterator = files_to_replace.begin();
                                      file_iterator != files_to_replace.end(); file_iterator++)
    {
        QString file_name = (*file_iterator);
        file_name.remove(0,1);

        QFile::rename(directory.absolutePath() + "/" + "." + file_name,
                      directory.absolutePath() + "/" + file_name);
    }
    displayFileList();
}

void DirectoryWidget::undoLastAdd()
{
    if (added.empty()) { return; }

    std::list<QString> files_to_remove = added.front();
    added.pop_front();

    for (std::list<QString>::iterator file_iterator = files_to_remove.begin();
                                      file_iterator != files_to_remove.end(); file_iterator++)
    {
        QString file_name = (*file_iterator);

        QFile::rename(directory.absolutePath() + "/" +  file_name,
                      directory.absolutePath() + "/" + "." + file_name);
    }
    displayFileList();
}

void DirectoryWidget::displayFileList()
{
    file_list->clear();
    QString input_path = directory_path_input->text();

    if ( QDir(input_path).exists() )
    {
        directory.cd(input_path);
        QStringList files = directory.entryList();

        for (int i=0; i < files.size(); i++)
        {
            file_list->addItem( new FileListItem(directory.absolutePath(), files.at(i)) );
        }
    } else {
        QMessageBox::warning(this, "Warning", "Not a valid directory.");
    }
}

// this doesn't actually remove the files it just makes them invisible
// we need to actually remove them before application quits
void DirectoryWidget::removeSelectedFiles()
{
    QList<QListWidgetItem*> selected_files = file_list->selectedItems();
    std::list<QString> removed_files;

    for (int i = 0; i < selected_files.size(); i++ )
    {
        if ( !dynamic_cast<FileListItem*>( selected_files.at(i) )->makeInvisible() )
        {
            dynamic_cast<FileListItem*>( selected_files.at(i) )->removeFile();
        }
        removed_files.push_back( dynamic_cast<FileListItem*>(selected_files.at(i))->returnFileName() );
    }
    removed.push_back(removed_files);
    displayFileList();
}

void DirectoryWidget::removeAllHiddenFiles()
{
    for (std::list<std::list<QString> >::iterator file_list_iterator = removed.begin();
                                                  file_list_iterator != removed.end(); file_list_iterator++)
    {
        for (std::list<QString>::iterator file_iterator = (*file_list_iterator).begin();
                                          file_iterator != (*file_list_iterator).end(); file_iterator++)
        {
            QString file_name = (*file_iterator);
            directory.remove(directory.absolutePath() + "/" + file_name);
        }
    }
}

void DirectoryWidget::removeNoFiles()
{
    std::list<QString> removed_files;
    removed.push_back(removed_files);
}

void DirectoryWidget::openDirectory()
{
    directory_path_input->setText( QFileDialog::getExistingDirectory(this, tr("Choose Directory") ));
    removeAllHiddenFiles();
    removed.clear();
    added.clear();
    displayFileList();
}



