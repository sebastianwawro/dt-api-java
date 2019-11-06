package AI.dtapijava.DTOs.Response;

import AI.dtapijava.Entities.Company;
import AI.dtapijava.Entities.SellOffer;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@Setter
@Getter
@AllArgsConstructor
public class UserSellOfferResDTO {
    private long id;
    private ResourceCompanyResDTO company;
    private int amount;
    private int startAmount;
    private double price;
    private OffsetDateTime date;

    public UserSellOfferResDTO(SellOffer sellOffer) {
        id = sellOffer.getID();
        company = new ResourceCompanyResDTO(sellOffer.getResource().getCompany());
        amount = sellOffer.getAmount();
        startAmount = sellOffer.getStartAmount();
        price = sellOffer.getPrice();
        date = sellOffer.getDate();
    }
}