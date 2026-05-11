package ru.msu.cmc.webprac.frontend;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.msu.cmc.webprac.backend.DAO.BonusCardDAO;
import ru.msu.cmc.webprac.backend.DAO.ClientDAO;
import ru.msu.cmc.webprac.backend.DAO.CompanyDAO;
import ru.msu.cmc.webprac.backend.DAO.impl.BonusCardDAOImpl;
import ru.msu.cmc.webprac.backend.DAO.impl.ClientDAOImpl;
import ru.msu.cmc.webprac.backend.DAO.impl.CompanyDAOImpl;
import ru.msu.cmc.webprac.backend.entity.BonusCard;
import ru.msu.cmc.webprac.backend.entity.Client;
import ru.msu.cmc.webprac.backend.entity.Company;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
public class ClientEditController {
    @Autowired
    private final ClientDAO clientDAO = new ClientDAOImpl();
    @Autowired
    private final BonusCardDAO bonusCardDAO = new BonusCardDAOImpl();
    @Autowired
    private final CompanyDAO companyDAO = new CompanyDAOImpl();

    @PostMapping("client_creation")
    public String createClient(@RequestParam String firstName,
                               @RequestParam String lastName,
                               @RequestParam String fathersName,
                               @RequestParam String phoneNumber,
                               @RequestParam String email,
                               @RequestParam String address,
                               @RequestParam(required = false)  List<Integer> bonusIds,
                               @RequestParam(required = false) List<Integer> bonusCompanies,
                               @RequestParam(required = false) List<BigDecimal> bonusAmounts) {
        if (firstName.isBlank() || lastName.isBlank() || fathersName.isBlank() || phoneNumber.isBlank()) {
            return "error";
        }

        Client client = clientDAO.getByPhoneNumber(phoneNumber);

        if (client != null) {
            return "error";
        }

        client = new Client();
        client.setFirstName(firstName);
        client.setLastName(lastName);
        client.setFathersName(fathersName);
        client.setPhoneNumber(phoneNumber);
        client.setEmail(email);
        client.setAddress(address);

        clientDAO.save(client);
        client = clientDAO.getByPhoneNumber(phoneNumber);

        if (bonusIds != null) {
            for (int i = 0; i < bonusIds.size(); i++) {
                Integer id = bonusIds.get(i);
                Integer companyId = bonusCompanies.get(i);
                BigDecimal amount = bonusAmounts.get(i);
                BonusCard bonusCard = bonusCardDAO.getById(id);
                if (bonusCard != null) {
                    return "error";
                }

                Company company = companyDAO.getById(companyId);

                if (company == null) {
                    return "error";
                }

                bonusCard = new BonusCard(id, company, client, amount);
                bonusCardDAO.save(bonusCard);
            }
        }

        return "redirect:/client?clientId=" + client.getId();
    }

    private BonusCard createBonusCard(Integer bonusId, Company company, BigDecimal amount, Client client) {
        BonusCard bonusCard = bonusCardDAO.getById(bonusId);
        if (bonusCard != null) {
            return null;
        }

        return new BonusCard(bonusId, company, client, amount);
    }

    @PostMapping("client_edit")
    public String editClient(@RequestParam Integer clientId,
                             @RequestParam String firstName,
                             @RequestParam String lastName,
                             @RequestParam String fathersName,
                             @RequestParam String phoneNumber,
                             @RequestParam String email,
                             @RequestParam String address,
                             @RequestParam(required = false) List<Integer> bonusIds,
                             @RequestParam(required = false) List<Integer> bonusCompanies,
                             @RequestParam(required = false) List<BigDecimal> bonusAmounts) {
        if (firstName.isBlank() || lastName.isBlank() || fathersName.isBlank() || phoneNumber.isBlank()) {
            return "error";
        }

        Client client = clientDAO.getById(clientId);

        if (client == null) {
            return "error";
        }

        if (!phoneNumber.equals(client.getPhoneNumber())) {
            Client newClient = clientDAO.getByPhoneNumber(phoneNumber);

            if (newClient != null) {
                return "error";
            }
        }

        client.setFirstName(firstName);
        client.setLastName(lastName);
        client.setFathersName(fathersName);
        client.setPhoneNumber(phoneNumber);
        client.setEmail(email);
        client.setAddress(address);

        clientDAO.update(client);

        Set<Integer> oldBonusCards = bonusCardDAO.getBonusCardsByClientId(clientId).stream().map(BonusCard::getId).collect(Collectors.toSet());

        if (bonusIds != null) {
            for (int i = 0; i < bonusIds.size(); i++) {
                Integer id = bonusIds.get(i);
                Integer companyId = bonusCompanies.get(i);
                Company company = companyDAO.getById(companyId);
                if (company == null) {
                    return "error";
                }

                if (oldBonusCards.contains(id)) {
                    BonusCard bonusCard = bonusCardDAO.getById(id);
                    bonusCard.setAmount(bonusCard.getAmount());
                    bonusCard.setClient(client);
                    bonusCard.setCompany(company);
                    bonusCardDAO.update(bonusCard);
                    oldBonusCards.remove(id);
                } else {
                    BigDecimal amount = bonusAmounts.get(i);
                    BonusCard bonusCard = createBonusCard(id, company, amount, client);
                    if (bonusCard == null) {
                        return "error";
                    }
                    bonusCardDAO.save(bonusCard);
                }
            }
        }

        for (Integer bonusId : oldBonusCards) {
            BonusCard bonusCard = bonusCardDAO.getById(bonusId);
            bonusCardDAO.delete(bonusCard);
        }

        return "redirect:/client?clientId=" + clientId;
    }

    @GetMapping("client_creation")
    public String openClientCreatePage(Model model) {
        List<Company> companies = companyDAO.getAll().stream().toList();
        model.addAttribute("companyList", companies);
        return "client_creation";
    }

    @GetMapping("client_edit")
    public String openClientEditPage(@RequestParam Integer clientId, Model model) {
        Client client = clientDAO.getById(clientId);
        if (client == null) {
            return "error";
        }

        model.addAttribute("client", client);

        List<BonusCard> bonusCards = bonusCardDAO.getBonusCardsByClientId(clientId);

        model.addAttribute("bonusList", bonusCards);

        List<Company> companies = companyDAO.getAll().stream().toList();
        model.addAttribute("companyList", companies);

        return "client_edit";
    }
}
