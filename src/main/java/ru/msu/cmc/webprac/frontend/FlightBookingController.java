package ru.msu.cmc.webprac.frontend;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.msu.cmc.webprac.backend.DAO.ClientDAO;
import ru.msu.cmc.webprac.backend.DAO.FlightDAO;
import ru.msu.cmc.webprac.backend.DAO.TicketDAO;
import ru.msu.cmc.webprac.backend.DAO.impl.ClientDAOImpl;
import ru.msu.cmc.webprac.backend.DAO.impl.FlightDAOImpl;
import ru.msu.cmc.webprac.backend.DAO.impl.TicketDAOImpl;
import ru.msu.cmc.webprac.backend.entity.Client;
import ru.msu.cmc.webprac.backend.entity.Flight;
import ru.msu.cmc.webprac.backend.entity.Ticket;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Controller
public class FlightBookingController {
    @Autowired
    private final ClientDAO clientDAO = new ClientDAOImpl();
    @Autowired
    private final FlightDAO flightDAO = new FlightDAOImpl();
    @Autowired
    private final TicketDAO ticketDAO = new TicketDAOImpl();

    @GetMapping("flight_booking")
    public String getClient(@RequestParam(required = false) String clientPhoneNumber,
                            @RequestParam String flightId,
                            Model model) {
        if (clientPhoneNumber != null) {
            Client client = clientDAO.getByPhoneNumber(clientPhoneNumber);
            if (client == null) {
                model.addAttribute("clientNotFound", true);
            } else {
                model.addAttribute("clientNotFound", false);
            }
            model.addAttribute("client", client);
        } else {
            model.addAttribute("client", null);
            model.addAttribute("clientNotFound", false);
        }

        Flight flight = flightDAO.getById(flightId);
        if (flight == null) {
            return "error";
        }

        model.addAttribute("flight", flight);

        return "flight_booking";
    }

    @PostMapping("flight_booking")
    public String bookTicket(@RequestParam Integer clientId,
                            @RequestParam String flightId) {
        Flight flight = flightDAO.getById(flightId);
        Client client = clientDAO.getById(clientId);

        Ticket ticket = new Ticket();
        ticket.setFlight(flight);
        ticket.setClient(client);
        ticket.setPrice(flight.getPrice());
        ticket.setIsPaidFor(false);
        ticket.setBookingTime(new Timestamp(System.currentTimeMillis()));
        flight.setPlacesTaken(flight.getPlacesTaken() + 1);
        flightDAO.update(flight);
        ticketDAO.save(ticket);

        return "redirect:/ticket?ticketId=" + ticket.getId();
    }
}
