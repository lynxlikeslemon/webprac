package ru.msu.cmc.webprac.frontend;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.msu.cmc.webprac.backend.DAO.ClientDAO;
import ru.msu.cmc.webprac.backend.DAO.TicketDAO;
import ru.msu.cmc.webprac.backend.DAO.impl.ClientDAOImpl;
import ru.msu.cmc.webprac.backend.DAO.impl.TicketDAOImpl;
import ru.msu.cmc.webprac.backend.entity.Client;
import ru.msu.cmc.webprac.backend.entity.Ticket;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
public class TicketListController {
    @Autowired
    private final TicketDAO ticketDAO = new TicketDAOImpl();
    @Autowired
    private final ClientDAO clientDAO = new ClientDAOImpl();

    private TicketDAO.Filter parseFilterParams(Integer clientId,
                                               String departureDate,
                                               String departureCity,
                                               String arrivalCity,
                                               Boolean ticketPaid) {
        TicketDAO.Filter.FilterBuilder builder = TicketDAO.getFilterBuilder();

        builder.clientId(clientId);

        if (!departureDate.isBlank()) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            builder.departureDate(LocalDate.parse(departureDate, formatter));
        }

        if (!departureCity.isBlank()) {
            builder.departureCity(departureCity);
        }

        if (!arrivalCity.isBlank()) {
            builder.arrivalCity(arrivalCity);
        }

        if (ticketPaid != null) {
            builder.isPaidFor(ticketPaid);
        }

        return builder.build();
    }

    @GetMapping("/tickets")
    public String getTicketList(@RequestParam() Integer clientId,
                                @RequestParam(required = false, defaultValue = "") String departureDate,
                                @RequestParam(required = false, defaultValue = "") String departureCity,
                                @RequestParam(required = false, defaultValue = "") String arrivalCity,
                                @RequestParam(required = false) Boolean ticketPaid,
                                Model model) {
        Client client = clientDAO.getById(clientId);

        if (client == null) {
            return "error";
        }

        TicketDAO.Filter filter = parseFilterParams(clientId, departureDate, departureCity, arrivalCity, ticketPaid);
        List<Ticket> tickets = ticketDAO.getTicketList(filter);

        model.addAttribute("client", client);
        model.addAttribute("ticketList", tickets);

        return "tickets";
    }
}
