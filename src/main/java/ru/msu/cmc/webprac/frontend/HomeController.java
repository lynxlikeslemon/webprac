package ru.msu.cmc.webprac.frontend;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class HomeController {
    @RequestMapping(value = {"/", "/index"})
    public String index() {
        return "index";
    }

    @GetMapping("/flightPage")
    public String flightList() {
        return "flights";
    }

    @GetMapping("/clientPage")
    public String clientList() {
        return "clients";
    }

}
