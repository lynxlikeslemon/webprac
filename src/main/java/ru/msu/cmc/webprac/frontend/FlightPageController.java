package ru.msu.cmc.webprac.frontend;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.msu.cmc.webprac.backend.DAO.FlightDAO;
import ru.msu.cmc.webprac.backend.DAO.impl.CompanyDAOImpl;
import ru.msu.cmc.webprac.backend.DAO.impl.FlightDAOImpl;
import ru.msu.cmc.webprac.backend.entity.Flight;

@Controller
public class FlightPageController {
    @Autowired
    private final FlightDAO flightDAO = new FlightDAOImpl();

    @GetMapping("/flight")
    public String getFlightList(@RequestParam String flightId,
                                Model model) {
        if (flightId == null) {
            return "error";
        }

        Flight flight = flightDAO.getById(flightId);
        model.addAttribute("flight", flight);
        return "flight";
    }

    @PostMapping("flight")
    public String deleteFlight(@RequestParam String flightId) {
        Flight flight = flightDAO.getById(flightId);
        flightDAO.delete(flight);
        return "redirect:/flights";
    }
}
