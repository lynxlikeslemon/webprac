package ru.msu.cmc.webprac.frontend;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.msu.cmc.webprac.backend.DAO.BonusCardDAO;
import ru.msu.cmc.webprac.backend.DAO.ClientDAO;
import ru.msu.cmc.webprac.backend.DAO.impl.BonusCardDAOImpl;
import ru.msu.cmc.webprac.backend.DAO.impl.ClientDAOImpl;
import ru.msu.cmc.webprac.backend.entity.BonusCard;
import ru.msu.cmc.webprac.backend.entity.Client;

import java.util.List;

@Controller
public class ClientPageController {
    @Autowired
    private final ClientDAO clientDao = new ClientDAOImpl();
    @Autowired
    private final BonusCardDAO bonusCardDAO = new BonusCardDAOImpl();

    @GetMapping("/client")
    public String getFlightList(@RequestParam Integer clientId,
                                Model model) {
        if (clientId == null) {
            return "error";
        }

        Client client = clientDao.getById(clientId);

        if (client == null) {
            return "error";
        }
        model.addAttribute("client", client);

        List<BonusCard> bonusCards = bonusCardDAO.getBonusCardsByClientId(client.getId());
        model.addAttribute("bonusList", bonusCards);

        return "client";
    }

    @PostMapping("/client")
    public String deleteClient(@RequestParam Integer clientId) {
        Client client = clientDao.getById(clientId);

        if (client == null) {
            return "error";
        }

        List<BonusCard> bonusCards = bonusCardDAO.getBonusCardsByClientId(clientId);

        for (BonusCard bonusCard : bonusCards) {
            bonusCardDAO.delete(bonusCard);
        }

        clientDao.delete(client);

        return "redirect:/clients";
    }
}
