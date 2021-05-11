package src.java.com.boxapp.ModulAplication.views.download;

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

@Route(value = "download", layout = MainView.class)
@PageTitle("Download")
@CssImport("./views/download/download-view.css")
public class DownloadView extends Div {

    public DownloadView() {
        addClassName("download-view");

        ArrayList<FileToManipulate> filesInInput = new ArrayList<>();
        Grid<FileToManipulate> inputGrid = new Grid(FileToManipulate.class);

        BoxFolder inputFolder = new BoxFolder(IDsAndTokens.getApi(), IDsAndTokens.folderInputID);

        for (BoxItem.Info itemInfo : inputFolder) {
            FileToManipulate file = new FileToManipulate(itemInfo.getName(), itemInfo.getID());
            filesInInput.add(file);
        }

        inputGrid.setItems(filesInInput);
        inputGrid.addComponentColumn(item -> CreateButtons.createDownloadButton(item, IDsAndTokens.folderInputID, IDsAndTokens.downloadToFolder))
                .setHeader("Download");
        inputGrid.addComponentColumn(item -> CreateButtons.createDeleteButton(item))
                .setHeader("Delete");

        add(inputGrid);

    }



}