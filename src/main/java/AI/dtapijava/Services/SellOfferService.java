package AI.dtapijava.Services;

import AI.dtapijava.Components.ExecDetailsHelper;
import AI.dtapijava.DTOs.Request.AddSellOfferReqDTO;
import AI.dtapijava.DTOs.Response.*;
import AI.dtapijava.Entities.Resource;
import AI.dtapijava.Entities.SellOffer;
import AI.dtapijava.Entities.User;
import AI.dtapijava.Exceptions.NotEnoughActionsException;
import AI.dtapijava.Exceptions.ResourceNotFoundException;
import AI.dtapijava.Exceptions.SellOfferNotFoundException;
import AI.dtapijava.Exceptions.UserNotFoundExceptions;
import AI.dtapijava.Infrastructure.Util.UserUtils;
import AI.dtapijava.Repositories.CompanyRepository;
import AI.dtapijava.Repositories.ResourceRepository;
import AI.dtapijava.Repositories.SellOfferRepository;
import AI.dtapijava.Repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SellOfferService {
    @Autowired
    private SellOfferRepository sellOfferRepository;
    @Autowired
    private CompanyRepository companyRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ResourceRepository resourceRepository;
    @Autowired
    private TradeService tradeService;

    public SellOfferExtResDTO getSellOffer(int id) {
        ExecDetailsHelper execHelper = new ExecDetailsHelper();

        execHelper.setStartDbTime(OffsetDateTime.now());
        SellOffer sellOffer = sellOfferRepository.findById(id).orElseThrow(() -> new SellOfferNotFoundException("Sell offer not found"));
        execHelper.addNewDbTime();

        return new SellOfferExtResDTO(new SellOfferResDTO(sellOffer), new ExecDetailsResDTO(execHelper.getDbTime(), execHelper.getExecTime()));
    }

    public SellOffersResDTO getSellOffers() {
        ExecDetailsHelper execHelper = new ExecDetailsHelper();

        execHelper.setStartDbTime(OffsetDateTime.now());
        List<SellOffer> sellOffers = sellOfferRepository.findAll();
        execHelper.addNewDbTime();

        List<SellOfferResDTO> sellOfferResDTOList = sellOffers.stream().map(SellOfferResDTO::new).collect(Collectors.toList());
        return new SellOffersResDTO(sellOfferResDTOList, new ExecDetailsResDTO(execHelper.getDbTime(), execHelper.getExecTime()));
    }

    public SellOffersResDTO getSellOffersValid(Boolean valid) {
        ExecDetailsHelper execHelper = new ExecDetailsHelper();

        execHelper.setStartDbTime(OffsetDateTime.now());
        List<SellOffer> sellOffers = sellOfferRepository.findByIsValid(valid);
        execHelper.addNewDbTime();

        List<SellOfferResDTO> sellOfferResDTOList = sellOffers.stream().map(SellOfferResDTO::new).collect(Collectors.toList());
        return new SellOffersResDTO(sellOfferResDTOList, new ExecDetailsResDTO(execHelper.getDbTime(), execHelper.getExecTime()));
    }

    public ExecTimeResDTO addSellOffer(AddSellOfferReqDTO addSellOfferReqDTO) {
        ExecDetailsHelper execHelper = new ExecDetailsHelper();
        execHelper.setStartDbTime(OffsetDateTime.now());
        User user = userRepository.findById(UserUtils.getCurrentUserId())
                .orElseThrow(() -> new UserNotFoundExceptions("User not found!"));
        Resource resource = resourceRepository.findById(addSellOfferReqDTO.getResourceId()).orElseThrow(() -> new ResourceNotFoundException("Resource not found"));
        execHelper.addNewDbTime();

        if (resource.getAmount() < addSellOfferReqDTO.getAmount()) {
            throw new NotEnoughActionsException("Not enough actions to sell");
        }

        SellOffer sellOffer = SellOffer.builder()
                .resource(resource)
                .isValid(true)
                .amount(addSellOfferReqDTO.getAmount())
                .startAmount(addSellOfferReqDTO.getAmount())
                .date(OffsetDateTime.now())
                .price(addSellOfferReqDTO.getPrice())
                .build();
        execHelper.setStartDbTime(OffsetDateTime.now());
        resource.setAmount(resource.getAmount() - sellOffer.getStartAmount());
        sellOfferRepository.save(sellOffer);
        execHelper.addNewDbTime();

        tradeService.startThread(sellOffer.getResource().getCompany().getID());

        return new ExecTimeResDTO(new ExecDetailsResDTO(execHelper.getDbTime(), execHelper.getExecTime()));
    }

    public ExecTimeResDTO withdrawSellOffer(int id) {
        ExecDetailsHelper execHelper = new ExecDetailsHelper();

        execHelper.setStartDbTime(OffsetDateTime.now());
        SellOffer sellOffer = sellOfferRepository.findById(id).orElseThrow(() -> new SellOfferNotFoundException("Sell offer not found"));
        execHelper.addNewDbTime();
        sellOffer.setIsValid(false);
        execHelper.setStartDbTime(OffsetDateTime.now());
        sellOffer.getResource().setAmount(sellOffer.getResource().getAmount() + sellOffer.getAmount());
        sellOfferRepository.save(sellOffer);
        execHelper.addNewDbTime();

        return new ExecTimeResDTO(new ExecDetailsResDTO(execHelper.getDbTime(), execHelper.getExecTime()));
    }
}
