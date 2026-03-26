package ru.msu.cmc.webprac.backend.DAO;

import lombok.Builder;
import lombok.Getter;
import ru.msu.cmc.webprac.backend.entity.Flight;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

public interface FlightDAO extends BaseDAO<Flight, String> {
    List<Flight> getFlightList(Filter filter);

    @Builder
    @Getter
    public class Filter {
        private String companyName;
        private String departureCity;
        private String arrivalCity;
        private LocalDate departureDate;
        private LocalDate arrivalDate;
        private Double maxPrice;
        private Boolean purchasable;

        private String notNull(Object o) {
            if (o == null) {
                return "null";
            }

            return o.toString();
        }
        @Override
        public String toString() {
            return  "departure city: " + notNull(departureCity) + "\n" +
                    "arrival city: " + notNull(arrivalCity) + "\n" +
                    "departure date: " + notNull(departureDate) + "\n" +
                    "arrival date: " + notNull(arrivalDate) + "\n" +
                    "company: " + notNull(companyName) + "\n" +
                    "max price: " + notNull(maxPrice) + "\n" +
                    "purchasable: " + notNull(purchasable) ;
        }
    }

    static Filter.FilterBuilder getFilterBuilder() {
        return FlightDAO.Filter.builder();
    }
}
