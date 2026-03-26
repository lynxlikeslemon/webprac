package ru.msu.cmc.webprac.backend.DAO;

import lombok.Builder;
import lombok.Getter;
import ru.msu.cmc.webprac.backend.entity.Ticket;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

public interface TicketDAO extends BaseDAO<Ticket, Integer> {
    List<Ticket> getTicketList(Filter filter);

    @Builder
    @Getter
    public class Filter {
        private Integer clientId;
        private LocalDate departureDate;
        private String departureCity;
        private String arrivalCity;
        private Boolean isPaidFor;

        private String notNull(Object o) {
            if (o == null) {
                return "null";
            }

            return o.toString();
        }

        @Override
        public String toString() {
            return  "client id: " + notNull(clientId) + "\n" +
                    "departure city: " + notNull(departureCity) + "\n" +
                    "arrival city: " + notNull(arrivalCity) + "\n" +
                    "departure date: " + notNull(departureDate) + "\n" +
                    "paid for: " + notNull(isPaidFor);
        }
    }

    static Filter.FilterBuilder getFilterBuilder() {
        return Filter.builder();
    }
}
