package com.gamakdragons.wheretruck.domain.user.service;

import com.gamakdragons.wheretruck.common.DeleteResultDto;
import com.gamakdragons.wheretruck.common.IndexUpdateResultDto;
import com.gamakdragons.wheretruck.common.UpdateResultDto;
import com.gamakdragons.wheretruck.domain.user.entity.User;

public interface UserService {
    
    User getById(String id);

    IndexUpdateResultDto saveUser(User user);
    DeleteResultDto deleteUser(String id);
    UpdateResultDto changeNickName(String userId, String nickName);
}
