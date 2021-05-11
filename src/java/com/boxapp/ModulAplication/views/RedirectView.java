package src.java.com.boxapp.ModulAplication.views;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;

@CssImport("./views/redirect/redirect-view.css")
@Route("")
public class RedirectView extends VerticalLayout {
    Button loginButton = new Button("Login");

    public RedirectView() {
        add(new H1("Welcome to Box Certification WebApp"));
        add(new H3("Cloud based application to certificate PDF files"));
        add(loginButton);

        loginButton.addClickListener( e -> {
//            getPage().setLocation("/myapp/logout.html");
            UI.getCurrent().getPage().setLocation("https://account.box.com/api/oauth2/authorize?client_id=dpn7gjta1hkff7gt2e2wsxp4m0rusf9k&response_type=code");
        });
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
        setHeightFull();
    }
}
