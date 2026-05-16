package ru.msu.cmc.webprac.frontend;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.msu.cmc.webprac.backend.DAO.FlightDAO;
import ru.msu.cmc.webprac.backend.DAO.impl.FlightDAOImpl;
import ru.msu.cmc.webprac.backend.entity.Flight;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
public class FlightListController {
    @Autowired
    private final FlightDAO flightDAO = new FlightDAOImpl();

    private FlightDAO.Filter parseFilterParams(String companyName,
                                               String departureCity,
                                               String arrivalCity,
                                               String departureDate,
                                               String arrivalDate,
                                               BigDecimal maxPrice,
                                               Boolean purchasable,
                                               Model model) {
        FlightDAO.Filter.FilterBuilder filterBuilder = FlightDAO.getFilterBuilder();

        if (!companyName.isBlank()) {
            filterBuilder.companyName(companyName);
        }

        if (!departureCity.isBlank()) {
            filterBuilder.departureCity(departureCity);
        }

        if (!arrivalCity.isBlank()) {
            filterBuilder.arrivalCity(arrivalCity);
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        if (!departureDate.isBlank()) {
            filterBuilder.departureDate(LocalDate.parse(departureDate, formatter));
        }

        if (!arrivalDate.isBlank()) {
            filterBuilder.arrivalDate(LocalDate.parse(arrivalDate, formatter));
        }

        if (maxPrice != null) {
            if (maxPrice.compareTo(BigDecimal.ZERO) < 0) {
                model.addAttribute("error_msg", "Отрицательное значение минимальной цены");
                return null;
            }

            filterBuilder.maxPrice(maxPrice);
        }

        if (purchasable != null) {
            filterBuilder.purchasable(purchasable);
        }

        return filterBuilder.build();
    }

    @GetMapping("/flights")
    public String getFlightList(@RequestParam(required = false, defaultValue = "") String companyName,
                                @RequestParam(required = false, defaultValue = "") String departureCity,
                                @RequestParam(required = false, defaultValue = "") String arrivalCity,
                                @RequestParam(required = false, defaultValue = "") String departureDate,
                                @RequestParam(required = false, defaultValue = "") String arrivalDate,
                                @RequestParam(required = false) BigDecimal maxPrice,
                                @RequestParam(required = false) Boolean purchasable,
                                Model model) {
        FlightDAO.Filter filter = parseFilterParams(companyName, departureCity, arrivalCity, departureDate, arrivalDate, maxPrice, purchasable, model);

        if (filter == null) {
            return "error";
        }

        List<Flight> flights = flightDAO.getFlightList(filter);
        model.addAttribute("flightList", flights);
        return "flights";
    }
}
