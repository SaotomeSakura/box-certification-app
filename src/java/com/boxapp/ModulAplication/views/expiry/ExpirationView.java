package src.java.com.boxapp.ModulAplication.views.expiry;

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

@Route(value = "expiration", layout = MainView.class)
@PageTitle("Resign")
@CssImport("./views/expiration/expiration-view.css")
public class ExpirationView extends Div{

    public ExpirationView() {
        addClassName("expiration-view");

        ArrayList<FileToManipulate> filesToUpload = new ArrayList<>();
        Grid<FileToManipulate> uploadGrid = new Grid(FileToManipulate.class);

        File folder = new File(System.getProperty("user.dir") + "\\Expiry");
        File[] listOfFiles = folder.listFiles();


        for (int i = 0; i < listOfFiles.length; i++) {
            if (listOfFiles[i].isFile()) {
                FileToManipulate file = new FileToManipulate(listOfFiles[i].getName(), String.valueOf(i+1));
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
        uploadGrid.addComponentColumn(item -> CreateButtons.createResignAndUploadButton(item))
                .setHeader("Resign");
//        uploadGrid.addComponentColumn(item -> createExtendButton(item))
//                .setHeader("Extend");
//        uploadGrid.addComponentColumn(item -> createValidateButton(item))
//                .setHeader("Validate");



        add(uploadGrid);

    }






}
