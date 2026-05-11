package ru.msu.cmc.webprac.frontend;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.msu.cmc.webprac.backend.DAO.ClientDAO;
import ru.msu.cmc.webprac.backend.DAO.impl.ClientDAOImpl;
import ru.msu.cmc.webprac.backend.entity.Client;
import java.util.List;

@Controller
public class ClientListController {
    @Autowired
    private final ClientDAO clientDAO = new ClientDAOImpl();

    private ClientDAO.Filter parseFilterParams(String firstName,
                                               String lastName,
                                               String fathersName,
                                               String phoneNumber,
                                               String companyName,
                                               String flightId,
                                               Boolean ticketPaid) {
        ClientDAO.Filter.FilterBuilder builder = ClientDAO.getFilterBuilder();

        if (!firstName.isBlank()) {
            builder.firstName(firstName);
        }

        if (!lastName.isBlank()) {
            builder.lastName(lastName);
        }

        if (!fathersName.isBlank()) {
            builder.fathersName(fathersName);
        }

        if (!phoneNumber.isBlank()) {
            builder.phoneNumber(phoneNumber);
        }

        if (!companyName.isBlank()) {
            builder.companyName(companyName);
        }

        if (!flightId.isBlank()) {
            builder.flightId(flightId);
        }

        if (ticketPaid != null) {
            builder.ticketPaid(ticketPaid);
        }

        return builder.build();
    }

    @GetMapping("/clients")
    public String getClientList(@RequestParam(required = false, defaultValue = "") String firstName,
                                @RequestParam(required = false, defaultValue = "") String lastName,
                                @RequestParam(required = false, defaultValue = "") String fathersName,
                                @RequestParam(required = false, defaultValue = "") String phoneNumber,
                                @RequestParam(required = false, defaultValue = "") String companyName,
                                @RequestParam(required = false, defaultValue = "") String flightId,
                                @RequestParam(required = false) Boolean ticketPaid,
                                Model model) {
        ClientDAO.Filter filter = parseFilterParams(firstName, lastName, fathersName, phoneNumber, companyName, flightId, ticketPaid);

        if (filter == null) {
            return "error";
        }

        List<Client> clients = clientDAO.getClientList(filter);
        model.addAttribute("clientList", clients);
        return "clients";
    }
}
