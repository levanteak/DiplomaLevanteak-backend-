package com.diplom.levanteak.service.interf;

import com.diplom.levanteak.dto.LoginRequest;
import com.diplom.levanteak.dto.Response;
import com.diplom.levanteak.dto.UserDto;
import com.diplom.levanteak.entity.User;

public interface UserService {
    Response registerUser(UserDto registrationRequest);
    Response loginUser(LoginRequest loginRequest);
    Response getAllUsers();
    User getLoginUser();
    Response getUserInfoAndOrderHistory();
}
