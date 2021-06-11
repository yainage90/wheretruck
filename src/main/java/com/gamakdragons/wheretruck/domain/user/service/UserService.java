package com.gamakdragons.wheretruck.domain.user.service;

import com.gamakdragons.wheretruck.common.DeleteResultDto;
import com.gamakdragons.wheretruck.common.IndexResultDto;
import com.gamakdragons.wheretruck.common.UpdateResultDto;
import com.gamakdragons.wheretruck.domain.user.entity.User;

public interface UserService {
    
    User getById(String id);

    IndexResultDto saveUser(User user);
    DeleteResultDto deleteUser(String id);
    UpdateResultDto changeNickName(String userId, String nickName);

    UpdateResultDto addFavorite(String userId, String truckId);
    UpdateResultDto deleteFavorite(String userId, String truckId);

}
