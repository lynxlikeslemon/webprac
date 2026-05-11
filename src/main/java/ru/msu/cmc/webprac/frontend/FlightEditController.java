package ru.msu.cmc.webprac.frontend;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.msu.cmc.webprac.backend.DAO.AirportDAO;
import ru.msu.cmc.webprac.backend.DAO.CompanyDAO;
import ru.msu.cmc.webprac.backend.DAO.FlightDAO;
import ru.msu.cmc.webprac.backend.DAO.impl.AirportDAOImpl;
import ru.msu.cmc.webprac.backend.DAO.impl.CompanyDAOImpl;
import ru.msu.cmc.webprac.backend.DAO.impl.FlightDAOImpl;
import ru.msu.cmc.webprac.backend.entity.Airport;
import ru.msu.cmc.webprac.backend.entity.Company;
import ru.msu.cmc.webprac.backend.entity.Flight;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
public class FlightEditController {
    @Autowired
    private final FlightDAO flightDAO = new FlightDAOImpl();
    @Autowired
    private final CompanyDAO companyDAO = new CompanyDAOImpl();
    @Autowired
    private final AirportDAO airportDAO = new AirportDAOImpl();

    @PostMapping("flight_creation")
    public String createFlight(@RequestParam String flightId,
                               @RequestParam Integer companyId,
                               @RequestParam String departureAirportId,
                               @RequestParam String arrivalAirportId,
                               @RequestParam String departureDate,
                               @RequestParam String arrivalDate,
                               @RequestParam String departureTime,
                               @RequestParam String arrivalTime,
                               @RequestParam BigDecimal price,
                               @RequestParam Integer places) {
        Flight flight = flightDAO.getById(flightId);

        if (flight != null) {
            return "error";
        }

        Company company = companyDAO.getById(companyId);
        Airport departureAirport = airportDAO.getById(departureAirportId);
        Airport arrivalAirport = airportDAO.getById(arrivalAirportId);

        if (company == null || departureAirport == null || arrivalAirport == null) {
            return "error";
        }

        flight = new Flight();
        flight.setId(flightId);
        flight.setCompany(company);
        flight.setArrivalAirport(arrivalAirport);
        flight.setDepartureAirport(departureAirport);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate departureDateParsed = LocalDate.parse(departureDate, formatter);
        LocalTime departureTimeParsed = LocalTime.parse(departureTime);
        flight.setDepartureTime(Timestamp.valueOf(LocalDateTime.of(departureDateParsed, departureTimeParsed)));

        LocalDate arrivalDateParsed = LocalDate.parse(arrivalDate, formatter);
        LocalTime arrivalTimeParsed = LocalTime.parse(arrivalTime);
        flight.setArrivalTime(Timestamp.valueOf(LocalDateTime.of(arrivalDateParsed, arrivalTimeParsed)));

        flight.setPrice(price);
        flight.setPlacesTotal(places);
        flight.setPlacesTaken(0);

        flightDAO.save(flight);

        return "redirect:/flight?flightId=" + flightId;
    }


    @PostMapping("flight_edit")
    public String editFlight(@RequestParam String flightId,
                             @RequestParam String newFlightId,
                             @RequestParam Integer companyId,
                             @RequestParam String departureAirportId,
                             @RequestParam String arrivalAirportId,
                             @RequestParam String departureDate,
                             @RequestParam String arrivalDate,
                             @RequestParam String departureTime,
                             @RequestParam String arrivalTime,
                             @RequestParam BigDecimal price,
                             @RequestParam Integer places) {
        Flight flight = flightDAO.getById(flightId);

        if (flight == null) {
            return "error";
        }

        if (!newFlightId.equals(flightId)) {
            Flight newFlight = flightDAO.getById(flightId);

            if (newFlight != null) {
                return "error";
            }
        }

        Company company = companyDAO.getById(companyId);
        Airport departureAirport = airportDAO.getById(departureAirportId);
        Airport arrivalAirport = airportDAO.getById(arrivalAirportId);

        if (company == null || departureAirport == null || arrivalAirport == null) {
            return "error";
        }

        flight.setId(flightId);
        flight.setCompany(company);
        flight.setArrivalAirport(arrivalAirport);
        flight.setDepartureAirport(departureAirport);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate departureDateParsed = LocalDate.parse(departureDate, formatter);
        LocalTime departureTimeParsed = LocalTime.parse(departureTime);
        flight.setDepartureTime(Timestamp.valueOf(LocalDateTime.of(departureDateParsed, departureTimeParsed)));

        LocalDate arrivalDateParsed = LocalDate.parse(arrivalDate, formatter);
        LocalTime arrivalTimeParsed = LocalTime.parse(arrivalTime);
        flight.setArrivalTime(Timestamp.valueOf(LocalDateTime.of(arrivalDateParsed, arrivalTimeParsed)));

        flight.setPrice(price);
        flight.setPlacesTotal(places);

        flightDAO.update(flight);

        return "redirect:/flight?flightId=" + flightId;
    }

    @GetMapping("flight_creation")
    public String openFlightCreatePage(Model model) {
        List<Company> companies = companyDAO.getAll().stream().toList();
        List<Airport> airports = airportDAO.getAll().stream().toList();
        model.addAttribute("companyList", companies);
        model.addAttribute("airportList", airports);
        return "flight_creation";
    }

    @GetMapping("flight_edit")
    public String openFlightEditPage(@RequestParam String flightId, Model model) {
        Flight flight = flightDAO.getById(flightId);
        if (flight == null) {
            return "error";
        }

        model.addAttribute("flight", flight);

        List<Company> companies = companyDAO.getAll().stream().toList();
        List<Airport> airports = airportDAO.getAll().stream().toList();
        model.addAttribute("companyList", companies);
        model.addAttribute("airportList", airports);

        return "flight_edit";
    }
}
