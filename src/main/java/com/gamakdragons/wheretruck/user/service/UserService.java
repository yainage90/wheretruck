package com.gamakdragons.wheretruck.user.service;

import com.gamakdragons.wheretruck.common.DeleteResultDto;
import com.gamakdragons.wheretruck.common.IndexResultDto;
import com.gamakdragons.wheretruck.common.UpdateResultDto;
import com.gamakdragons.wheretruck.user.entity.User;

public interface UserService {
    
    User getById(String id);

    IndexResultDto saveUser(User user);
    UpdateResultDto updateUser(User user);
    DeleteResultDto deleteUser(String id);

    UpdateResultDto addFavorite(String userId, String truckId);
    UpdateResultDto deleteFavorite(String userId, String truckId);

}
