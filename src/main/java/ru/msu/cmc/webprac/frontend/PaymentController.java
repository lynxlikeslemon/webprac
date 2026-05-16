package ru.msu.cmc.webprac.frontend;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.msu.cmc.webprac.backend.DAO.BonusCardDAO;
import ru.msu.cmc.webprac.backend.DAO.TicketDAO;
import ru.msu.cmc.webprac.backend.DAO.impl.BonusCardDAOImpl;
import ru.msu.cmc.webprac.backend.DAO.impl.TicketDAOImpl;
import ru.msu.cmc.webprac.backend.entity.BonusCard;
import ru.msu.cmc.webprac.backend.entity.Ticket;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;

@Controller
public class PaymentController {
    @Autowired
    private final TicketDAO ticketDAO = new TicketDAOImpl();
    @Autowired
    private final BonusCardDAO bonusCardDAO = new BonusCardDAOImpl();

    @GetMapping("/payment")
    public String getTicket(@RequestParam Integer ticketId,
                            @RequestParam(required = false) Integer bonusId,
                            @RequestParam(required = false) Double bonusAmount,
                            Model model) {
        Ticket ticket = ticketDAO.getById(ticketId);

        if (ticket == null) {
            return "error";
        }

        model.addAttribute("ticket", ticket);

        List<BonusCard> bonusCards = bonusCardDAO.getBonusCardsByClientId(ticket.getClient().getId());

        model.addAttribute("bonusList", bonusCards);

        if (bonusId != null) {
            BonusCard bonusCard = bonusCardDAO.getById(bonusId);

            if (bonusCard == null) {
                return "error";
            }
            model.addAttribute("bonus", bonusCard);
        } else {
            model.addAttribute("bonus", null);
        }

        model.addAttribute("bonusAmount", bonusAmount);

        return "payment";
    }

    @PostMapping("/payment")
    public String payForTicket(@RequestParam Integer ticketId,
                               @RequestParam(required = false) Integer bonusId,
                               @RequestParam(required = false) BigDecimal bonusAmount) {
        Ticket ticket = ticketDAO.getById(ticketId);

        if (ticket == null || ticket.getIsPaidFor()) {
            return "redirect:/payment_error?ticketId=" + ticketId;
        }

        BigDecimal price = ticket.getPrice();

        if (bonusId != null) {
            BonusCard bonusCard = bonusCardDAO.getById(bonusId);

            if (bonusCard == null) {
                return "redirect:/payment_error?ticketId=" + ticketId;
            }

            if (bonusCard.getAmount().compareTo(bonusAmount) < 0) {
                return "redirect:/payment_error?ticketId=" + ticketId;
            }

            price = price.subtract(bonusAmount);

            if (price.compareTo(BigDecimal.ZERO) < 0) {
                return "redirect:/payment_error?ticketId=" + ticketId;
            }

            bonusCard.setAmount(bonusCard.getAmount().subtract(bonusAmount));
            ticket.setBonusCardUsed(bonusCard);
            ticket.setBonusAmountUsed(bonusAmount);
            bonusCardDAO.update(bonusCard);
        }

        ticket.setPaymentTime(new Timestamp(System.currentTimeMillis()));
        ticket.setIsPaidFor(true);
        ticketDAO.update(ticket);
        return "redirect:/payment_success?ticketId=" + ticket.getId();
    }

    @GetMapping("/payment_success")
    public String paymentSuccess(@RequestParam Integer ticketId, Model model) {
        model.addAttribute("ticketId", ticketId);
        return "payment_success";
    }

    @GetMapping("/payment_error")
    public String paymentError(@RequestParam Integer ticketId, Model model) {
        model.addAttribute("ticketId", ticketId);
        return "payment_error";
    }
}
