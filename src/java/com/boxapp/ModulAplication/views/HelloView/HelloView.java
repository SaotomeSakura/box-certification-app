package src.java.com.boxapp.ModulAplication.views.HelloView;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.*;
import src.java.com.boxapp.ModulAplication.fileManipulation.IDsAndTokens;
import src.java.com.boxapp.ModulExspiration.ExpiryCheck;

import java.util.List;
import java.util.Map;

@Route("hello")
public class HelloView extends VerticalLayout implements HasUrlParameter<String> {
         Button enterApplication = new Button("Enter application");

     HelloView() {
         add(new H1("Successfully logged in!"));
         add(enterApplication);

         enterApplication.addClickListener( e -> {
             UI.getCurrent().getPage().setLocation("http://localhost:8080/download");
         });
         setAlignItems(Alignment.CENTER);
         setJustifyContentMode(JustifyContentMode.CENTER);
         setHeightFull();
         ExpiryCheck.timingExpiry();
     }

    @Override
    public void setParameter(BeforeEvent event, @OptionalParameter String parameter) {

        Location location = event.getLocation();
        QueryParameters queryParameters = location.getQueryParameters();

        Map<String, List<String>> parametersMap = queryParameters.getParameters();
        for (String name : parametersMap.keySet()) {
            String key = name;
            List<String> values = parametersMap.get(name);
            IDsAndTokens.setApiConnection(values.get(0));
            System.out.println(key + " " + IDsAndTokens.getApi());
        }
    }
}
