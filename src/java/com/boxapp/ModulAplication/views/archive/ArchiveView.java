package src.java.com.boxapp.ModulAplication.views.archive;

import com.box.sdk.BoxFolder;
import com.box.sdk.BoxItem;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import src.java.com.boxapp.ModulAplication.GridManipulation.CreateButtons;
import src.java.com.boxapp.ModulAplication.GridManipulation.FileToManipulate;
import src.java.com.boxapp.ModulAplication.fileManipulation.IDsAndTokens;
import src.java.com.boxapp.ModulAplication.views.main.MainView;

import java.util.ArrayList;

@Route(value = "archive", layout = MainView.class)
@PageTitle("Archive")
@CssImport("./views/archive/archive-view.css")
public class ArchiveView extends Div  {

    public ArchiveView() {
        addClassName("archive-view");

        ArrayList<FileToManipulate> filesinArchive = new ArrayList<>();
        Grid<FileToManipulate> archiveGrid = new Grid(FileToManipulate.class);

        BoxFolder inputFolder = new BoxFolder(IDsAndTokens.getApi(), IDsAndTokens.folderArchiveID);

        for (BoxItem.Info itemInfo : inputFolder) {
            FileToManipulate file = new FileToManipulate(itemInfo.getName(), itemInfo.getID());
            filesinArchive.add(file);
        }

        archiveGrid.setItems(filesinArchive);
        archiveGrid.addComponentColumn(item -> CreateButtons.createDownloadButton(item, IDsAndTokens.folderArchiveID, IDsAndTokens.expiryFolder))
                .setHeader("Download");
        archiveGrid.addComponentColumn(item -> CreateButtons.createDeleteButton(item))
                .setHeader("Delete");

        add(archiveGrid);

    }




}

