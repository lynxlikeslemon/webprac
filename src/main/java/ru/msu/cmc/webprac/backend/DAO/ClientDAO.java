package ru.msu.cmc.webprac.backend.DAO;

import lombok.Builder;
import lombok.Getter;
import ru.msu.cmc.webprac.backend.entity.Client;

import java.util.List;

public interface ClientDAO extends BaseDAO<Client, Integer> {
    Client getByPhoneNumber(String phoneNumber);

    List<Client> getClientList(Filter filter);

    @Builder
    @Getter
    public class Filter {
        private String firstName;
        private String lastName;
        private String fathersName;
        private String phoneNumber;
        private String companyName;
        private String flightId;
        private Boolean ticketPaid;

        private String notNull(Object o) {
            if (o == null) {
                return "null";
            }

            return o.toString();
        }
        @Override
        public String toString() {
            return  "first name: " + notNull(firstName) + "\n" +
                    "last name: " + notNull(lastName) + "\n" +
                    "fathers name: " + notNull(fathersName) + "\n" +
                    "phone number: " + notNull(phoneNumber) + "\n" +
                    "company: " + notNull(companyName) + "\n" +
                    "flight: " + notNull(flightId) + "\n" +
                    "ticket Paid: " + notNull(ticketPaid);
        }
    }

    static Filter.FilterBuilder getFilterBuilder() {
        return Filter.builder();
    }
}
