package com.diplom.levanteak.service.interf;

import com.diplom.levanteak.dto.Response;
import com.diplom.levanteak.dto.AddressDto;

public interface AddressService {
    Response saveAndUpdateAddress(AddressDto addressDto);
}
