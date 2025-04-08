package com.diplom.levanteak.service.impl;

import com.diplom.levanteak.dto.AddressDto;
import com.diplom.levanteak.dto.Response;
import com.diplom.levanteak.entity.Address;
import com.diplom.levanteak.entity.User;
import com.diplom.levanteak.service.interf.AddressService;
import com.diplom.levanteak.service.interf.UserService;
import com.diplom.levanteak.repository.AddressRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AddressServiceImpl implements AddressService {

    private final AddressRepo addressRepo;
    private final UserService userService;


    @Override
    public Response saveAndUpdateAddress(AddressDto addressDto) {
        log.info("Starting saveAndUpdateAddress with data: {}", addressDto);

        User user = userService.getLoginUser();
        log.debug("Fetched logged-in user: {}", user.getId());
        Address address = user.getAddress();

        if (address == null){
            log.info("No existing address found. Creating new address.");
            address = new Address();
            address.setUser(user);
        }
        if (addressDto.getStreet() != null) address.setStreet(addressDto.getStreet());
        if (addressDto.getCity() != null) address.setCity(addressDto.getCity());
        if (addressDto.getState() != null) address.setState(addressDto.getState());
        if (addressDto.getZipCode() != null) address.setZipCode(addressDto.getZipCode());
        if (addressDto.getCountry() != null) address.setCountry(addressDto.getCountry());

        addressRepo.save(address);
        log.info("Address saved successfully for user ID: {}", user.getId());

        String message = (user.getAddress() == null) ? "Address successfully created" : "Address successfully updated";
        return Response.builder()
                .status(200)
                .message(message)
                .build();
    }
}
