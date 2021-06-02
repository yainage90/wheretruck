package com.gamakdragons.wheretruck.user.service;

import com.gamakdragons.wheretruck.common.DeleteResultDto;
import com.gamakdragons.wheretruck.common.IndexResultDto;
import com.gamakdragons.wheretruck.common.SearchResultDto;
import com.gamakdragons.wheretruck.common.UpdateResultDto;
import com.gamakdragons.wheretruck.user.model.User;

public interface UserService {
    
    User getById(String id);

    SearchResultDto<User> findByEmail(String email);

    IndexResultDto saveUser(User user);
    UpdateResultDto updateUser(User user);
    DeleteResultDto deleteUser(String id);

}
