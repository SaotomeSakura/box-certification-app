package src.java.com.boxapp.ModulAplication.views.upload;

import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import src.java.com.boxapp.ModulAplication.GridManipulation.CreateButtons;
import src.java.com.boxapp.ModulAplication.GridManipulation.FileToManipulate;
import src.java.com.boxapp.ModulAplication.fileManipulation.IDsAndTokens;
import src.java.com.boxapp.ModulAplication.views.main.MainView;

import java.io.File;
import java.util.ArrayList;

@Route(value = "upload", layout = MainView.class)
@PageTitle("Upload/Sign")
@CssImport("./views/upload/upload-view.css")
//@RouteAlias(value = "", layout = MainView.class)
public class UploadView extends Div {


    public UploadView() {
        addClassName("upload-view");

        ArrayList<FileToManipulate> filesToUpload = new ArrayList<>();
        Grid<FileToManipulate> uploadGrid = new Grid(FileToManipulate.class);

        File folder = new File(System.getProperty("user.dir") + "\\DownloadTo");
        File[] listOfFiles = folder.listFiles();

//        HashMap<FilesToManipulate, Boolean> validyMap = new HashMap<>();

        for (int i = 0; i < listOfFiles.length; i++) {
            if (listOfFiles[i].isFile()) {
                FileToManipulate file = new FileToManipulate(listOfFiles[i].getName(), String.valueOf(i+1));
//                validyMap.put(file, hasValidSignature(file));
                filesToUpload.add(file);
            } else if (listOfFiles[i].isDirectory()) {
                System.out.println("Directory " + listOfFiles[i].getName());
            }
        }

        uploadGrid.setItems(filesToUpload);
        uploadGrid.addComponentColumn(item -> CreateButtons.createUploadButton(item, IDsAndTokens.folderArchiveID))
                .setHeader("Upload");
        uploadGrid.addComponentColumn(item -> CreateButtons.createLocalDeleteButton(item))
                .setHeader("Delete");
        uploadGrid.addComponentColumn(item -> CreateButtons.createValidateButton(item))
                .setHeader("Sign/Extend");
//        uploadGrid.addComponentColumn(item -> createSignButton(item))
//                .setHeader("Sign");
//        uploadGrid.addComponentColumn(item -> createExtendButton(item))
//                .setHeader("Extend");

        add(uploadGrid);
    }
}
