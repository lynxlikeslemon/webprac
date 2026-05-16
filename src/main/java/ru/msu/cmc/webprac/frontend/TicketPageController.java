package ru.msu.cmc.webprac.frontend;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.msu.cmc.webprac.backend.DAO.FlightDAO;
import ru.msu.cmc.webprac.backend.DAO.TicketDAO;
import ru.msu.cmc.webprac.backend.DAO.impl.FlightDAOImpl;
import ru.msu.cmc.webprac.backend.DAO.impl.TicketDAOImpl;
import ru.msu.cmc.webprac.backend.entity.Client;
import ru.msu.cmc.webprac.backend.entity.Flight;
import ru.msu.cmc.webprac.backend.entity.Ticket;

@Controller
public class TicketPageController {
    @Autowired
    private final TicketDAO ticketDAO = new TicketDAOImpl();
    @Autowired
    private final FlightDAO flightDAO = new FlightDAOImpl();

    @GetMapping("/ticket")
    public String getTicket(@RequestParam() Integer ticketId, Model model) {
        Ticket ticket = ticketDAO.getById(ticketId);
        model.addAttribute("ticket", ticket);

        return "ticket";
    }

    @PostMapping("/ticket")
    public String deleteTicket(@RequestParam Integer ticketId) {
        Ticket ticket = ticketDAO.getById(ticketId);
        Client client = ticket.getClient();
        Flight flight = ticket.getFlight();
        flight.setPlacesTaken(flight.getPlacesTaken() - 1);
        ticketDAO.delete(ticket);
        flightDAO.update(flight);

        return "redirect:/tickets?clientId=" + client.getId();
    }
}
